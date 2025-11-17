package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public class VideoCallWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 46);
    private static final Color PANEL_COLOR = new Color(40, 42, 54);
    private static final Color ACCENT_COLOR = new Color(139, 233, 253);
    private static final Color ERROR_COLOR = new Color(255, 85, 85);
    private static final Color SUCCESS_COLOR = new Color(80, 250, 123);
    
    private JLabel localVideoLabel;
    private JLabel remoteVideoLabel;
    private JButton endCallButton;
    private JButton muteButton;
    private JButton cameraButton;
    private JLabel statusLabel;
    
    private WebcamCapture webcam;
    private volatile boolean running = false;
    private volatile boolean cameraEnabled = true;
    private volatile boolean disposed = false;
    private Thread captureThread;
    private Thread streamThread;
    private final Object frameLock = new Object();
    private long lastFrameTime = 0;
    private static final long MIN_FRAME_INTERVAL = 200; // 5 FPS để tránh overload socket
    private int frameSkipCounter = 0;
    
    private String partnerName;
    private String callId;
    private ChatClient client;
    private boolean isVideoCall;
    
    public VideoCallWindow(String partnerName, String callId, boolean videoEnabled, ChatClient client) {
        this.partnerName = partnerName;
        this.callId = callId;
        this.isVideoCall = videoEnabled;
        this.client = client;
        
        initComponents();
        
        if (videoEnabled) {
            startCamera();
        } else {
            // Audio only mode
            localVideoLabel.setText("Audio Only Call");
            remoteVideoLabel.setText("Audio Only Call");
            localVideoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            remoteVideoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            localVideoLabel.setForeground(Color.WHITE);
            remoteVideoLabel.setForeground(Color.WHITE);
        }
    }
    
    private void initComponents() {
        String windowTitle = isVideoCall ? "Video Call - " + partnerName : "Audio Call - " + partnerName;
        setTitle(windowTitle);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Handle window close event
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                endCall();
            }
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        statusLabel = new JLabel("Connected with " + partnerName);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        localVideoLabel = new JLabel("Camera starting...");
        localVideoLabel.setHorizontalAlignment(JLabel.CENTER);
        localVideoLabel.setBackground(Color.BLACK);
        localVideoLabel.setOpaque(true);
        
        remoteVideoLabel = new JLabel("Waiting for video...");
        remoteVideoLabel.setHorizontalAlignment(JLabel.CENTER);
        remoteVideoLabel.setBackground(Color.BLACK);
        remoteVideoLabel.setOpaque(true);
        
        JPanel videoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        videoPanel.add(localVideoLabel);
        videoPanel.add(remoteVideoLabel);
        
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(videoPanel, BorderLayout.CENTER);
        
        endCallButton = new JButton("End Call");
        endCallButton.addActionListener(e -> endCall());
        mainPanel.add(endCallButton, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void startCamera() {
        running = true;
        captureThread = new Thread(() -> {
            WebcamCapture localWebcam = null;
            try {
                localWebcam = new WebcamCapture();
                webcam = localWebcam;
                
                if (!webcam.isAvailable()) {
                    SwingUtilities.invokeLater(() -> localVideoLabel.setText("No camera"));
                    return;
                }
                
                webcam.start();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Camera active"));
                
                int frameCount = 0;
                long startTime = System.currentTimeMillis();

                while (running && cameraEnabled && !disposed) {
                    try {
                        BufferedImage image = webcam.captureFrame();
                        if (image == null) {
                            Thread.sleep(MIN_FRAME_INTERVAL);
                            continue;
                        }
                        
                        // Rate limiting - chỉ gửi nếu đủ khoảng thời gian
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastFrameTime < MIN_FRAME_INTERVAL) {
                            Thread.sleep(MIN_FRAME_INTERVAL - (currentTime - lastFrameTime));
                            continue;
                        }
                        lastFrameTime = currentTime;
                        
                        // Hiển thị local (scale nhẹ để giảm CPU)
                        final BufferedImage finalImage = image;
                        SwingUtilities.invokeLater(() -> {
                            try {
                                if (localVideoLabel.getWidth() > 0 && localVideoLabel.getHeight() > 0) {
                                    ImageIcon scaledIcon = new ImageIcon(finalImage.getScaledInstance(
                                        localVideoLabel.getWidth(), localVideoLabel.getHeight(), Image.SCALE_FAST));
                                    localVideoLabel.setIcon(scaledIcon);
                                }
                            } catch (Exception e) {
                                // Ignore display errors
                            }
                        });
                        
                        // Stream tới remote user (throttled heavily)
                        frameCount++;
                        frameSkipCounter++;
                        if (client != null && frameSkipCounter >= 3) { // Chỉ gửi mỗi 3 frame = ~2 FPS
                            frameSkipCounter = 0;
                            try {
                                // Resize nhỏ hơn để giảm bandwidth
                                BufferedImage resized = resizeImage(image, 240, 180); // Giảm size
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(resized, "jpg", baos);
                                byte[] frameData = baos.toByteArray();
                                baos.close();
                                
                                // Kiểm tra kích thước trước khi gửi
                                if (frameData.length < 50_000) { // Max 50KB thay vì 100KB
                                    client.sendVideoFrame(partnerName, callId, frameData);
                                } else {
                                    System.err.println("Frame too large: " + frameData.length + " - skipping");
                                }
                            } catch (Exception e) {
                                System.err.println("Error streaming frame: " + e.getMessage());
                            }
                        }
                        
                        // FPS monitoring
                        if (frameCount % 30 == 0) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            double fps = (frameCount * 1000.0) / elapsed;
                            System.out.println("Video FPS: " + String.format("%.1f", fps));
                        }
                        
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        System.err.println("Error in capture loop: " + e.getMessage());
                        Thread.sleep(500); // Wait before retry
                    }
                }
            } catch (Exception e) {
                System.err.println("Camera error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (localWebcam != null) {
                    try {
                        localWebcam.stop();
                    } catch (Exception e) {
                        System.err.println("Error stopping webcam: " + e.getMessage());
                    }
                }
            }
        }, "VideoCapture-" + partnerName);
        captureThread.setDaemon(true);
        captureThread.start();
    }
    
    /**
     * Resize image để giảm bandwidth
     */
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
    
    public void endCall() {
        if (disposed) return;
        disposed = true;
        running = false;
        
        try {
            if (webcam != null) {
                webcam.stop();
            }
        } catch (Exception e) {
            System.err.println("Error stopping webcam: " + e.getMessage());
        }
        
        try {
            if (captureThread != null && captureThread.isAlive()) {
                captureThread.interrupt();
                captureThread.join(1000); // Wait max 1s
            }
        } catch (Exception e) {
            // Ignore
        }
        
        try {
            if (streamThread != null && streamThread.isAlive()) {
                streamThread.interrupt();
            }
        } catch (Exception e) {
            // Ignore
        }
        
        try {
            if (client != null) {
                client.endVideoCall(partnerName, callId);
                client.cleanupCallWindow();
            }
        } catch (Exception e) {
            System.err.println("Error notifying client: " + e.getMessage());
        }
        
        try {
            dispose();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Force close without sending end message (khi remote đã end)
     */
    public void forceClose() {
        if (disposed) return;
        disposed = true;
        running = false;
        
        try {
            if (webcam != null) {
                webcam.stop();
            }
        } catch (Exception e) {
            // Ignore
        }
        
        try {
            if (captureThread != null && captureThread.isAlive()) {
                captureThread.interrupt();
                captureThread.join(1000);
            }
        } catch (Exception e) {
            // Ignore
        }
        
        try {
            if (streamThread != null && streamThread.isAlive()) {
                streamThread.interrupt();
            }
        } catch (Exception e) {
            // Ignore
        }
        
        try {
            if (client != null) {
                client.cleanupCallWindow();
            }
        } catch (Exception e) {
            // Ignore
        }
        
        try {
            dispose();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Hiển thị remote frame từ byte array
     */
    public void displayRemoteFrame(byte[] frameData) {
        if (disposed || frameData == null || frameData.length == 0) {
            return;
        }
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(frameData);
            BufferedImage image = ImageIO.read(bais);
            bais.close();
            
            if (image != null && !disposed) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (!disposed && remoteVideoLabel != null && 
                            remoteVideoLabel.getWidth() > 0 && remoteVideoLabel.getHeight() > 0) {
                            ImageIcon scaledIcon = new ImageIcon(image.getScaledInstance(
                                remoteVideoLabel.getWidth(), remoteVideoLabel.getHeight(), Image.SCALE_FAST));
                            remoteVideoLabel.setIcon(scaledIcon);
                            remoteVideoLabel.setText(""); // Xóa text "Waiting for video..."
                        }
                    } catch (Exception e) {
                        // Ignore - window might be closing
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error displaying remote frame: " + e.getMessage());
        }
    }
}

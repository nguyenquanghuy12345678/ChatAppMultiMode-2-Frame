package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.sound.sampled.*; // Thư viện âm thanh
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public class VideoCallWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 46);
    
    private JLabel localVideoLabel;
    private JLabel remoteVideoLabel;
    private JButton endCallButton;
    private JLabel statusLabel;
    
    private WebcamCapture webcam;
    private volatile boolean running = false;
    private volatile boolean cameraEnabled = true;
    private Thread captureThread;
    
    // Performance improvements
    private static final int TARGET_FPS = 25;
    private static final int FRAME_DELAY_MS = 1000 / TARGET_FPS; // 40ms for 25 FPS
    private long lastFrameTime = 0;
    private int droppedFrames = 0;
    
    // --- AUDIO COMPONENTS ---
    private TargetDataLine microphone; // Thu
    private SourceDataLine speakers;   // Phát
    private Thread audioThread;
    
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
        
        // Bắt đầu Video nếu có yêu cầu
        if (videoEnabled) {
            startCamera();
        } else {
            localVideoLabel.setText("AUDIO ONLY");
            remoteVideoLabel.setText("AUDIO ONLY");
            localVideoLabel.setForeground(Color.WHITE);
            remoteVideoLabel.setForeground(Color.WHITE);
            localVideoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            remoteVideoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        }
        
        // Luôn bắt đầu Audio khi gọi
        startAudio();
    }
    
    private void initComponents() {
        setTitle((isVideoCall ? "Video" : "Audio") + " Call - " + partnerName);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        statusLabel = new JLabel("Calling " + partnerName + "...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        
        JPanel videoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        videoPanel.setBackground(BACKGROUND_COLOR);
        
        localVideoLabel = createVideoLabel("Local");
        remoteVideoLabel = createVideoLabel("Remote");
        
        videoPanel.add(localVideoLabel);
        videoPanel.add(remoteVideoLabel);
        mainPanel.add(videoPanel, BorderLayout.CENTER);
        
        endCallButton = new JButton("End Call");
        endCallButton.addActionListener(e -> endCall());
        mainPanel.add(endCallButton, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JLabel createVideoLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setBackground(Color.BLACK);
        lbl.setOpaque(true);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }
    
    // --- CAMERA LOGIC ---
    private void startCamera() {
        running = true;
        captureThread = new Thread(() -> {
            WebcamCapture localWebcam = null;
            try {
                localWebcam = new WebcamCapture();
                if (localWebcam.isAvailable()) {
                    localWebcam.start();
                    webcam = localWebcam;
                    
                    lastFrameTime = System.currentTimeMillis();
                    
                    while (running && cameraEnabled) {
                        long currentTime = System.currentTimeMillis();
                        long timeSinceLastFrame = currentTime - lastFrameTime;
                        
                        // Frame rate limiting
                        if (timeSinceLastFrame < FRAME_DELAY_MS) {
                            Thread.sleep(FRAME_DELAY_MS - timeSinceLastFrame);
                            continue;
                        }
                        
                        try {
                            BufferedImage img = webcam.captureFrame();
                            if (img != null) {
                                lastFrameTime = currentTime;
                                
                                // Show Local (non-blocking)
                                BufferedImage localImg = img;
                                SwingUtilities.invokeLater(() -> {
                                    try {
                                        ImageIcon icon = new ImageIcon(localImg.getScaledInstance(
                                            localVideoLabel.getWidth(), localVideoLabel.getHeight(), Image.SCALE_FAST));
                                        localVideoLabel.setIcon(icon);
                                    } catch (Exception e) {
                                        // Ignore UI update errors
                                    }
                                });
                                
                                // Send Remote with compression
                                try {
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    // Use JPEG writer with quality control
                                    if (!ImageIO.write(img, "jpg", baos)) {
                                        System.err.println("Failed to encode frame");
                                        continue;
                                    }
                                    
                                    byte[] frameData = baos.toByteArray();
                                    
                                    // Drop frame if too large (> 100KB)
                                    if (frameData.length > 102400) {
                                        droppedFrames++;
                                        if (droppedFrames % 10 == 0) {
                                            System.out.println("⚠ Dropped " + droppedFrames + " frames (too large)");
                                        }
                                        continue;
                                    }
                                    
                                    client.sendVideoFrame(partnerName, callId, frameData);
                                } catch (Exception e) {
                                    System.err.println("Error encoding/sending frame: " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error capturing frame: " + e.getMessage());
                            Thread.sleep(100); // Back off on errors
                        }
                    }
                    
                    if (webcam != null) {
                        webcam.stop();
                    }
                    
                    System.out.println("Camera stopped. Total dropped frames: " + droppedFrames);
                } else {
                    SwingUtilities.invokeLater(() -> {
                        localVideoLabel.setText("NO CAMERA");
                        localVideoLabel.setForeground(Color.RED);
                    });
                }
            } catch (Exception e) { 
                System.err.println("Camera thread error: " + e.getMessage());
                e.printStackTrace();
                if (localWebcam != null) {
                    try {
                        localWebcam.forceRelease();
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                }
            }
        });
        captureThread.setName("VideoCapture-" + partnerName);
        captureThread.start();
    }
    
    // --- AUDIO LOGIC (MỚI) ---
    private void startAudio() {
        try {
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            
            // Setup Mic
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
            if (AudioSystem.isLineSupported(micInfo)) {
                microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
                microphone.open(format);
                microphone.start();
                
                running = true;
                audioThread = new Thread(() -> {
                    byte[] buffer = new byte[1024];
                    while (running && microphone.isOpen()) {
                        int read = microphone.read(buffer, 0, buffer.length);
                        if (read > 0) {
                            client.sendAudioData(partnerName, callId, buffer);
                        }
                    }
                });
                audioThread.start();
            } else {
                System.err.println("Microphone not supported!");
            }
            
            // Setup Speaker
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
            if (AudioSystem.isLineSupported(speakerInfo)) {
                speakers = (SourceDataLine) AudioSystem.getLine(speakerInfo);
                speakers.open(format);
                speakers.start();
            } else {
                System.err.println("Speakers not supported!");
            }
            
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public void playAudio(byte[] data) {
        if (speakers != null && data != null) {
            speakers.write(data, 0, data.length);
        }
    }
    
    public void displayRemoteFrame(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            BufferedImage img = ImageIO.read(bais);
            if (img != null) {
                SwingUtilities.invokeLater(() -> {
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(
                        remoteVideoLabel.getWidth(), remoteVideoLabel.getHeight(), Image.SCALE_FAST));
                    remoteVideoLabel.setIcon(icon);
                });
            }
        } catch (Exception e) {}
    }
    
    public void endCall() {
        running = false;
        
        // Stop camera
        if (webcam != null) {
            try {
                webcam.forceRelease();
            } catch (Exception e) {
                System.err.println("Error stopping webcam: " + e.getMessage());
            }
        }
        
        if (captureThread != null) {
            captureThread.interrupt();
            try {
                captureThread.join(2000); // Wait max 2s
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Stop audio
        if (microphone != null) {
            try {
                microphone.stop();
                microphone.close();
            } catch (Exception e) {
                System.err.println("Error stopping microphone: " + e.getMessage());
            }
        }
        
        if (speakers != null) {
            try {
                speakers.stop();
                speakers.close();
            } catch (Exception e) {
                System.err.println("Error stopping speakers: " + e.getMessage());
            }
        }
        
        if (audioThread != null) {
            audioThread.interrupt();
            try {
                audioThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        client.endVideoCall(partnerName, callId);
        client.cleanupCallWindow();
        dispose();
    }
    
    public void forceClose() {
        running = false;
        
        if (webcam != null) {
            try {
                webcam.forceRelease();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        if (microphone != null) {
            try {
                microphone.stop();
                microphone.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        if (speakers != null) {
            try {
                speakers.stop();
                speakers.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        client.cleanupCallWindow();
        dispose();
    }
}
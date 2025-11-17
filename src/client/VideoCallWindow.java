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
    private Thread captureThread;
    private Thread streamThread;
    
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
            try {
                webcam = new WebcamCapture();
                if (!webcam.isAvailable()) {
                    SwingUtilities.invokeLater(() -> localVideoLabel.setText("No camera"));
                    return;
                }
                
                webcam.start();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Camera active"));

                while (running && cameraEnabled) {
                    BufferedImage image = webcam.captureFrame();
                    if (image != null) {
                        // Hiển thị local
                        SwingUtilities.invokeLater(() -> {
                            ImageIcon scaledIcon = new ImageIcon(image.getScaledInstance(
                                localVideoLabel.getWidth(), localVideoLabel.getHeight(), Image.SCALE_FAST));
                            localVideoLabel.setIcon(scaledIcon);
                        });
                        
                        // Stream tới remote user
                        if (client != null) {
                            try {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(image, "jpg", baos);
                                byte[] frameData = baos.toByteArray();
                                client.sendVideoFrame(partnerName, callId, frameData);
                            } catch (Exception e) {
                                // Ignore streaming errors
                            }
                        }
                    }
                    Thread.sleep(100); // ~10 FPS for network streaming
                }
                webcam.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        captureThread.start();
    }
    
    public void endCall() {
        running = false;
        if (webcam != null) webcam.stop();
        if (captureThread != null) captureThread.interrupt();
        if (streamThread != null) streamThread.interrupt();
        if (client != null) {
            client.endVideoCall(partnerName, callId);
            client.cleanupCallWindow();
        }
        dispose();
    }
    
    /**
     * Force close without sending end message (khi remote đã end)
     */
    public void forceClose() {
        running = false;
        if (webcam != null) webcam.stop();
        if (captureThread != null) captureThread.interrupt();
        if (streamThread != null) streamThread.interrupt();
        if (client != null) {
            client.cleanupCallWindow();
        }
        dispose();
    }
    
    /**
     * Hiển thị remote frame từ byte array
     */
    public void displayRemoteFrame(byte[] frameData) {
        if (frameData != null && frameData.length > 0) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(frameData);
                BufferedImage image = ImageIO.read(bais);
                if (image != null) {
                    SwingUtilities.invokeLater(() -> {
                        ImageIcon scaledIcon = new ImageIcon(image.getScaledInstance(
                            remoteVideoLabel.getWidth(), remoteVideoLabel.getHeight(), Image.SCALE_FAST));
                        remoteVideoLabel.setIcon(scaledIcon);
                        remoteVideoLabel.setText(""); // Xóa text "Waiting for video..."
                    });
                }
            } catch (Exception e) {
                System.err.println("Error displaying remote frame: " + e.getMessage());
            }
        }
    }
}

package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

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
        }
    }
    
    private void initComponents() {
        setTitle("Video Call - " + partnerName);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
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
        new Thread(() -> {
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
                        SwingUtilities.invokeLater(() -> localVideoLabel.setIcon(new ImageIcon(image)));
                    }
                    Thread.sleep(33);
                }
                webcam.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    public void endCall() {
        running = false;
        if (webcam != null) webcam.stop();
        if (client != null) client.endVideoCall(partnerName, callId);
        dispose();
    }
    
    public void displayRemoteFrame(BufferedImage image) {
        if (image != null) {
            SwingUtilities.invokeLater(() -> remoteVideoLabel.setIcon(new ImageIcon(image)));
        }
    }
}

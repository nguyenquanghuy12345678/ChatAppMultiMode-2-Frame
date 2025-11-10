package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Video Call Window with real camera support using Webcam Capture library
 */
public class VideoCallWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    // UI Colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 46);
    private static final Color PANEL_COLOR = new Color(40, 42, 54);
    private static final Color ACCENT_COLOR = new Color(139, 233, 253);
    private static final Color ERROR_COLOR = new Color(255, 85, 85);
    private static final Color SUCCESS_COLOR = new Color(80, 250, 123);

    // Components
    private JLabel localVideoLabel;
    private JLabel remoteVideoLabel;
    private JButton endCallButton;
    private JButton muteButton;
    private JButton cameraButton;
    private JLabel statusLabel;

    // Video capture
    private WebcamCapture webcam;
    private volatile boolean running = false;
    private volatile boolean cameraEnabled = true;
    private Thread captureThread;

    // Call info
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

        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleEndCall();
            }
        });

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top status bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PANEL_COLOR);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                new EmptyBorder(10, 15, 10, 15)));

        statusLabel = new JLabel("Connected with " + partnerName);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(SUCCESS_COLOR);
        topPanel.add(statusLabel, BorderLayout.WEST);

        JLabel callIdLabel = new JLabel("Call ID: " + callId.substring(0, Math.min(8, callId.length())) + "...");
        callIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        callIdLabel.setForeground(ACCENT_COLOR);
        topPanel.add(callIdLabel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Video panels
        JPanel videoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        videoPanel.setBackground(BACKGROUND_COLOR);

        // Local video (You)
        JPanel localPanel = new JPanel(new BorderLayout());
        localPanel.setBackground(PANEL_COLOR);
        localPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                new EmptyBorder(5, 5, 5, 5)));

        JLabel localTitle = new JLabel("You", JLabel.CENTER);
        localTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        localTitle.setForeground(ACCENT_COLOR);
        localPanel.add(localTitle, BorderLayout.NORTH);

        localVideoLabel = new JLabel();
        localVideoLabel.setHorizontalAlignment(JLabel.CENTER);
        localVideoLabel.setVerticalAlignment(JLabel.CENTER);
        localVideoLabel.setBackground(Color.BLACK);
        localVideoLabel.setOpaque(true);
        localVideoLabel.setText("Camera starting...");
        localVideoLabel.setForeground(Color.WHITE);
        localPanel.add(localVideoLabel, BorderLayout.CENTER);

        // Remote video (Partner)
        JPanel remotePanel = new JPanel(new BorderLayout());
        remotePanel.setBackground(PANEL_COLOR);
        remotePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
                new EmptyBorder(5, 5, 5, 5)));

        JLabel remoteTitle = new JLabel(partnerName, JLabel.CENTER);
        remoteTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        remoteTitle.setForeground(SUCCESS_COLOR);
        remotePanel.add(remoteTitle, BorderLayout.NORTH);

        remoteVideoLabel = new JLabel();
        remoteVideoLabel.setHorizontalAlignment(JLabel.CENTER);
        remoteVideoLabel.setVerticalAlignment(JLabel.CENTER);
        remoteVideoLabel.setBackground(Color.BLACK);
        remoteVideoLabel.setOpaque(true);
        remoteVideoLabel.setText("Waiting for video...");
        remoteVideoLabel.setForeground(Color.WHITE);
        remotePanel.add(remoteVideoLabel, BorderLayout.CENTER);

        videoPanel.add(localPanel);
        videoPanel.add(remotePanel);

        mainPanel.add(videoPanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(BACKGROUND_COLOR);

        // Mute button
        muteButton = createControlButton("Mute", new Color(241, 196, 15));
        muteButton.addActionListener(e -> toggleMute());
        muteButton.setEnabled(false);

        // Camera toggle button
        cameraButton = createControlButton("Camera", SUCCESS_COLOR);
        cameraButton.addActionListener(e -> toggleCamera());
        if (!isVideoCall) {
            cameraButton.setEnabled(false);
        }

        // End call button
        endCallButton = createControlButton("End Call", ERROR_COLOR);
        endCallButton.addActionListener(e -> handleEndCall());

        controlPanel.add(muteButton);
        controlPanel.add(cameraButton);
        controlPanel.add(endCallButton);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton createControlButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(140, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor.brighter());
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                }
            }
        });

        return button;
    }

    private void startCamera() {
        running = true;
        captureThread = new Thread(() -> {
            try {
                webcam = new WebcamCapture();
                
                if (!webcam.isAvailable()) {
                    SwingUtilities.invokeLater(() -> {
                        localVideoLabel.setText("<html><center>No webcam detected!<br><br>Please connect a camera</center></html>");
                        localVideoLabel.setForeground(ERROR_COLOR);
                        statusLabel.setText("No camera available");
                        statusLabel.setForeground(ERROR_COLOR);
                        cameraButton.setEnabled(false);
                    });
                    return;
                }
                
                webcam.start();

                SwingUtilities.invokeLater(() -> {
                    localVideoLabel.setText("");
                    statusLabel.setText("Camera active - Connected with " + partnerName);
                });

                while (running && cameraEnabled) {
                    BufferedImage image = webcam.captureFrame();

                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(
                                localVideoLabel.getWidth() > 0 ? localVideoLabel.getWidth() : 400,
                                localVideoLabel.getHeight() > 0 ? localVideoLabel.getHeight() : 300,
                                Image.SCALE_FAST);

                        SwingUtilities.invokeLater(() -> {
                            localVideoLabel.setIcon(new ImageIcon(scaledImage));
                        });
                        
                        // TODO: Send frame to partner through client
                        // client.sendVideoFrame(image, partnerName);
                    }

                    Thread.sleep(33); // ~30 FPS
                }

                webcam.stop();

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    localVideoLabel.setText("<html><center>Camera Error!<br>" +
                            e.getMessage() + "<br><br>Check if camera is available</center></html>");
                    localVideoLabel.setForeground(ERROR_COLOR);
                    statusLabel.setText("Camera failed - Audio only");
                    statusLabel.setForeground(ERROR_COLOR);
                    cameraButton.setEnabled(false);
                });
                e.printStackTrace();
            }
        });

        captureThread.start();
    }

    private void toggleCamera() {
        cameraEnabled = !cameraEnabled;

        if (cameraEnabled) {
            cameraButton.setText("Camera");
            cameraButton.setBackground(SUCCESS_COLOR);
            localVideoLabel.setText("Restarting camera...");
            statusLabel.setText("Camera starting...");

            if (!running) {
                startCamera();
            }
        } else {
            cameraButton.setText("Camera OFF");
            cameraButton.setBackground(new Color(150, 150, 150));
            localVideoLabel.setIcon(null);
            localVideoLabel.setText("Camera disabled");
            statusLabel.setText("Camera disabled");
        }
    }

    private void toggleMute() {
        JOptionPane.showMessageDialog(this,
                "Audio functionality not yet implemented.\n" +
                        "This requires additional audio libraries.",
                "Audio Not Available",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleEndCall() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "End call with " + partnerName + "?",
                "End Call",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            endCall();
        }
    }

    public void endCall() {
        running = false;
        cameraEnabled = false;

        try {
            if (webcam != null) {
                webcam.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (client != null) {
            client.endVideoCall(partnerName, callId);
        }

        dispose();
    }

    public void displayRemoteFrame(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            if (image != null) {
                remoteVideoLabel.setText("");
                Image scaledImage = image.getScaledInstance(
                        remoteVideoLabel.getWidth() > 0 ? remoteVideoLabel.getWidth() : 400,
                        remoteVideoLabel.getHeight() > 0 ? remoteVideoLabel.getHeight() : 300,
                        Image.SCALE_FAST);
                remoteVideoLabel.setIcon(new ImageIcon(scaledImage));
            }
        });
    }
}

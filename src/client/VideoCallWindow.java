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
            try {
                webcam = new WebcamCapture();
                if (webcam.isAvailable()) {
                    webcam.start();
                    while (running && cameraEnabled) {
                        BufferedImage img = webcam.captureFrame();
                        if (img != null) {
                            // Show Local
                            SwingUtilities.invokeLater(() -> {
                                ImageIcon icon = new ImageIcon(img.getScaledInstance(
                                    localVideoLabel.getWidth(), localVideoLabel.getHeight(), Image.SCALE_FAST));
                                localVideoLabel.setIcon(icon);
                            });
                            // Send Remote
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(img, "jpg", baos);
                            client.sendVideoFrame(partnerName, callId, baos.toByteArray());
                        }
                        Thread.sleep(100);
                    }
                    webcam.stop();
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
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
        if (webcam != null) webcam.stop();
        if (captureThread != null) captureThread.interrupt();
        
        if (microphone != null) microphone.close();
        if (speakers != null) speakers.close();
        if (audioThread != null) audioThread.interrupt();
        
        client.endVideoCall(partnerName, callId);
        client.cleanupCallWindow();
        dispose();
    }
    
    public void forceClose() {
        running = false;
        if (webcam != null) webcam.stop();
        if (microphone != null) microphone.close();
        if (speakers != null) speakers.close();
        client.cleanupCallWindow();
        dispose();
    }
}
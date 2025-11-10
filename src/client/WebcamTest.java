package client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Test webcam capture functionality
 */
public class WebcamTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Webcam Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            JLabel videoLabel = new JLabel();
            videoLabel.setHorizontalAlignment(JLabel.CENTER);
            videoLabel.setVerticalAlignment(JLabel.CENTER);
            videoLabel.setBackground(Color.BLACK);
            videoLabel.setOpaque(true);

            JLabel statusLabel = new JLabel("Initializing camera...");
            statusLabel.setHorizontalAlignment(JLabel.CENTER);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

            frame.setLayout(new BorderLayout());
            frame.add(videoLabel, BorderLayout.CENTER);
            frame.add(statusLabel, BorderLayout.SOUTH);

            frame.setVisible(true);

            // Start webcam in separate thread
            new Thread(() -> {
                try {
                    WebcamCapture webcam = new WebcamCapture();

                    if (!webcam.isAvailable()) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("❌ No webcam detected!");
                            videoLabel.setText("Please connect a camera");
                        });
                        return;
                    }

                    webcam.start();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("✅ Camera running - Press Ctrl+C to stop");
                    });

                    int frameCount = 0;
                    while (true) {
                        BufferedImage image = webcam.captureFrame();

                        if (image != null) {
                            frameCount++;
                            final int fc = frameCount;

                            // Scale to fit window
                            Image scaledImage = image.getScaledInstance(
                                    videoLabel.getWidth() > 0 ? videoLabel.getWidth() : 640,
                                    videoLabel.getHeight() > 0 ? videoLabel.getHeight() : 480,
                                    Image.SCALE_FAST);

                            SwingUtilities.invokeLater(() -> {
                                videoLabel.setIcon(new ImageIcon(scaledImage));
                                statusLabel.setText(String.format("✅ Frame: %d | Resolution: %dx%d | FPS: ~30",
                                        fc, image.getWidth(), image.getHeight()));
                            });
                        }

                        Thread.sleep(33); // ~30 FPS
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("❌ Error: " + e.getMessage());
                        videoLabel.setText("<html><center>Camera Error!<br>" + e.getMessage() + "</center></html>");
                    });
                }
            }).start();
        });
    }
}

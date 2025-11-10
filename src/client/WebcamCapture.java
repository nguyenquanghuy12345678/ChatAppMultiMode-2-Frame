package client;

import com.github.sarxos.webcam.Webcam;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Webcam capture using Sarxos Webcam Capture library
 * Real camera support!
 */
public class WebcamCapture {

    private Webcam webcam;
    private boolean available = false;
    private boolean isOpen = false;

    public WebcamCapture() {
        try {
            System.out.println("Initializing webcam...");
            webcam = Webcam.getDefault();
            if (webcam != null) {
                available = true;
                System.out.println("✓ Webcam found: " + webcam.getName());
            } else {
                System.out.println("✗ No webcam detected - using demo mode");
            }
        } catch (Exception e) {
            System.err.println("✗ Error initializing webcam: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isAvailable() {
        return available && webcam != null;
    }

    public void start() {
        if (webcam != null && !webcam.isOpen()) {
            try {
                // Force close if locked by another instance
                if (webcam.getLock().isLocked()) {
                    System.out.println("⚠ Camera is locked, forcing release...");
                    try {
                        webcam.close();
                        Thread.sleep(500); // Wait for release
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
                
                webcam.setViewSize(new Dimension(640, 480));
                webcam.open();
                isOpen = true;
                System.out.println("✓ Webcam started");
            } catch (Exception e) {
                System.err.println("✗ Error starting webcam: " + e.getMessage());
                System.err.println("💡 Tip: Close other apps using camera (WebcamTest, Zoom, Skype, etc.)");
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (webcam != null && webcam.isOpen()) {
            try {
                webcam.close();
                isOpen = false;
                System.out.println("✓ Webcam stopped");
            } catch (Exception e) {
                System.err.println("✗ Error stopping webcam: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Force release camera even if locked
     */
    public void forceRelease() {
        if (webcam != null) {
            try {
                if (webcam.isOpen()) {
                    webcam.close();
                }
                isOpen = false;
                System.out.println("✓ Webcam force released");
            } catch (Exception e) {
                System.err.println("✗ Error releasing webcam: " + e.getMessage());
            }
        }
    }

    public BufferedImage captureFrame() {
        if (webcam != null && webcam.isOpen()) {
            try {
                return webcam.getImage();
            } catch (Exception e) {
                System.err.println("✗ Error capturing frame: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public Dimension getViewSize() {
        if (webcam != null) {
            return webcam.getViewSize();
        }
        return new Dimension(640, 480);
    }
}

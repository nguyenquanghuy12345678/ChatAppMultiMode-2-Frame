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
    private final Object lock = new Object();
    
    // Will be set to best available resolution
    private Dimension selectedResolution = null;

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

    public synchronized void start() {
        synchronized (lock) {
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
                    
                    // Get supported resolutions and choose best one
                    Dimension[] supportedSizes = webcam.getViewSizes();
                    if (supportedSizes != null && supportedSizes.length > 0) {
                        System.out.print("Supported resolutions: ");
                        for (Dimension d : supportedSizes) {
                            System.out.print(d.width + "x" + d.height + " ");
                        }
                        System.out.println();
                        
                        // Priority: 320x240 (best balance) > 640x480 (quality) > others
                        selectedResolution = chooseBestResolution(supportedSizes);
                    } else {
                        // Fallback to default
                        selectedResolution = new Dimension(640, 480);
                    }
                    
                    webcam.setViewSize(selectedResolution);
                    webcam.open();
                    isOpen = true;
                    System.out.println("✓ Webcam started (" + selectedResolution.width + "x" + selectedResolution.height + ")");
                } catch (Exception e) {
                    System.err.println("✗ Error starting webcam: " + e.getMessage());
                    System.err.println("💡 Tip: Close other apps using camera (WebcamTest, Zoom, Skype, etc.)");
                    e.printStackTrace();
                    available = false;
                }
            }
        }
    }
    
    /**
     * Choose best resolution from supported list
     * Priority: 320x240 (balance) > 640x480 (quality) > 176x144 (fallback) > largest available
     */
    private Dimension chooseBestResolution(Dimension[] supported) {
        // First choice: 320x240 (best balance between quality and performance)
        for (Dimension d : supported) {
            if (d.width == 320 && d.height == 240) {
                return d;
            }
        }
        
        // Second choice: 640x480 (higher quality)
        for (Dimension d : supported) {
            if (d.width == 640 && d.height == 480) {
                return d;
            }
        }
        
        // Third choice: 176x144 (low quality but fast)
        for (Dimension d : supported) {
            if (d.width == 176 && d.height == 144) {
                return d;
            }
        }
        
        // Fallback: return largest available
        Dimension largest = supported[0];
        for (Dimension d : supported) {
            if (d.width * d.height > largest.width * largest.height) {
                largest = d;
            }
        }
        return largest;
    }

    public synchronized void stop() {
        synchronized (lock) {
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
    }
    
    /**
     * Force release camera even if locked
     */
    public synchronized void forceRelease() {
        synchronized (lock) {
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
    }

    public synchronized BufferedImage captureFrame() {
        synchronized (lock) {
            if (webcam != null && webcam.isOpen()) {
                try {
                    BufferedImage img = webcam.getImage();
                    if (img == null) {
                        System.err.println("⚠ Webcam returned null frame");
                    }
                    return img;
                } catch (Exception e) {
                    System.err.println("✗ Error capturing frame: " + e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    public Dimension getViewSize() {
        if (webcam != null && isOpen) {
            return webcam.getViewSize();
        }
        return selectedResolution != null ? selectedResolution : new Dimension(640, 480);
    }
}

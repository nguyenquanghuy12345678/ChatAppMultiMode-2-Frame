package client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility class for screen capture functionality
 */
public class ScreenCaptureUtil {
    
    /**
     * Capture toàn bộ màn hình
     * @return byte array của ảnh PNG
     */
    public static byte[] captureFullScreen() throws AWTException, IOException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenCapture = robot.createScreenCapture(screenRect);
        return imageToByteArray(screenCapture);
    }
    
    /**
     * Capture một vùng cụ thể của màn hình
     * @param x tọa độ x
     * @param y tọa độ y
     * @param width chiều rộng
     * @param height chiều cao
     * @return byte array của ảnh PNG
     */
    public static byte[] captureScreenArea(int x, int y, int width, int height) 
            throws AWTException, IOException {
        Robot robot = new Robot();
        Rectangle captureRect = new Rectangle(x, y, width, height);
        BufferedImage screenCapture = robot.createScreenCapture(captureRect);
        return imageToByteArray(screenCapture);
    }
    
    /**
     * Capture màn hình và resize để giảm kích thước
     * @param maxWidth chiều rộng tối đa
     * @param maxHeight chiều cao tối đa
     * @return byte array của ảnh PNG đã resize
     */
    public static byte[] captureAndResize(int maxWidth, int maxHeight) 
            throws AWTException, IOException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenCapture = robot.createScreenCapture(screenRect);
        
        // Calculate resize dimensions maintaining aspect ratio
        int originalWidth = screenCapture.getWidth();
        int originalHeight = screenCapture.getHeight();
        
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        // Resize image
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(screenCapture, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return imageToByteArray(resized);
    }
    
    /**
     * Chuyển BufferedImage thành byte array
     */
    private static byte[] imageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
    
    /**
     * Lấy kích thước màn hình
     */
    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}

package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Dialog để preview ảnh và file
 */
public class ImagePreviewDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 46);
    private static final Color PANEL_COLOR = new Color(40, 42, 54);
    private static final Color TEXT_COLOR = new Color(248, 248, 242);
    private static final Color ACCENT_COLOR = new Color(139, 233, 253);
    
    private JLabel imageLabel;
    private JLabel infoLabel;
    private JButton saveButton;
    private JButton closeButton;
    
    private byte[] imageData;
    private String fileName;
    
    public ImagePreviewDialog(Frame parent, String fileName, byte[] imageData) {
        super(parent, "Image Preview - " + fileName, true);
        this.fileName = fileName;
        this.imageData = imageData;
        
        initComponents();
        loadImage();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        
        // Top panel - Info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PANEL_COLOR);
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        infoLabel = new JLabel("File: " + fileName);
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoLabel.setForeground(ACCENT_COLOR);
        topPanel.add(infoLabel, BorderLayout.WEST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center - Image
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBackground(PANEL_COLOR);
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(68, 71, 90), 2));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        saveButton = createStyledButton("Save As...", new Color(80, 250, 123));
        saveButton.addActionListener(e -> saveImage());
        
        closeButton = createStyledButton("Close", new Color(255, 85, 85));
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadImage() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(bais);
            
            if (image != null) {
                // Scale image to fit dialog if too large
                int maxWidth = 750;
                int maxHeight = 450;
                
                int width = image.getWidth();
                int height = image.getHeight();
                
                if (width > maxWidth || height > maxHeight) {
                    double widthRatio = (double) maxWidth / width;
                    double heightRatio = (double) maxHeight / height;
                    double ratio = Math.min(widthRatio, heightRatio);
                    
                    int newWidth = (int) (width * ratio);
                    int newHeight = (int) (height * ratio);
                    
                    Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    imageLabel.setIcon(new ImageIcon(image));
                }
                
                infoLabel.setText(String.format("File: %s | Size: %dx%d | %.2f KB", 
                    fileName, width, height, imageData.length / 1024.0));
            } else {
                imageLabel.setText("Cannot display image");
                imageLabel.setForeground(TEXT_COLOR);
            }
            
        } catch (IOException e) {
            imageLabel.setText("Error loading image: " + e.getMessage());
            imageLabel.setForeground(new Color(255, 85, 85));
        }
    }
    
    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File saveFile = fileChooser.getSelectedFile();
                java.nio.file.Files.write(saveFile.toPath(), imageData);
                JOptionPane.showMessageDialog(this, 
                    "Image saved successfully to:\n" + saveFile.getAbsolutePath(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving image: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        
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
    
    /**
     * Hiển thị preview dialog cho ảnh
     */
    public static void showImagePreview(Frame parent, String fileName, byte[] imageData) {
        SwingUtilities.invokeLater(() -> {
            ImagePreviewDialog dialog = new ImagePreviewDialog(parent, fileName, imageData);
            dialog.setVisible(true);
        });
    }
    
    /**
     * Kiểm tra file có phải là ảnh không
     */
    public static boolean isImageFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || 
               lower.endsWith(".jpeg") || lower.endsWith(".gif") || 
               lower.endsWith(".bmp");
    }
}

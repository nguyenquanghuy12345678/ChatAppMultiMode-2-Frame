package common;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading and managing icons throughout the application
 */
public class IconManager {
    private static final String ICON_PATH = "resources/icons/";
    
    // List of 50 emoji icon files
    private static final String[] EMOJI_ICONS = {
        "emoji_smile.png", "emoji_laugh.png", "emoji_wink.png", "emoji_love.png", "emoji_heart.png",
        "emoji_kiss.png", "emoji_thinking.png", "emoji_cool.png", "emoji_star.png", "emoji_sad.png",
        "emoji_cry.png", "emoji_angry.png", "emoji_surprised.png", "emoji_sleepy.png", "emoji_sick.png",
        "emoji_party.png", "emoji_celebrate.png", "emoji_fire.png", "emoji_clap.png", "emoji_thumbsup.png",
        "emoji_thumbsdown.png", "emoji_ok.png", "emoji_peace.png", "emoji_muscle.png", "emoji_pray.png",
        "emoji_sun.png", "emoji_moon.png", "emoji_star2.png", "emoji_cloud.png", "emoji_rain.png",
        "emoji_snow.png", "emoji_thunder.png", "emoji_rainbow.png", "emoji_flower.png", "emoji_tree.png",
        "emoji_cat.png", "emoji_dog.png", "emoji_bird.png", "emoji_fish.png", "emoji_butterfly.png",
        "emoji_pizza.png", "emoji_cake.png", "emoji_coffee.png", "emoji_beer.png", "emoji_fruit.png",
        "emoji_check.png", "emoji_cross.png", "emoji_warning.png", "emoji_info.png", "emoji_question.png"
    };
    
    /**
     * Get list of all available emoji icons
     * @return Array of emoji icon file names
     */
    public static String[] getEmojiIcons() {
        return EMOJI_ICONS;
    }
    
    /**
     * Get list of emoji icons that actually exist in resources folder
     * @return List of existing emoji icon file names
     */
    public static List<String> getAvailableEmojiIcons() {
        List<String> available = new ArrayList<>();
        for (String emojiIcon : EMOJI_ICONS) {
            File file = new File(ICON_PATH + emojiIcon);
            if (file.exists()) {
                available.add(emojiIcon);
            }
        }
        return available;
    }
    
    /**
     * Load an icon from the resources folder
     * @param iconName The name of the icon file (e.g., "emoji_smile.png")
     * @param size The size to scale the icon to
     * @return ImageIcon scaled to the specified size, or null if not found
     */
    public static ImageIcon loadIcon(String iconName, int size) {
        try {
            ImageIcon icon = new ImageIcon(ICON_PATH + iconName);
            if (icon.getIconWidth() == -1) {
                // Icon not found, return null
                return null;
            }
            Image image = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + iconName);
            return null;
        }
    }
    
    /**
     * Load an icon without scaling
     * @param iconName The name of the icon file
     * @return ImageIcon in original size, or null if not found
     */
    public static ImageIcon loadIcon(String iconName) {
        try {
            ImageIcon icon = new ImageIcon(ICON_PATH + iconName);
            if (icon.getIconWidth() == -1) {
                return null;
            }
            return icon;
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + iconName);
            return null;
        }
    }
    
    /**
     * Create a JLabel with icon and text
     * @param iconName The icon file name
     * @param text The text to display
     * @param iconSize The size of the icon
     * @return JLabel with icon and text
     */
    public static JLabel createIconLabel(String iconName, String text, int iconSize) {
        JLabel label = new JLabel(text);
        ImageIcon icon = loadIcon(iconName, iconSize);
        if (icon != null) {
            label.setIcon(icon);
        }
        return label;
    }
    
    /**
     * Create a JButton with icon and text
     * @param iconName The icon file name
     * @param text The button text
     * @param iconSize The size of the icon
     * @return JButton with icon and text
     */
    public static JButton createIconButton(String iconName, String text, int iconSize) {
        JButton button = new JButton(text);
        ImageIcon icon = loadIcon(iconName, iconSize);
        if (icon != null) {
            button.setIcon(icon);
        }
        return button;
    }
    
    /**
     * Set icon for an existing JLabel
     * @param label The JLabel to set icon for
     * @param iconName The icon file name
     * @param iconSize The size of the icon
     */
    public static void setLabelIcon(JLabel label, String iconName, int iconSize) {
        ImageIcon icon = loadIcon(iconName, iconSize);
        if (icon != null) {
            label.setIcon(icon);
            label.setText(""); // Remove text when icon is set
        }
    }
    
    /**
     * Get emoji icon code for inserting into messages
     * @param iconName The emoji icon file name
     * @return Code like [:emoji_smile:]
     */
    public static String getEmojiCode(String iconName) {
        return "[:" + iconName.replace(".png", "") + ":]";
    }
}

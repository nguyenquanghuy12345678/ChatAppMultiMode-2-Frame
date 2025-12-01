package client;

import common.IconManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom JTextPane that can display emoji icons inline with text
 */
public class EmojiTextPane extends JTextPane {
    private static final long serialVersionUID = 1L;
    private static final Pattern EMOJI_PATTERN = Pattern.compile("\\[:([^\\]]+):\\]");
    
    public EmojiTextPane() {
        super();
        setEditable(false);
    }
    
    /**
     * Append text with emoji icons
     * @param text Text containing emoji codes like [:emoji_smile:]
     */
    public void appendText(String text) {
        StyledDocument doc = getStyledDocument();
        
        try {
            // Parse text and find emoji codes
            Matcher matcher = EMOJI_PATTERN.matcher(text);
            int lastIndex = 0;
            
            while (matcher.find()) {
                // Add text before emoji
                String beforeText = text.substring(lastIndex, matcher.start());
                if (!beforeText.isEmpty()) {
                    doc.insertString(doc.getLength(), beforeText, null);
                }
                
                // Add emoji icon
                String emojiName = matcher.group(1) + ".png";
                ImageIcon icon = IconManager.loadIcon(emojiName, 20);
                if (icon != null) {
                    Style style = doc.addStyle("icon", null);
                    StyleConstants.setIcon(style, icon);
                    doc.insertString(doc.getLength(), " ", style);
                } else {
                    // Icon not found, just show the code
                    doc.insertString(doc.getLength(), matcher.group(0), null);
                }
                
                lastIndex = matcher.end();
            }
            
            // Add remaining text
            if (lastIndex < text.length()) {
                String remainingText = text.substring(lastIndex);
                doc.insertString(doc.getLength(), remainingText, null);
            }
            
            // Add newline
            doc.insertString(doc.getLength(), "\n", null);
            
            // Scroll to bottom
            setCaretPosition(doc.getLength());
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    
 // --- THÊM HÀM MỚI NÀY ---
    public void insertImage(ImageIcon image, String description) {
        if (image == null) return;
        StyledDocument doc = getStyledDocument();
        try {
            // Chèn text mô tả trước
            if (description != null && !description.isEmpty()) {
                doc.insertString(doc.getLength(), description + "\n", null);
            }
            
            // Chèn ảnh
            Style style = doc.addStyle("image", null);
            StyleConstants.setIcon(style, image);
            doc.insertString(doc.getLength(), " ", style);
            doc.insertString(doc.getLength(), "\n", null); // Xuống dòng sau ảnh
            
            setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
 // --- THÊM HÀM MỚI ĐỂ CHÈN NÚT BẤM (FILE) ---
    public void insertComponent(Component component, String description) {
        StyledDocument doc = getStyledDocument();
        try {
            // Chèn mô tả (VD: You sent a file...)
            if (description != null && !description.isEmpty()) {
                doc.insertString(doc.getLength(), description + "\n", null);
            }
            
            // Chèn Component (Nút bấm)
            setCaretPosition(doc.getLength());
            insertComponent(component);
            
            // Xuống dòng
            doc.insertString(doc.getLength(), "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Clear all text
     */
    public void clearText() {
        setText("");
    }
}

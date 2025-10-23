package dev.threadgaugexp.util;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class XPStyleManager {
    
    // Windows XP color palette
    public static final Color XP_BLUE = new Color(0, 78, 152);
    public static final Color XP_LIGHT_BLUE = new Color(167, 199, 255);
    public static final Color XP_VERY_LIGHT_BLUE = new Color(215, 231, 255);
    public static final Color XP_BUTTON_BLUE = new Color(236, 243, 255);
    public static final Color XP_BORDER = new Color(0, 60, 116);
    
    public static void applyXPStyle() {
        try {
            // Try to use system look and feel as base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Apply Windows XP-inspired colors
            UIManager.put("Panel.background", new ColorUIResource(XP_VERY_LIGHT_BLUE));
            UIManager.put("Button.background", new ColorUIResource(XP_BUTTON_BLUE));
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Button.border", BorderFactory.createLineBorder(XP_BORDER));
            
            // Text components
            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("TextArea.foreground", Color.BLACK);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.BLACK);
            
            // Labels
            UIManager.put("Label.foreground", Color.BLACK);
            
            // Progress bars
            UIManager.put("ProgressBar.foreground", new ColorUIResource(new Color(0, 150, 0)));
            UIManager.put("ProgressBar.background", Color.WHITE);
            
            // Scroll panes
            UIManager.put("ScrollPane.background", Color.WHITE);
            
            // Set default font to Tahoma (XP default)
            Font tahomaFont = new Font("Tahoma", Font.PLAIN, 11);
            if (tahomaFont.getFamily().equals("Tahoma")) {
                setUIFont(tahomaFont);
            }
            
        } catch (Exception e) {
            // If system L&F fails, continue with default
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
    }
    
    private static void setUIFont(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}

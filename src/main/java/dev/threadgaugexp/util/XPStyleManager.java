package dev.threadgaugexp.util;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class XPStyleManager {
    private static final Color ORANGE = new Color(255, 140, 0);
    private static final Color DARK_GRAY_BG = new Color(45, 45, 45);
    private static final Color DARK_GRAY_PANEL = new Color(60, 60, 60);
    private static final Color DARK_TEXT = Color.WHITE;
    private static final Color DARK_BUTTON_BG = new Color(0x33, 0x33, 0x33); // #333333 for dark-mode buttons

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
            boolean dark = isDarkMode();

            if (dark) {
                // Dark theme
                UIManager.put("Panel.background", new ColorUIResource(DARK_GRAY_PANEL));
                UIManager.put("Button.background", new ColorUIResource(DARK_BUTTON_BG));
                UIManager.put("Button.foreground", DARK_TEXT);
                UIManager.put("Button.disabledText", new ColorUIResource(new Color(170, 170, 170)));

                UIManager.put("TextArea.background", new ColorUIResource(DARK_GRAY_BG));
                UIManager.put("TextArea.foreground", DARK_TEXT);
                UIManager.put("TextField.background", new ColorUIResource(DARK_GRAY_BG));
                UIManager.put("TextField.foreground", DARK_TEXT);
                UIManager.put("TextField.caretForeground", DARK_TEXT);
                UIManager.put("TextArea.caretForeground", DARK_TEXT);
                UIManager.put("FormattedTextField.background", new ColorUIResource(DARK_GRAY_BG));
                UIManager.put("FormattedTextField.foreground", DARK_TEXT);
                UIManager.put("PasswordField.background", new ColorUIResource(DARK_GRAY_BG));
                UIManager.put("PasswordField.foreground", DARK_TEXT);
                UIManager.put("Spinner.background", new ColorUIResource(DARK_GRAY_BG));
                UIManager.put("Spinner.foreground", DARK_TEXT);
                UIManager.put("ComboBox.background", new ColorUIResource(DARK_GRAY_BG));
                UIManager.put("ComboBox.foreground", DARK_TEXT);
                UIManager.put("ComboBox.selectionBackground", new ColorUIResource(ORANGE));
                UIManager.put("ComboBox.selectionForeground", DARK_TEXT);
                UIManager.put("Label.foreground", DARK_TEXT);

                UIManager.put("ProgressBar.foreground", new ColorUIResource(new Color(255, 165, 0))); // orange-ish
                UIManager.put("ProgressBar.background", new ColorUIResource(DARK_GRAY_BG));
                UIManager.put("ScrollPane.background", new ColorUIResource(DARK_GRAY_BG));
            } else {
                // XP-like light theme
                UIManager.put("Panel.background", new ColorUIResource(XP_VERY_LIGHT_BLUE));
                UIManager.put("Button.background", new ColorUIResource(XP_BUTTON_BLUE));
                UIManager.put("Button.foreground", Color.BLACK);
                UIManager.put("Button.border", BorderFactory.createLineBorder(XP_BORDER));
                UIManager.put("Button.disabledText", new ColorUIResource(new Color(120, 120, 120)));

                UIManager.put("TextArea.background", Color.WHITE);
                UIManager.put("TextArea.foreground", Color.BLACK);
                UIManager.put("TextField.background", Color.WHITE);
                UIManager.put("TextField.foreground", Color.BLACK);
                UIManager.put("TextField.caretForeground", Color.BLACK);
                UIManager.put("TextArea.caretForeground", Color.BLACK);
                UIManager.put("FormattedTextField.background", Color.WHITE);
                UIManager.put("FormattedTextField.foreground", Color.BLACK);
                UIManager.put("PasswordField.background", Color.WHITE);
                UIManager.put("PasswordField.foreground", Color.BLACK);
                UIManager.put("Spinner.background", Color.WHITE);
                UIManager.put("Spinner.foreground", Color.BLACK);
                UIManager.put("ComboBox.background", Color.WHITE);
                UIManager.put("ComboBox.foreground", Color.BLACK);
                UIManager.put("ComboBox.selectionBackground", new ColorUIResource(new Color(0, 120, 215)));
                UIManager.put("ComboBox.selectionForeground", Color.WHITE);
                UIManager.put("Label.foreground", Color.BLACK);

                UIManager.put("ProgressBar.foreground", new ColorUIResource(new Color(0, 150, 0)));
                UIManager.put("ProgressBar.background", Color.WHITE);
                UIManager.put("ScrollPane.background", Color.WHITE);
            }
            
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

    public static boolean isDarkMode() {
        return Boolean.parseBoolean(System.getProperty("tgxp.darkMode", "false"));
    }

    public static Color getPanelBackground() {
        return isDarkMode() ? DARK_GRAY_PANEL : XP_VERY_LIGHT_BLUE;
    }

    public static Color getBorderColor() {
        return isDarkMode() ? ORANGE : XP_BORDER;
    }

    public static Color getTitleColor() {
        return isDarkMode() ? ORANGE : new Color(0, 51, 153);
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

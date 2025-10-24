package dev.threadgaugexp.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OutputPanel extends JPanel {
    private JTextArea outputArea;
    private SimpleDateFormat timeFormat;

    public OutputPanel() {
        timeFormat = new SimpleDateFormat("HH:mm:ss");
        initializeUI();
    }

    private void initializeUI() {
    setLayout(new BorderLayout());
    setBorder(createXPBorder("Output Log"));
    setBackground(dev.threadgaugexp.util.XPStyleManager.getPanelBackground());

    outputArea = new JTextArea();
        outputArea.setEditable(false);
    outputArea.setFont(new Font("Consolas", Font.PLAIN, 11));
    // Use UI defaults to allow theme to control colors
    outputArea.setBackground(UIManager.getColor("TextArea.background"));
    outputArea.setForeground(UIManager.getColor("TextArea.foreground"));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // Clear button
        JButton clearButton = new JButton("Clear Log");
        clearButton.setFont(new Font("Tahoma", Font.PLAIN, 10));
        clearButton.setBackground(UIManager.getColor("Button.background"));
        clearButton.setForeground(UIManager.getColor("Button.foreground"));
        if (dev.threadgaugexp.util.XPStyleManager.isDarkMode()) {
            clearButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        }
        clearButton.setOpaque(true);
        clearButton.setContentAreaFilled(true);
        clearButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dev.threadgaugexp.util.XPStyleManager.getBorderColor()),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        clearButton.addActionListener(e -> clear());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(dev.threadgaugexp.util.XPStyleManager.getPanelBackground());
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);

        log("ThreadGauge XP initialized. Ready to test thread behavior.");
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            outputArea.append("[" + timestamp + "] " + message + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    public void logError(String message) {
        log("ERROR: " + message);
    }

    public void logWarning(String message) {
        log("WARNING: " + message);
    }

    public void logSuccess(String message) {
        log("SUCCESS: " + message);
    }

    public void clear() {
        outputArea.setText("");
        log("Log cleared.");
    }

    public String getLogText() {
        return outputArea.getText();
    }

    private TitledBorder createXPBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(dev.threadgaugexp.util.XPStyleManager.getBorderColor(), 2),
            title
        );
        border.setTitleFont(new Font("Tahoma", Font.BOLD, 11));
        border.setTitleColor(dev.threadgaugexp.util.XPStyleManager.getTitleColor());
        return border;
    }
}

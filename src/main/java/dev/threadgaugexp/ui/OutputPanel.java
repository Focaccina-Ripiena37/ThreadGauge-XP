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
        setBackground(new Color(215, 231, 255));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        outputArea.setBackground(Color.WHITE);
        outputArea.setForeground(Color.BLACK);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // Clear button
        JButton clearButton = new JButton("Clear Log");
        clearButton.setFont(new Font("Tahoma", Font.PLAIN, 10));
        clearButton.addActionListener(e -> clear());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
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
            BorderFactory.createLineBorder(new Color(167, 199, 255), 2),
            title
        );
        border.setTitleFont(new Font("Tahoma", Font.BOLD, 11));
        border.setTitleColor(new Color(0, 51, 153));
        return border;
    }
}

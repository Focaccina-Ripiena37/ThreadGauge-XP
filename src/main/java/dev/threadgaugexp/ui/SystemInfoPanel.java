package dev.threadgaugexp.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.management.ManagementFactory;

public class SystemInfoPanel extends JPanel {
    private JLabel osLabel;
    private JLabel jvmLabel;
    private JLabel coresLabel;
    private JLabel totalMemLabel;
    private JLabel freeMemLabel;

    public SystemInfoPanel() {
        initializeUI();
        updateSystemInfo();
    }

    private void initializeUI() {
        setLayout(new GridLayout(1, 5, 10, 5));
        setBorder(createXPBorder("System Information"));
        setBackground(new Color(215, 231, 255)); // XP light blue

        osLabel = createInfoLabel();
        jvmLabel = createInfoLabel();
        coresLabel = createInfoLabel();
        totalMemLabel = createInfoLabel();
        freeMemLabel = createInfoLabel();

        add(createLabelPanel("OS:", osLabel));
        add(createLabelPanel("JVM:", jvmLabel));
        add(createLabelPanel("Cores:", coresLabel));
        add(createLabelPanel("Total RAM:", totalMemLabel));
        add(createLabelPanel("Free RAM:", freeMemLabel));
    }

    private JLabel createInfoLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Tahoma", Font.BOLD, 11));
        return label;
    }

    private JPanel createLabelPanel(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        titleLabel.setForeground(Color.DARK_GRAY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }

    private void updateSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        
        osLabel.setText(System.getProperty("os.name"));
        jvmLabel.setText("Java " + System.getProperty("java.version"));
        coresLabel.setText(String.valueOf(runtime.availableProcessors()));
        
        long totalMem = runtime.maxMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        
        totalMemLabel.setText(totalMem + " MB");
        freeMemLabel.setText(freeMem + " MB");
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

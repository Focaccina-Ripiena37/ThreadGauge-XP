package dev.threadgaugexp.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class TelemetryPanel extends JPanel {
    private JLabel activeThreadsLabel;
    private JLabel heapUsedLabel;
    private JLabel heapMaxLabel;
    private JLabel cpuLoadLabel;
    private JProgressBar heapProgressBar;
    private JProgressBar cpuProgressBar;
    private Timer updateTimer;

    public TelemetryPanel() {
        initializeUI();
        startTelemetry();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBorder(createXPBorder("Live Telemetry"));
        setBackground(new Color(215, 231, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        int row = 0;

        // Active threads
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Active Threads:"), gbc);
        gbc.gridx = 1;
        activeThreadsLabel = createValueLabel();
        add(activeThreadsLabel, gbc);
        row++;

        // Heap used
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Heap Used:"), gbc);
        gbc.gridx = 1;
        heapUsedLabel = createValueLabel();
        add(heapUsedLabel, gbc);
        row++;

        // Heap max
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Heap Max:"), gbc);
        gbc.gridx = 1;
        heapMaxLabel = createValueLabel();
        add(heapMaxLabel, gbc);
        row++;

        // Heap progress bar
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        heapProgressBar = new JProgressBar(0, 100);
        heapProgressBar.setStringPainted(true);
        heapProgressBar.setString("Heap: 0%");
        add(heapProgressBar, gbc);
        row++;

        // CPU load
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("CPU Load:"), gbc);
        gbc.gridx = 1;
        cpuLoadLabel = createValueLabel();
        add(cpuLoadLabel, gbc);
        row++;

        // CPU progress bar
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        cpuProgressBar = new JProgressBar(0, 100);
        cpuProgressBar.setStringPainted(true);
        cpuProgressBar.setString("CPU: 0%");
        add(cpuProgressBar, gbc);
        row++;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Tahoma", Font.BOLD, 11));
        return label;
    }

    private void startTelemetry() {
        updateTimer = new Timer(500, e -> updateTelemetry());
        updateTimer.start();
    }

    private void updateTelemetry() {
        // Active threads
        int threadCount = Thread.activeCount();
        activeThreadsLabel.setText(String.valueOf(threadCount));

        // Heap memory
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        
        heapUsedLabel.setText(usedMemory + " MB");
        heapMaxLabel.setText(maxMemory + " MB");
        
        int heapPercent = (int) ((usedMemory * 100) / maxMemory);
        heapProgressBar.setValue(heapPercent);
        heapProgressBar.setString("Heap: " + heapPercent + "%");
        
        // Set color based on usage
        if (heapPercent > 80) {
            heapProgressBar.setForeground(new Color(200, 0, 0));
        } else if (heapPercent > 60) {
            heapProgressBar.setForeground(new Color(200, 150, 0));
        } else {
            heapProgressBar.setForeground(new Color(0, 150, 0));
        }

        // CPU load
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = -1;
            
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                cpuLoad = sunOsBean.getCpuLoad();
            }
            
            if (cpuLoad >= 0) {
                int cpuPercent = (int) (cpuLoad * 100);
                cpuLoadLabel.setText(cpuPercent + "%");
                cpuProgressBar.setValue(cpuPercent);
                cpuProgressBar.setString("CPU: " + cpuPercent + "%");
                
                if (cpuPercent > 80) {
                    cpuProgressBar.setForeground(new Color(200, 0, 0));
                } else if (cpuPercent > 60) {
                    cpuProgressBar.setForeground(new Color(200, 150, 0));
                } else {
                    cpuProgressBar.setForeground(new Color(0, 150, 0));
                }
            } else {
                cpuLoadLabel.setText("N/A");
            }
        } catch (Exception ex) {
            cpuLoadLabel.setText("N/A");
        }
    }

    public void stopTelemetry() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
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

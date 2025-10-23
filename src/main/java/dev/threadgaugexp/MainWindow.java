package dev.threadgaugexp;

import dev.threadgaugexp.ui.*;
import dev.threadgaugexp.util.XPStyleManager;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private SystemInfoPanel systemInfoPanel;
    private ControlsPanel controlsPanel;
    private TelemetryPanel telemetryPanel;
    private OutputPanel outputPanel;
    private JLabel statusLabel;

    public MainWindow() {
        super("ThreadGauge XP - Thread Behavior Explorer");
        initializeUI();
    }

    private void initializeUI() {
        // Apply XP style
        XPStyleManager.applyXPStyle();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout(10, 10));

        // Top panel - System Info
        systemInfoPanel = new SystemInfoPanel();
        add(systemInfoPanel, BorderLayout.NORTH);

        // Center panel - split between controls/telemetry and output
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Top of center - Controls and Telemetry side by side
        JPanel topCenterPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        controlsPanel = new ControlsPanel(this);
        telemetryPanel = new TelemetryPanel();
        topCenterPanel.add(controlsPanel);
        topCenterPanel.add(telemetryPanel);
        
        centerPanel.add(topCenterPanel, BorderLayout.NORTH);

        // Bottom of center - Output log
        outputPanel = new OutputPanel();
        centerPanel.add(outputPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        add(statusLabel, BorderLayout.SOUTH);

        // Add padding
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public OutputPanel getOutputPanel() {
        return outputPanel;
    }

    public TelemetryPanel getTelemetryPanel() {
        return telemetryPanel;
    }

    public ControlsPanel getControlsPanel() {
        return controlsPanel;
    }

    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(" " + status));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}

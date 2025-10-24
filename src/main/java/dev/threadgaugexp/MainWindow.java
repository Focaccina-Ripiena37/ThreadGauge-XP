package dev.threadgaugexp;

import dev.threadgaugexp.ui.*;
import dev.threadgaugexp.util.XPStyleManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

        // Set application icon (if provided under resources/icons/ico.png)
        try {
            List<Image> icons = loadIcons();
            if (!icons.isEmpty()) {
                setIconImages(icons);
            }
        } catch (Exception ignore) {
            // Icon is optional; continue without blocking the UI
        }

    // Tooltips appear only after hovering > 1.5 seconds (1500ms)
    ToolTipManager.sharedInstance().setInitialDelay(1500);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(XPStyleManager.getPanelBackground());

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
        statusLabel.setOpaque(true);
        statusLabel.setBackground(XPStyleManager.getPanelBackground());
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
        add(statusLabel, BorderLayout.SOUTH);

        // Add padding
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ensure background timers/workers stop cleanly on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (telemetryPanel != null) telemetryPanel.stopTelemetry();
            }
        });
    }

    private List<Image> loadIcons() throws IOException {
        List<Image> images = new ArrayList<>();
        URL url = getClass().getResource("/icons/ico.png");
        if (url == null) {
            return images; // icon not bundled
        }
        BufferedImage base = javax.imageio.ImageIO.read(url);
        if (base == null) return images;

        int[] sizes = new int[]{16, 20, 24, 32, 40, 48, 64, 128, 256};
        for (int s : sizes) {
            Image scaled = base.getScaledInstance(s, s, Image.SCALE_SMOOTH);
            images.add(scaled);
        }
        return images;
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

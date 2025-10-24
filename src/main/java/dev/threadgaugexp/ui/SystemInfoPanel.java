package dev.threadgaugexp.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class SystemInfoPanel extends JPanel {
    private JLabel osLabel;
    private JLabel jvmLabel;
    private JLabel systemJavaLabel;
    private JLabel coresLabel;
    private JLabel totalMemLabel;
    private JLabel freeMemLabel;

    public SystemInfoPanel() {
        initializeUI();
        updateSystemInfo();
    }

    private void initializeUI() {
    setLayout(new GridLayout(1, 6, 10, 5));
    setBorder(createXPBorder("System Information"));
    setBackground(dev.threadgaugexp.util.XPStyleManager.getPanelBackground());

        osLabel = createInfoLabel();
    jvmLabel = createInfoLabel();
        coresLabel = createInfoLabel();
        totalMemLabel = createInfoLabel();
        freeMemLabel = createInfoLabel();
    systemJavaLabel = createInfoLabel();

        add(createLabelPanel("OS:", osLabel));
    add(createLabelPanel("JVM (App):", jvmLabel));
        add(createLabelPanel("Cores:", coresLabel));
        add(createLabelPanel("Total RAM:", totalMemLabel));
        add(createLabelPanel("Free RAM:", freeMemLabel));
    add(createLabelPanel("System Java:", systemJavaLabel));
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

    @SuppressWarnings({"deprecation"})
    private void updateSystemInfo() {
        Runtime runtime = Runtime.getRuntime();

    osLabel.setText(System.getProperty("os.name"));
    osLabel.setToolTipText("Sistema operativo rilevato");

        // JVM version + implementation type (HotSpot, OpenJ9, GraalVM)
        String version = System.getProperty("java.runtime.version", System.getProperty("java.version", "?"));
        String vmName = System.getProperty("java.vm.name", "?");
        String vendor = System.getProperty("java.vendor", "?");
        String impl = detectImpl(vmName, vendor);
        jvmLabel.setText(version + " (" + impl + ")");
        jvmLabel.setToolTipText("VM: " + vmName + " | Vendor: " + vendor + " | Note: l'app può usare una JDK diversa da quella di sistema");
    coresLabel.setText(String.valueOf(runtime.availableProcessors()));
    coresLabel.setToolTipText("CPU logiche disponibili alla JVM");

        // Prefer physical system memory via OperatingSystemMXBean
        long totalPhysMB = -1;
        long freePhysMB = -1;
        try {
            java.lang.management.OperatingSystemMXBean base = ManagementFactory.getOperatingSystemMXBean();
            if (base instanceof OperatingSystemMXBean) {
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) base;
                totalPhysMB = osBean.getTotalPhysicalMemorySize() / (1024 * 1024);
                freePhysMB = osBean.getFreePhysicalMemorySize() / (1024 * 1024);
            }
        } catch (Throwable ignored) {
            // Fallback handled below
        }

        if (totalPhysMB >= 0 && freePhysMB >= 0) {
            totalMemLabel.setText(totalPhysMB + " MB");
            freeMemLabel.setText(freePhysMB + " MB");
            totalMemLabel.setToolTipText("Total physical RAM detected by the OS");
            freeMemLabel.setToolTipText("Free physical RAM reported by the OS");
        } else {
            // Fallback to JVM heap info if OS bean is unavailable
            long totalHeapMB = runtime.maxMemory() / (1024 * 1024);
            long freeHeapMB = runtime.freeMemory() / (1024 * 1024);
            totalMemLabel.setText(totalHeapMB + " MB (JVM Heap Max)");
            freeMemLabel.setText(freeHeapMB + " MB (JVM Heap Free)");
            totalMemLabel.setToolTipText("Maximum Java heap available to this JVM (fallback)");
            freeMemLabel.setToolTipText("Currently free memory inside the allocated Java heap (fallback)");
        }

        // Populate System Java (PATH) asynchronously to avoid UI stall
        if (isSystemJavaDetectionEnabled()) {
            systemJavaLabel.setText("...");
            systemJavaLabel.setToolTipText("Versione di 'java -version' nel PATH di sistema (se disponibile). Disattivabile con -Dtgxp.detectSystemJava=false");
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return detectSystemJavaVersion();
                }
                @Override
                protected void done() {
                    try {
                        String v = get();
                        systemJavaLabel.setText(v);
                    } catch (Exception ex) {
                        systemJavaLabel.setText("N/A");
                    }
                }
            }.execute();
        } else {
            systemJavaLabel.setText("Disabled");
            systemJavaLabel.setToolTipText("Rilevamento disattivato: avvia con -Dtgxp.detectSystemJava=true per abilitarlo");
        }
    }

    private static String detectImpl(String vmName, String vendor) {
        String n = (vmName == null ? "" : vmName).toLowerCase();
        String v = (vendor == null ? "" : vendor).toLowerCase();
        if (n.contains("openj9")) return "OpenJ9";
        if (n.contains("graal") || v.contains("graal")) return "GraalVM";
        if (n.contains("hotspot") || n.contains("openjdk") || v.contains("oracle") || v.contains("adoptium") || v.contains("temurin")) return "HotSpot";
        return vmName != null && !vmName.isEmpty() ? vmName : "Unknown";
    }

    private static String detectSystemJavaVersion() {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line = r.readLine();
            p.waitFor(1500, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (line == null) line = "";
            // Typical outputs: 'java version "25" ...' or 'openjdk version "21.0.3" ...'
            return line.trim().isEmpty() ? "N/A" : line.trim();
        } catch (Exception e) {
            return "N/A";
        }
    }

    private static boolean isSystemJavaDetectionEnabled() {
        String prop = System.getProperty("tgxp.detectSystemJava", "true");
        return "true".equalsIgnoreCase(prop);
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

package dev.threadgaugexp.ui;

import dev.threadgaugexp.MainWindow;
import dev.threadgaugexp.core.StressTest;
import dev.threadgaugexp.core.ThreadTester;
import dev.threadgaugexp.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ControlsPanel extends JPanel {
    private MainWindow mainWindow;
    private JButton maxThreadsButton;
    private JButton stopButton;
    private JButton stressTestButton;
    private JButton exportButton;
    private JButton restartNoJavaDetectButton;
    private JButton restartDarkModeButton;
    private JSpinner stackSizeSpinner;
    private JSpinner stressThreadsSpinner;
    private JSpinner stressDurationSpinner;
    private JProgressBar progressBar;
    private JLabel animationLabel;
    
    private ThreadTester threadTester;
    private StressTest stressTest;
    private boolean testRunning = false;

    public ControlsPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initializeUI();
    }

    private void initializeUI() {
    setLayout(new BorderLayout(5, 5));
    setBorder(createXPBorder("Controls"));
    setBackground(dev.threadgaugexp.util.XPStyleManager.getPanelBackground());

    JPanel innerPanel = new JPanel(new GridBagLayout());
    innerPanel.setOpaque(true);
    innerPanel.setBackground(dev.threadgaugexp.util.XPStyleManager.getPanelBackground());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        int row = 0;

    // Stack size configuration
        gbc.gridx = 0; gbc.gridy = row;
    JLabel stackSizeLabel = new JLabel("Stack Size (KB):");
    stackSizeLabel.setToolTipText("Dimensione dello stack per ogni thread. 0 = predefinito JVM. Su Windows, 512 KB esplicito può essere meno stabile per molti thread.");
    innerPanel.add(stackSizeLabel, gbc);
        gbc.gridx = 1;
    stackSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 128, 8192, 128));
    stackSizeSpinner.setToolTipText("Imposta la dimensione dello stack per i thread del test massimo.");
    styleSpinner(stackSizeSpinner);
        innerPanel.add(stackSizeSpinner, gbc);
        row++;

        // Max threads button
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
    maxThreadsButton = createXPButton("Find Max Threads");
    maxThreadsButton.setToolTipText("Trova il numero massimo di thread creabili in sicurezza (con limiti e soglie di heap). Esegue in background.");
        maxThreadsButton.addActionListener(e -> startMaxThreadsTest());
        innerPanel.add(maxThreadsButton, gbc);
        row++;

        // Separator
        gbc.gridy = row;
        innerPanel.add(new JSeparator(), gbc);
        row++;

        // Stress test configuration
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
    JLabel stressThreadsLabel = new JLabel("Stress Threads:");
    stressThreadsLabel.setToolTipText("Numero di thread lavoratori da avviare per lo stress test.");
    innerPanel.add(stressThreadsLabel, gbc);
        gbc.gridx = 1;
    stressThreadsSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 50));
    stressThreadsSpinner.setToolTipText("Numero di thread nello stress test.");
    styleSpinner(stressThreadsSpinner);
        innerPanel.add(stressThreadsSpinner, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
    JLabel durationLabel = new JLabel("Duration (sec):");
    durationLabel.setToolTipText("Durata dello stress test in secondi.");
    innerPanel.add(durationLabel, gbc);
        gbc.gridx = 1;
    stressDurationSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 60, 5));
    stressDurationSpinner.setToolTipText("Secondi di esecuzione dello stress test.");
    styleSpinner(stressDurationSpinner);
        innerPanel.add(stressDurationSpinner, gbc);
        row++;

        // Stress test button
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
    stressTestButton = createXPButton("Run Stress Test");
    stressTestButton.setToolTipText("Avvia uno stress test con i parametri scelti (thread e durata).");
        stressTestButton.addActionListener(e -> startStressTest());
        innerPanel.add(stressTestButton, gbc);
        row++;

        // Stop button
        gbc.gridy = row;
        stopButton = createXPButton("Stop Test");
        stopButton.setToolTipText("Ferma il test in esecuzione (interrompe e pulisce i thread in background).");
        stopButton.setEnabled(false);
        // Solo in light mode usiamo un rosato per enfatizzare lo stop; in dark lasciamo il grigio scuro del tema
        if (!dev.threadgaugexp.util.XPStyleManager.isDarkMode()) {
            stopButton.setBackground(new Color(255, 200, 200));
            stopButton.setForeground(Color.BLACK);
        }
        stopButton.addActionListener(e -> stopCurrentTest());
        innerPanel.add(stopButton, gbc);
        row++;

        // Separator
        gbc.gridy = row;
        innerPanel.add(new JSeparator(), gbc);
        row++;

        // Export button
        gbc.gridy = row;
    exportButton = createXPButton("Export Results");
    exportButton.setToolTipText("Esporta risultati e log in file (TXT/CSV).");
        exportButton.addActionListener(e -> exportResults());
        innerPanel.add(exportButton, gbc);
        row++;

    // App Controls section (distinct from test controls)
    gbc.gridy = row; gbc.gridwidth = 2;
    JPanel appPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    appPanel.setOpaque(true);
    appPanel.setBackground(dev.threadgaugexp.util.XPStyleManager.getPanelBackground());
    appPanel.setBorder(createXPBorder("App Controls"));

    restartNoJavaDetectButton = createXPButton("Restart (No System Java)");
    restartNoJavaDetectButton.setToolTipText("Riavvia l'app con -Dtgxp.detectSystemJava=false");
    appPanel.add(restartNoJavaDetectButton);

    boolean dark = dev.threadgaugexp.util.XPStyleManager.isDarkMode();
    restartDarkModeButton = createXPButton(dark ? "Restart (Light Mode)" : "Restart (Dark Mode)");
    restartDarkModeButton.setToolTipText("Riavvia l'app nella modalità di visualizzazione selezionata");
    appPanel.add(restartDarkModeButton);

    // Wire actions after placement
    restartNoJavaDetectButton.addActionListener(e -> restartApp(false, null));
    restartDarkModeButton.addActionListener(e -> restartApp(true, !dark));

    innerPanel.add(appPanel, gbc);
    row++;

        // Progress bar and animation
        gbc.gridy = row;
    progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
    progressBar.setToolTipText("Stato del test in corso.");
        innerPanel.add(progressBar, gbc);
        row++;

        gbc.gridy = row;
    animationLabel = new JLabel("", SwingConstants.CENTER);
        animationLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
    animationLabel.setToolTipText("Indicatore semplice di attività.");
        innerPanel.add(animationLabel, gbc);

        add(innerPanel, BorderLayout.NORTH);
    }

    private JButton createXPButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Tahoma", Font.PLAIN, 11));
        button.setBackground(UIManager.getColor("Button.background"));
        button.setForeground(UIManager.getColor("Button.foreground"));
        if (dev.threadgaugexp.util.XPStyleManager.isDarkMode()) {
            // Forza una UI basic piatta che rispetta i colori impostati anche da disabilitato
            button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        }
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dev.threadgaugexp.util.XPStyleManager.getBorderColor()),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setOpaque(true);
        spinner.setBackground(UIManager.getColor("Spinner.background"));
        spinner.setForeground(UIManager.getColor("Spinner.foreground"));
        // Also style the editor's text field so entered values adapt to theme
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            if (tf != null) {
                tf.setBackground(UIManager.getColor("TextField.background"));
                tf.setForeground(UIManager.getColor("TextField.foreground"));
                Color caret = UIManager.getColor("TextField.caretForeground");
                if (caret != null) {
                    tf.setCaretColor(caret);
                }
            }
        }
    }

    private void startMaxThreadsTest() {
        if (testRunning) return;
        
        testRunning = true;
        setControlsEnabled(false);
        showProgress("Finding maximum threads...");
        
        int stackSize = (Integer) stackSizeSpinner.getValue();
        threadTester = new ThreadTester(mainWindow, stackSize);
        threadTester.execute();
    }

    private void startStressTest() {
        if (testRunning) return;
        
        testRunning = true;
        setControlsEnabled(false);
        showProgress("Running stress test...");
        
        int threads = (Integer) stressThreadsSpinner.getValue();
        int duration = (Integer) stressDurationSpinner.getValue();
        
        stressTest = new StressTest(mainWindow, threads, duration);
        stressTest.execute();
    }

    private void stopCurrentTest() {
        if (threadTester != null) {
            threadTester.cancel(true);
        }
        if (stressTest != null) {
            stressTest.cancel(true);
        }
        testCompleted();
    }

    private void exportResults() {
        ExportUtil.exportResults(mainWindow);
    }

    public void testCompleted() {
        testRunning = false;
        setControlsEnabled(true);
        hideProgress();
        threadTester = null;
        stressTest = null;
    }

    private void setControlsEnabled(boolean enabled) {
        maxThreadsButton.setEnabled(enabled);
        stressTestButton.setEnabled(enabled);
        exportButton.setEnabled(enabled);
        stackSizeSpinner.setEnabled(enabled);
        stressThreadsSpinner.setEnabled(enabled);
        stressDurationSpinner.setEnabled(enabled);
        stopButton.setEnabled(!enabled);
    }

    private void showProgress(String message) {
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString(message);
        
        // Simple animation
        Timer animationTimer = new Timer(500, null);
        animationTimer.addActionListener(e -> {
            String current = animationLabel.getText();
            if (current.length() >= 3) {
                animationLabel.setText(".");
            } else {
                animationLabel.setText(current + ".");
            }
        });
        animationLabel.putClientProperty("animationTimer", animationTimer);
        animationTimer.start();
    }

    private void hideProgress() {
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
        
        Timer timer = (Timer) animationLabel.getClientProperty("animationTimer");
        if (timer != null) {
            timer.stop();
        }
        animationLabel.setText("");
    }

    private void restartApp(boolean toggleDarkMode, Boolean darkModeTarget) {
        try {
            String javaHome = System.getProperty("java.home");
            String sep = System.getProperty("file.separator");
            String javaBin = javaHome + sep + "bin" + sep + (isWindows() ? "java.exe" : "java");

            java.util.List<String> cmd = new java.util.ArrayList<>();
            cmd.add(javaBin);

            // Preserve existing properties we care about
            String classPath = System.getProperty("java.class.path", ".");
            String detectProp = System.getProperty("tgxp.detectSystemJava", "true");
            String darkProp = System.getProperty("tgxp.darkMode", "false");

            // If this restart is specifically for disabling system java detection
            if (toggleDarkMode) {
                boolean target = (darkModeTarget != null) ? darkModeTarget : !Boolean.parseBoolean(darkProp);
                cmd.add("-Dtgxp.darkMode=" + Boolean.toString(target));
                cmd.add("-Dtgxp.detectSystemJava=" + detectProp);
            } else {
                cmd.add("-Dtgxp.detectSystemJava=false");
                cmd.add("-Dtgxp.darkMode=" + darkProp);
            }

            cmd.add("-cp");
            cmd.add(classPath);
            cmd.add("dev.threadgaugexp.MainWindow");

            new ProcessBuilder(cmd).inheritIO().start();
            // Dispose and exit current instance
            SwingUtilities.getWindowAncestor(this).dispose();
            System.exit(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Restart failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
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

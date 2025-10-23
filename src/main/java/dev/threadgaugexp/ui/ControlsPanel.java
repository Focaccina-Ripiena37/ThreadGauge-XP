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
        setBackground(new Color(215, 231, 255));

        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        int row = 0;

        // Stack size configuration
        gbc.gridx = 0; gbc.gridy = row;
        innerPanel.add(new JLabel("Stack Size (KB):"), gbc);
        gbc.gridx = 1;
        stackSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 128, 8192, 128));
        innerPanel.add(stackSizeSpinner, gbc);
        row++;

        // Max threads button
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        maxThreadsButton = createXPButton("Find Max Threads");
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
        innerPanel.add(new JLabel("Stress Threads:"), gbc);
        gbc.gridx = 1;
        stressThreadsSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 50));
        innerPanel.add(stressThreadsSpinner, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        innerPanel.add(new JLabel("Duration (sec):"), gbc);
        gbc.gridx = 1;
        stressDurationSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 60, 5));
        innerPanel.add(stressDurationSpinner, gbc);
        row++;

        // Stress test button
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        stressTestButton = createXPButton("Run Stress Test");
        stressTestButton.addActionListener(e -> startStressTest());
        innerPanel.add(stressTestButton, gbc);
        row++;

        // Stop button
        gbc.gridy = row;
        stopButton = createXPButton("Stop Test");
        stopButton.setEnabled(false);
        stopButton.setBackground(new Color(255, 200, 200));
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
        exportButton.addActionListener(e -> exportResults());
        innerPanel.add(exportButton, gbc);
        row++;

        // Progress bar and animation
        gbc.gridy = row;
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        innerPanel.add(progressBar, gbc);
        row++;

        gbc.gridy = row;
        animationLabel = new JLabel("", SwingConstants.CENTER);
        animationLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        innerPanel.add(animationLabel, gbc);

        add(innerPanel, BorderLayout.NORTH);
    }

    private JButton createXPButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Tahoma", Font.PLAIN, 11));
        button.setBackground(new Color(236, 243, 255));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 60, 116)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
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

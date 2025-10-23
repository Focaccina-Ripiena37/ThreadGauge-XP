package dev.threadgaugexp.util;

import dev.threadgaugexp.MainWindow;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportUtil {
    
    public static void exportResults(MainWindow mainWindow) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Test Results");
        
        // Add file filters
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }
            public String getDescription() {
                return "Text Files (*.txt)";
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        int result = fileChooser.showSaveDialog(mainWindow);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String filename = file.getAbsolutePath();
            
            // Determine format from selected filter or extension
            boolean isCSV = filename.toLowerCase().endsWith(".csv") || 
                          fileChooser.getFileFilter().getDescription().contains("CSV");
            
            // Add extension if missing
            if (!filename.toLowerCase().endsWith(".txt") && !filename.toLowerCase().endsWith(".csv")) {
                filename += isCSV ? ".csv" : ".txt";
                file = new java.io.File(filename);
            }
            
            try {
                if (isCSV) {
                    exportCSV(file, mainWindow);
                } else {
                    exportText(file, mainWindow);
                }
                mainWindow.getOutputPanel().logSuccess("Results exported to: " + file.getAbsolutePath());
                JOptionPane.showMessageDialog(
                    mainWindow,
                    "Results exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException e) {
                mainWindow.getOutputPanel().logError("Export failed: " + e.getMessage());
                JOptionPane.showMessageDialog(
                    mainWindow,
                    "Failed to export results: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private static void exportText(java.io.File file, MainWindow mainWindow) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Runtime runtime = Runtime.getRuntime();
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("ThreadGauge XP - Test Results Export\n");
            writer.write("=====================================\n\n");
            
            writer.write("Export Date: " + dateFormat.format(new Date()) + "\n\n");
            
            // System Information
            writer.write("SYSTEM INFORMATION\n");
            writer.write("------------------\n");
            writer.write("Operating System: " + System.getProperty("os.name") + " " + 
                        System.getProperty("os.version") + "\n");
            writer.write("OS Architecture: " + System.getProperty("os.arch") + "\n");
            writer.write("JVM Version: " + System.getProperty("java.version") + "\n");
            writer.write("JVM Vendor: " + System.getProperty("java.vendor") + "\n");
            writer.write("Available Processors: " + runtime.availableProcessors() + "\n");
            writer.write("Total Memory: " + (runtime.maxMemory() / (1024 * 1024)) + " MB\n");
            writer.write("Free Memory: " + (runtime.freeMemory() / (1024 * 1024)) + " MB\n");
            writer.write("Active Threads: " + Thread.activeCount() + "\n\n");
            
            // Test Log
            writer.write("TEST LOG\n");
            writer.write("--------\n");
            writer.write(mainWindow.getOutputPanel().getLogText());
            writer.write("\n\n");
            
            writer.write("End of Report\n");
        }
    }
    
    private static void exportCSV(java.io.File file, MainWindow mainWindow) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        try (FileWriter writer = new FileWriter(file)) {
            // Header
            writer.write("Metric,Value\n");
            
            // Metadata
            writer.write("Export Date," + dateFormat.format(new Date()) + "\n");
            writer.write("Application,ThreadGauge XP\n");
            writer.write("\n");
            
            // System info
            writer.write("Operating System," + System.getProperty("os.name") + " " + 
                        System.getProperty("os.version") + "\n");
            writer.write("OS Architecture," + System.getProperty("os.arch") + "\n");
            writer.write("JVM Version," + System.getProperty("java.version") + "\n");
            writer.write("JVM Vendor," + System.getProperty("java.vendor") + "\n");
            writer.write("Available Processors," + runtime.availableProcessors() + "\n");
            writer.write("Total Memory (MB)," + (runtime.maxMemory() / (1024 * 1024)) + "\n");
            writer.write("Free Memory (MB)," + (runtime.freeMemory() / (1024 * 1024)) + "\n");
            writer.write("Used Memory (MB)," + 
                        ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)) + "\n");
            writer.write("Active Threads," + Thread.activeCount() + "\n");
            writer.write("\n");
            
            // Note about log
            writer.write("\nNote: Full test log available in text export format\n");
        }
    }
}

package dev.threadgaugexp.core;

import dev.threadgaugexp.MainWindow;

import javax.swing.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

public class StressTest extends SwingWorker<StressResult, String> {
    private MainWindow mainWindow;
    private int threadCount;
    private int durationSeconds;
    private volatile boolean stopRequested = false;

    public StressTest(MainWindow mainWindow, int threadCount, int durationSeconds) {
        this.mainWindow = mainWindow;
        this.threadCount = threadCount;
        this.durationSeconds = durationSeconds;
    }

    @Override
    protected StressResult doInBackground() throws Exception {
        publish("Starting stress test with " + threadCount + " threads for " + durationSeconds + " seconds");
        mainWindow.setStatus("Running stress test...");

        List<Thread> threads = new ArrayList<>();
        List<Double> cpuSamples = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        // Create worker threads
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted() && !stopRequested) {
                    // Light computational work
                    double result = 0;
                    for (int j = 0; j < 1000; j++) {
                        result += Math.sqrt(j) * Math.sin(j);
                    }
                    
                    try {
                        Thread.sleep(10); // Small sleep to avoid busy-spin
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            thread.setName("StressThread-" + i);
            thread.start();
            threads.add(thread);
        }

        publish("All " + threadCount + " threads started. Monitoring...");

        // Monitor CPU while test runs
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long endTime = startTime + (durationSeconds * 1000L);
        
        while (System.currentTimeMillis() < endTime && !isCancelled()) {
            try {
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean sunOsBean = 
                        (com.sun.management.OperatingSystemMXBean) osBean;
                    double cpuLoad = sunOsBean.getCpuLoad();
                    if (cpuLoad >= 0) {
                        cpuSamples.add(cpuLoad * 100);
                    }
                }
            } catch (Exception e) {
                // Ignore CPU sampling errors
            }

            long remaining = (endTime - System.currentTimeMillis()) / 1000;
            if (remaining > 0 && remaining % 2 == 0) {
                publish("Test running... " + remaining + " seconds remaining");
            }
            
            Thread.sleep(500);
        }

        long actualDuration = System.currentTimeMillis() - startTime;
        
        publish("Stopping threads...");
        stopRequested = true;

        // Stop all threads
        for (Thread thread : threads) {
            thread.interrupt();
        }

        // Wait for threads to finish
        for (Thread thread : threads) {
            try {
                thread.join(100);
            } catch (InterruptedException e) {
                // Continue cleanup
            }
        }

        // Calculate average CPU
        double avgCpu = 0;
        if (!cpuSamples.isEmpty()) {
            double sum = 0;
            for (double sample : cpuSamples) {
                sum += sample;
            }
            avgCpu = sum / cpuSamples.size();
        }

        StressResult result = new StressResult();
        result.threadCount = threadCount;
        result.plannedDuration = durationSeconds;
        result.actualDuration = actualDuration / 1000.0;
        result.averageCpuLoad = avgCpu;
        result.cancelled = isCancelled();

        return result;
    }

    @Override
    protected void process(List<String> chunks) {
        for (String message : chunks) {
            mainWindow.getOutputPanel().log(message);
        }
    }

    @Override
    protected void done() {
        try {
            StressResult result = get();
            String status = result.cancelled ? "cancelled" : "completed";
            String message = String.format(
                "Stress test %s:\nThreads: %d\nDuration: %.1f / %d seconds\nAverage CPU Load: %.1f%%",
                status, result.threadCount, result.actualDuration, result.plannedDuration, result.averageCpuLoad
            );
            
            if (result.cancelled) {
                mainWindow.getOutputPanel().logWarning(message);
            } else {
                mainWindow.getOutputPanel().logSuccess(message);
            }
            
            JOptionPane.showMessageDialog(
                mainWindow,
                message,
                "Stress Test " + (result.cancelled ? "Cancelled" : "Complete"),
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception e) {
            mainWindow.getOutputPanel().logError("Stress test failed: " + e.getMessage());
        } finally {
            mainWindow.setStatus("Ready");
            mainWindow.getControlsPanel().testCompleted();
        }
    }

    public static class StressResult {
        public int threadCount;
        public int plannedDuration;
        public double actualDuration;
        public double averageCpuLoad;
        public boolean cancelled;
    }
}

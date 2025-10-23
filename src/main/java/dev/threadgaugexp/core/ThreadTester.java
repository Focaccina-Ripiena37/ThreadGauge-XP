package dev.threadgaugexp.core;

import dev.threadgaugexp.MainWindow;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ThreadTester extends SwingWorker<TestResult, String> {
    private MainWindow mainWindow;
    private int stackSizeKB;
    private static final int SAFETY_CAP = 50000;
    private static final int BATCH_SIZE = 100;
    private static final long MIN_FREE_HEAP_MB = 50;

    public ThreadTester(MainWindow mainWindow, int stackSizeKB) {
        this.mainWindow = mainWindow;
        this.stackSizeKB = stackSizeKB;
    }

    @Override
    protected TestResult doInBackground() throws Exception {
        publish("Starting max thread test with stack size: " + stackSizeKB + " KB");
        mainWindow.setStatus("Testing max threads...");

        List<Thread> threads = new ArrayList<>();
        int threadCount = 0;
        boolean limitReached = false;
        String stopReason = "";

        // Measure memory before
        System.gc();
        Thread.sleep(100);
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        try {
            while (!isCancelled() && threadCount < SAFETY_CAP) {
                // Check available heap
                long freeHeap = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
                if (freeHeap < MIN_FREE_HEAP_MB * 1024 * 1024) {
                    stopReason = "Low heap memory (< " + MIN_FREE_HEAP_MB + " MB free)";
                    break;
                }

                // Create batch of threads
                for (int i = 0; i < BATCH_SIZE && threadCount < SAFETY_CAP; i++) {
                    try {
                        Thread thread = new Thread(() -> {
                            try {
                                while (!Thread.currentThread().isInterrupted()) {
                                    Thread.sleep(1000);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                        
                        // Set stack size if supported
                        if (stackSizeKB > 0) {
                            thread = new Thread(null, () -> {
                                try {
                                    while (!Thread.currentThread().isInterrupted()) {
                                        Thread.sleep(1000);
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }, "TestThread-" + threadCount, stackSizeKB * 1024L);
                        }
                        
                        thread.start();
                        threads.add(thread);
                        threadCount++;
                    } catch (OutOfMemoryError e) {
                        limitReached = true;
                        stopReason = "OutOfMemoryError caught";
                        break;
                    }
                }

                if (limitReached) {
                    break;
                }

                if (threadCount % 500 == 0) {
                    publish("Created " + threadCount + " threads so far...");
                }

                Thread.sleep(10);
            }
        } catch (OutOfMemoryError e) {
            limitReached = true;
            stopReason = "OutOfMemoryError: " + e.getMessage();
        }

        if (isCancelled()) {
            stopReason = "Test cancelled by user";
        } else if (threadCount >= SAFETY_CAP) {
            stopReason = "Safety cap reached (" + SAFETY_CAP + " threads)";
        }

        // Measure memory after
        System.gc();
        Thread.sleep(100);
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryPerThread = threadCount > 0 ? (memoryAfter - memoryBefore) / threadCount : 0;

        publish("Test complete. Cleaning up threads...");

        // Cleanup
        for (Thread thread : threads) {
            thread.interrupt();
        }

        // Wait a bit for cleanup
        Thread.sleep(500);

        TestResult result = new TestResult();
        result.maxThreads = threadCount;
        result.stackSizeKB = stackSizeKB;
        result.memoryPerThreadKB = memoryPerThread / 1024;
        result.stopReason = stopReason;

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
            if (!isCancelled()) {
                TestResult result = get();
                String message = String.format(
                    "Max threads reached: %d\nStack size: %d KB\nMemory per thread: ~%d KB\nReason: %s",
                    result.maxThreads, result.stackSizeKB, result.memoryPerThreadKB, result.stopReason
                );
                mainWindow.getOutputPanel().logSuccess(message);
                
                JOptionPane.showMessageDialog(
                    mainWindow,
                    message,
                    "Max Threads Test Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                mainWindow.getOutputPanel().logWarning("Max threads test was cancelled.");
            }
        } catch (Exception e) {
            mainWindow.getOutputPanel().logError("Test failed: " + e.getMessage());
        } finally {
            mainWindow.setStatus("Ready");
            mainWindow.getControlsPanel().testCompleted();
        }
    }

    public static class TestResult {
        public int maxThreads;
        public int stackSizeKB;
        public long memoryPerThreadKB;
        public String stopReason;
    }
}

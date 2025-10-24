package dev.threadgaugexp.core;

import dev.threadgaugexp.MainWindow;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ThreadTester extends SwingWorker<TestResult, String> {
    private MainWindow mainWindow;
    private int stackSizeKB;
    private static final int SAFETY_CAP = 50000;
    private static final int BATCH_SIZE = 100;
    private static final long MIN_FREE_HEAP_MB = 50;
    private static final long JOIN_TIMEOUT_MS = 200; // avoid indefinite waits during cleanup

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

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

        boolean defaultedStackOnWindows = false;
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
                        Thread thread;
                        Runnable worker = () -> {
                            // Low-impact idle loop that responds quickly to interrupts
                            while (!Thread.currentThread().isInterrupted()) {
                                LockSupport.parkNanos(1_000_000L); // ~1ms
                            }
                        };

                        boolean useCustomStack = (stackSizeKB > 0) && !(isWindows() && stackSizeKB == 512);
                        if (!useCustomStack && stackSizeKB > 0 && isWindows()) {
                            defaultedStackOnWindows = true;
                        }
                        thread = useCustomStack
                                ? new Thread(null, worker, "TestThread-" + threadCount, stackSizeKB * 1024L)
                                : new Thread(worker, "TestThread-" + threadCount);

                        // Ensure these threads never block JVM shutdown and are easy to reap
                        thread.setDaemon(true);
                        thread.start();
                        threads.add(thread);
                        threadCount++;
                    } catch (OutOfMemoryError e) {
                        limitReached = true;
                        stopReason = "OutOfMemoryError caught";
                        break;
                    } catch (Throwable t) {
                        // Catch other failures like unable to create native thread
                        limitReached = true;
                        stopReason = "Thread creation failed: " + t.getClass().getSimpleName();
                        break;
                    }
                }

                if (limitReached) {
                    break;
                }

                if (threadCount % 500 == 0) {
                    publish("Created " + threadCount + " threads so far...");
                }

                // brief yield between batches
                LockSupport.parkNanos(1_000_000L);
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

        // Cleanup: interrupt and join with timeouts to avoid hangs
        for (Thread thread : threads) {
            try {
                thread.interrupt();
            } catch (Throwable ignored) {}
        }
        // Join with bounded wait per thread (in background worker thread, not EDT)
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); // overall budget
        for (Thread thread : threads) {
            long remainingMs = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
            if (remainingMs <= 0) break; // stop waiting if budget exceeded
            try {
                thread.join(Math.min(JOIN_TIMEOUT_MS, remainingMs));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        TestResult result = new TestResult();
        result.maxThreads = threadCount;
        result.stackSizeKB = stackSizeKB;
        result.memoryPerThreadKB = memoryPerThread / 1024;
        if (defaultedStackOnWindows) {
            result.stopReason = stopReason + " | Note: used JVM default stack on Windows (requested " + stackSizeKB + " KB)";
        } else {
            result.stopReason = stopReason;
        }

        return result;
    }

    // Package-private helper used by tests: spawns N threads then cleans up deterministically.
    static boolean spawnAndCleanup(int threadsToCreate, int stackSizeKB) {
        List<Thread> threads = new ArrayList<>(threadsToCreate);
        for (int i = 0; i < threadsToCreate; i++) {
            Runnable worker = () -> {
                while (!Thread.currentThread().isInterrupted()) {
                    LockSupport.parkNanos(1_000_000L);
                }
            };
            Thread t = (stackSizeKB > 0)
                ? new Thread(null, worker, "TestThread-" + i, stackSizeKB * 1024L)
                : new Thread(worker, "TestThread-" + i);
            t.setDaemon(true);
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) {
            t.interrupt();
        }
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        boolean allStopped = true;
        for (Thread t : threads) {
            long remainingMs = Math.max(50, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
            try {
                t.join(Math.min(remainingMs, JOIN_TIMEOUT_MS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            if (t.isAlive()) allStopped = false;
        }
        return allStopped;
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

    
}

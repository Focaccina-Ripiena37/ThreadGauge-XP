package dev.threadgaugexp.core;

import java.util.ArrayList;
import java.util.List;

public class MemoryEstimator {
    
    public static MemoryResult estimateThreadMemory(int sampleSize, int stackSizeKB) throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection and wait
        System.gc();
        Thread.sleep(200);
        
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Create sample threads
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            Thread thread;
            if (stackSizeKB > 0) {
                thread = new Thread(null, () -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, "MemTestThread-" + i, stackSizeKB * 1024L);
            } else {
                thread = new Thread(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            thread.start();
            threads.add(thread);
        }
        
        // Wait a bit for threads to stabilize
        Thread.sleep(200);
        
        // Force garbage collection and measure
        System.gc();
        Thread.sleep(200);
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        
        // Cleanup
        for (Thread thread : threads) {
            thread.interrupt();
        }
        
        for (Thread thread : threads) {
            thread.join(100);
        }
        
        long totalMemoryUsed = memoryAfter - memoryBefore;
        long memoryPerThread = totalMemoryUsed / sampleSize;
        
        MemoryResult result = new MemoryResult();
        result.sampleSize = sampleSize;
        result.totalMemoryKB = totalMemoryUsed / 1024;
        result.memoryPerThreadKB = memoryPerThread / 1024;
        
        return result;
    }
    
    public static class MemoryResult {
        public int sampleSize;
        public long totalMemoryKB;
        public long memoryPerThreadKB;
    }
}

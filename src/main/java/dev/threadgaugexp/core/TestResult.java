package dev.threadgaugexp.core;

/**
 * Result data for the ThreadTester SwingWorker.
 */
public class TestResult {
    public int maxThreads;
    public int stackSizeKB;
    public long memoryPerThreadKB;
    public String stopReason;
}

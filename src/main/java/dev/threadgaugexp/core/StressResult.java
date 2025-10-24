package dev.threadgaugexp.core;

/**
 * Result data for the StressTest SwingWorker.
 */
public class StressResult {
    public int threadCount;
    public int plannedDuration;
    public double actualDuration;
    public double averageCpuLoad;
    public boolean cancelled;
}

package gov.epa.emissions.framework.services;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PerformanceMetrics {
    private long startMemory;

    private long startTime;

    private static Log LOG = LogFactory.getLog(PerformanceMetrics.class);

    public void startTracking() {
        startMemory = usedMemory();
        startTime = time();
    }

    public void gc() {
        startTracking();

        LOG.warn("gc started....");
        System.gc();
        LOG.warn("gc complete");

        dumpStats();
    }

    public long time() {
        return new Date().getTime() / 1000;
    }

    public void dumpStats() {
        dumpStats("");
    }

    public void dumpStats(String prefix) {
        long end = usedMemory();
        LOG.warn(prefix + "\tTime: " + (time() - startTime) + " secs using " + (end - startMemory) + " MB memory "
                + "(end:" + end + ", start: " + startMemory + ")");
    }

    public long usedMemory() {
        return (totalMemory() - freeMemory());
    }

    public long freeMemory() {
        return Runtime.getRuntime().freeMemory() / megabyte();
    }

    private int megabyte() {
        return (1024 * 1024);
    }

    public long totalMemory() {
        return Runtime.getRuntime().totalMemory() / megabyte();
    }

}

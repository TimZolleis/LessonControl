package de.waldorfaugsburg.lessoncontrol.client.performance;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkState;
import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientPerformancePacket;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public final class PerformanceManager {

    private final Runtime runtime = Runtime.getRuntime();
    private final OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

    private final LessonControlClientApplication application;
    private final int processorCount;
    private final long totalMemory;

    public PerformanceManager(final LessonControlClientApplication application) {
        this.application = application;
        this.processorCount = bean.getAvailableProcessors();
        this.totalMemory = runtime.totalMemory();
    }

    public void startPerformanceTransmission() {
        Scheduler.schedule(this::transmitPerformance, 1000);
    }

    private void transmitPerformance() {
        if (application.getNetworkClient().getState() != NetworkState.READY) return;

        final long usedMemory = totalMemory - runtime.freeMemory();
        application.getNetworkClient().sendPacket(new ClientPerformancePacket(bean.getSystemLoadAverage(), usedMemory));
    }

    public int getProcessorCount() {
        return processorCount;
    }

    public long getTotalMemory() {
        return totalMemory;
    }
}

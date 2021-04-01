package de.waldorfaugsburg.lessoncontrol.client.util;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public final class SystemResourcesUtil {

    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final CentralProcessor CENTRAL_PROCESSOR = SYSTEM_INFO.getHardware().getProcessor();
    private static long[] PREVIOUS_TICKS = new long[CentralProcessor.TickType.values().length];

    public static double getLoad() {
        final double load = CENTRAL_PROCESSOR.getSystemCpuLoadBetweenTicks(PREVIOUS_TICKS);
        PREVIOUS_TICKS = CENTRAL_PROCESSOR.getSystemCpuLoadTicks();
        return load * 100;
    }

    public static long getTotalMemory() {
        return SYSTEM_INFO.getHardware().getMemory().getTotal();
    }

    public static long getFreeMemory() {
        return SYSTEM_INFO.getHardware().getMemory().getAvailable();
    }
}

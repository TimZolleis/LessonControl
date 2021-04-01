package de.waldorfaugsburg.lessoncontrol.client.util;

import oshi.SystemInfo;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public final class SystemResourcesUtil {

    private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();

    public static int getThreads() {
        return SYSTEM_INFO.getHardware().getProcessor().getLogicalProcessorCount();
    }

    public static double getLoad() {
        return OPERATING_SYSTEM_MX_BEAN.getSystemLoadAverage() / getThreads();
    }

    public static long getTotalMemory() {
        return SYSTEM_INFO.getHardware().getMemory().getTotal();
    }

    public static long getFreeMemory() {
        return SYSTEM_INFO.getHardware().getMemory().getAvailable();
    }
}

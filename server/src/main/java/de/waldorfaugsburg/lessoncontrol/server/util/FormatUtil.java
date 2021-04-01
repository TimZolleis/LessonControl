package de.waldorfaugsburg.lessoncontrol.server.util;

import java.text.DecimalFormat;

public final class FormatUtil {

    private static final double GIGABYTE = 1024 * 1024 * 1024;
    private static final DecimalFormat MEMORY_FORMAT = new DecimalFormat("##0.##");

    public static String formatMemory(final double memory) {
        return MEMORY_FORMAT.format(memory / GIGABYTE) + " GB";
    }
}

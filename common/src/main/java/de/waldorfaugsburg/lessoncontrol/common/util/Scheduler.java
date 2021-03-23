package de.waldorfaugsburg.lessoncontrol.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Scheduler {

    private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(8);

    private Scheduler() {

    }

    public static void runLater(final Runnable runnable, final long delay) {
        SERVICE.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public static void schedule(final Runnable runnable, final long period) {
        schedule(runnable, 0, period);
    }

    public static void schedule(final Runnable runnable, final long delay, final long period) {
        SERVICE.scheduleAtFixedRate(runnable, delay, period, TimeUnit.MILLISECONDS);
    }
}

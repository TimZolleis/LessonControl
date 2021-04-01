package de.waldorfaugsburg.lessoncontrol.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class Scheduler {

    private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(8);

    private Scheduler() {

    }

    public static ScheduledFuture<?> runLater(final Runnable runnable, final long delay) {
        return SERVICE.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> schedule(final Runnable runnable, final long period) {
        return schedule(runnable, 0, period);
    }

    public static ScheduledFuture<?> schedule(final Runnable runnable, final long delay, final long period) {
        return SERVICE.scheduleAtFixedRate(runnable, delay, period, TimeUnit.MILLISECONDS);
    }
}

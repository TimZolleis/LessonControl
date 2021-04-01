package de.waldorfaugsburg.lessoncontrol.client.network;

import de.waldorfaugsburg.lessoncontrol.client.util.SystemResourcesUtil;
import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientSystemResourcesPacket;

public final class PerformanceRunnable implements Runnable {

    private final NetworkClient client;

    public PerformanceRunnable(final NetworkClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        client.sendPacket(new ClientSystemResourcesPacket(SystemResourcesUtil.getFreeMemory(), SystemResourcesUtil.getLoad()));
    }
}

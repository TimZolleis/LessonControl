package de.waldorfaugsburg.lessoncontrol.client.network;

import de.waldorfaugsburg.lessoncontrol.client.util.SystemResourcesUtil;
import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientSystemResourcesPacket;

public final class SystemResourceTransmissionRunnable implements Runnable {

    private final NetworkClient client;

    public SystemResourceTransmissionRunnable(final NetworkClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        if (client.getState() == NetworkState.READY)
            client.sendPacket(new ClientSystemResourcesPacket(SystemResourcesUtil.getFreeMemory(), SystemResourcesUtil.getLoad()));
    }
}

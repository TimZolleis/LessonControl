package de.waldorfaugsburg.lessoncontrol.server.device;

import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientSystemResourcesPacket;
import de.waldorfaugsburg.lessoncontrol.server.network.NetworkServer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public final class DeviceSystemResourcesListener {

    private final NetworkServer networkServer;

    public DeviceSystemResourcesListener(final NetworkServer networkServer) {
        this.networkServer = networkServer;
    }

    @PostConstruct
    private void init() {
        networkServer.getDistributor().addReceiver(ClientSystemResourcesPacket.class, (connection, packet) -> {
            final Device device = connection.getDevice();
            device.updateSystemResources(packet.getFreeMemory(), packet.getLoad());
        });
    }
}

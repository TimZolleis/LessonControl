package de.waldorfaugsburg.lessoncontrol.server.device;

import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientPerformancePacket;
import de.waldorfaugsburg.lessoncontrol.server.network.NetworkServer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public final class DevicePerformanceService {

    private final NetworkServer networkServer;

    public DevicePerformanceService(final NetworkServer networkServer) {
        this.networkServer = networkServer;
    }

    @PostConstruct
    private void init() {
        networkServer.getDistributor().addReceiver(ClientPerformancePacket.class, (connection, packet) -> {
            final Device device = connection.getDevice();
            device.setCpuUsage(packet.getCpuUsage());
            device.setUsedMemory(packet.getUsedMemory());
        });
    }
}

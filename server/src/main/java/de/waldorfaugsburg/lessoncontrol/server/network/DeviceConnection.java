package de.waldorfaugsburg.lessoncontrol.server.network;

import com.esotericsoftware.kryonet.Connection;
import de.waldorfaugsburg.lessoncontrol.common.network.server.AcceptPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.DenyPacket;
import de.waldorfaugsburg.lessoncontrol.server.device.Device;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DeviceConnection extends Connection {

    private Device device;

    public void accept(final Device device) {
        this.device = device;

        sendTCP(new AcceptPacket());
        log.info("'{}' registered as '{}'", getRemoteAddressTCP().getHostString(), device.getName());
    }

    public void deny(final DenyPacket.Reason reason, final String message) {
        final String hostString = getRemoteAddressTCP().getHostString();
        sendTCP(new DenyPacket(DenyPacket.Reason.OUTDATED_CLIENT, message));
        close();
        log.info("Denied '{}' due to '{}' ({})", hostString, reason, message);
    }

    public Device getDevice() {
        return device;
    }
}

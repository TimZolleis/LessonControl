package de.waldorfaugsburg.lessoncontrol.server.network;

import com.esotericsoftware.kryonet.Connection;
import de.waldorfaugsburg.lessoncontrol.server.device.Device;
import lombok.Data;

@Data
public final class DeviceConnection extends Connection {
    private Device device;
}

package de.waldorfaugsburg.lessoncontrol.server.device;

import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientRegisterPacket;
import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.network.DeviceConnection;

public final class Device {

    private final DeviceConfiguration.DeviceInfo info;

    private DeviceConnection connection;
    private DeviceState state = DeviceState.OFFLINE;

    private long totalMemory;
    private long freeMemory;
    private double load;

    public Device(final DeviceConfiguration.DeviceInfo info) {
        this.info = info;
    }

    public void handleRegistration(final DeviceConnection connection, final ClientRegisterPacket packet) {
        this.connection = connection;

        state = DeviceState.ONLINE;
        totalMemory = packet.getTotalMemory();
    }

    public void updateSystemResources(final long freeMemory, final double load) {
        this.freeMemory = freeMemory;
        this.load = load;
    }

    public String getName() {
        return info.getName();
    }

    public DeviceConfiguration.DeviceInfo getInfo() {
        return info;
    }

    public DeviceConnection getConnection() {
        return connection;
    }

    public DeviceState getState() {
        return state;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public double getLoad() {
        return load;
    }
}

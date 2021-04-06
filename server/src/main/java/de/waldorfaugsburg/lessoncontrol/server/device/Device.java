package de.waldorfaugsburg.lessoncontrol.server.device;

import de.waldorfaugsburg.lessoncontrol.common.network.client.RegisterPacket;
import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.network.DeviceConnection;

public final class Device {

    private final DeviceConfiguration.DeviceInfo info;

    private DeviceConnection connection;
    private long connectedAt;

    private long totalMemory;
    private long freeMemory;
    private double load;

    public Device(final DeviceConfiguration.DeviceInfo info) {
        this.info = info;
    }

    public void handleConnect(final DeviceConnection connection, final RegisterPacket packet) {
        this.totalMemory = packet.getTotalMemory();
        this.connectedAt = System.currentTimeMillis();

        // We're connected after setting this field
        this.connection = connection;
    }

    public void updateSystemResources(final long freeMemory, final double load) {
        this.freeMemory = freeMemory;
        this.load = load;
    }

    public void handleDisconnect() {
        // We disconnect by nulling connection; then reset data fields
        this.connection = null;

        this.totalMemory = this.freeMemory = 0;
        this.load = 0D;
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

    public boolean isConnected() {
        return connection != null;
    }

    public long getConnectedAt() {
        return connectedAt;
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

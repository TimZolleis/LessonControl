package de.waldorfaugsburg.lessoncontrol.server.device;

import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientRegisterPacket;
import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.network.DeviceConnection;

public final class Device {

    private final DeviceConfiguration.DeviceInfo info;

    private DeviceState state = DeviceState.OFFLINE;

    private DeviceConnection connection;
    private int processorCount;
    private double cpuUsage;
    private long totalMemory;
    private long usedMemory;

    public Device(final DeviceConfiguration.DeviceInfo info) {
        this.info = info;
    }

    public void handleRegistration(final DeviceConnection connection, final ClientRegisterPacket packet) {
        this.connection = connection;

        state = DeviceState.ONLINE;
        processorCount = packet.getProcessorCount();
        totalMemory = packet.getTotalMemory();
    }

    public String getName() {
        return info.getName();
    }

    public DeviceConfiguration.DeviceInfo getInfo() {
        return info;
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(final DeviceState state) {
        this.state = state;
    }

    public DeviceConnection getConnection() {
        return connection;
    }

    public int getProcessorCount() {
        return processorCount;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(final double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(final long usedMemory) {
        this.usedMemory = usedMemory;
    }
}

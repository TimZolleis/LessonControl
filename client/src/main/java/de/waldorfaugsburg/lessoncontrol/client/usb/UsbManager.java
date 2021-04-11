package de.waldorfaugsburg.lessoncontrol.client.usb;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;
import lombok.extern.slf4j.Slf4j;
import oshi.hardware.UsbDevice;
import oshi.hardware.platform.windows.WindowsUsbDevice;

import java.util.List;

@Slf4j
public final class UsbManager {

    private List<UsbDevice> lastDevices;

    public UsbManager(final LessonControlClientApplication application) {
        Scheduler.schedule(() -> {
            final List<UsbDevice> devices = WindowsUsbDevice.getUsbDevices(false);
            if (lastDevices != null) {
                for (final UsbDevice device : lastDevices) {
                    if (!containsDevice(devices, device.getName())) {
                        application.getEventDistributor().call(UsbListener.class, l -> l.deviceDisconnected(device));
                        log.info("Device '{}' disconnected!", device.getName());
                    }
                }
                for (final UsbDevice device : devices) {
                    if (!containsDevice(lastDevices, device.getName())) {
                        application.getEventDistributor().call(UsbListener.class, l -> l.deviceConnected(device));
                        log.info("Device '{}' connected!", device.getName());
                    }
                }
            }
            lastDevices = devices;
        }, 1000);
    }

    public boolean isConnected(final String deviceName) {
        return containsDevice(lastDevices, deviceName);
    }

    private boolean containsDevice(final List<UsbDevice> devices, final String deviceName) {
        for (final UsbDevice currentDevice : devices) {
            if (currentDevice.getName().equals(deviceName)) return true;
        }
        return false;
    }
}

package de.waldorfaugsburg.lessoncontrol.client.service.obs;

import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.common.service.OBSServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;
import lombok.extern.slf4j.Slf4j;
import net.twasi.obsremotejava.OBSRemoteController;
import oshi.hardware.UsbDevice;
import oshi.hardware.platform.windows.WindowsUsbDevice;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public final class OBSService extends AbstractService<OBSServiceConfiguration> {

    private static final String WORKING_DIRECTORY = "C:\\Program Files\\obs-studio\\bin\\64bit\\";
    private static final String EXECUTABLE_PATH = WORKING_DIRECTORY + "obs64.exe";

    private ScheduledFuture<?> cameraTask;
    private List<UsbDevice> lastDevices;
    private OBSRemoteController controller;
    private boolean connected;

    public OBSService(final OBSServiceConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void enable() {
        cameraTask = Scheduler.schedule(() -> {
            if (!connected) return;

            final List<UsbDevice> devices = WindowsUsbDevice.getUsbDevices(false);
            if (lastDevices != null) {
                for (final UsbDevice device : devices) {
                    if (getConfiguration().getRestartingCameras().contains(device.getName()) && isUnknown(device.getName())) {
                        restart();
                    }
                }
            }
            lastDevices = devices;
        }, 1000);

        start();
    }

    @Override
    public void disable() {
        cameraTask.cancel(true);
        stop();
    }

    private void restart() {
        stop();
        start();
    }

    private void start() {
        try {
            new ProcessBuilder(EXECUTABLE_PATH).directory(new File(WORKING_DIRECTORY)).start();
        } catch (final IOException e) {
            log.error("An error occurred starting OBS", e);
        }

        controller = new OBSRemoteController("ws://localhost:4444", false);
        controller.registerConnectCallback(response -> connected = true);
        controller.registerDisconnectCallback(() -> connected = false);
    }

    private void stop() {
        controller.disconnect();
        try {
            Runtime.getRuntime().exec("taskkill /F /T /IM obs64.exe");
        } catch (final IOException e) {
            log.error("An error occurred stopping OBS", e);
        }
    }

    private boolean isUnknown(final String name) {
        for (final UsbDevice device : lastDevices) {
            if (device.getName().equals(name)) return false;
        }
        return true;
    }
}

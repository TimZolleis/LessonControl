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

    private OBSRemoteController controller;
    private ScheduledFuture<?> cameraTask;
    private List<UsbDevice> lastDevices;
    private boolean running;

    public OBSService(final OBSServiceConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void enable() {
        cameraTask = Scheduler.schedule(() -> {
            final List<UsbDevice> usbDevices = WindowsUsbDevice.getUsbDevices(false);
            if (lastDevices != null) {
                for (final UsbDevice device : usbDevices) {
                    if (getConfiguration().getRestartingCameras().contains(device.getName()) && isUnknown(device.getName())) {
                        restartOBS();
                    }
                }
            }
            lastDevices = usbDevices;
        }, 1000);

        startOBS();

        controller = new OBSRemoteController("ws://localhost:4444", false);
        controller.registerConnectCallback(response -> {


            log.info("Successfully connected to OBS v{} via WS v{}", response.getObsStudioVersion(), response.getObsWebsocketVersion());
        });
    }

    private void startOBS() {
        try {
            new ProcessBuilder(EXECUTABLE_PATH).directory(new File(WORKING_DIRECTORY)).start();
            Thread.sleep(3000);
        } catch (final IOException | InterruptedException e) {
            log.error("An error occurred starting OBS", e);
        }

    }

    private void restartOBS() {
        try {
            Runtime.getRuntime().exec("taskkill /F /T /IM obs64.exe");
            startOBS();
        } catch (final IOException e) {
            log.error("An error occurred stopping OBS", e);
        }
    }

    @Override
    public void disable() {
        cameraTask.cancel(true);
        controller.disconnect();
    }

    private boolean isUnknown(final String name) {
        for (final UsbDevice device : lastDevices) {
            if (device.getName().equals(name)) return false;
        }
        return true;
    }
}

package de.waldorfaugsburg.lessoncontrol.client.service.obs;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
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

    public OBSService(final LessonControlClientApplication application, final OBSServiceConfiguration configuration) {
        super(application, configuration);
    }

    @Override
    public void enable() {
        cameraTask = Scheduler.schedule(() -> {
            if (!connected) return;

            // Check if camera is known or was plugged-in previously
            final List<UsbDevice> devices = WindowsUsbDevice.getUsbDevices (true);
            if (lastDevices != null) {
                for (final UsbDevice device : devices) {
                    if (getConfiguration().getDocumentCamera().getCamera().equals(device.getName()) && isUnknown(device.getName())) {
                        restart();
                    }
                }
            }
            lastDevices = devices;

            // Set source visibility
            final OBSServiceConfiguration.DocumentCamera documentCamera = getConfiguration().getDocumentCamera();
            final boolean visibility = !isUnknown(documentCamera.getCamera());
            controller.setSourceVisibility(documentCamera.getScene(), documentCamera.getSource(), visibility, r -> {
            });
        }, 1000);

        start();
    }

    @Override
    public void disable(final boolean shutdown) {
        cameraTask.cancel(true);
        stop();
    }

    private void restart() {
        stop();
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        start();
    }

    private void start() {
        try {
            new ProcessBuilder(EXECUTABLE_PATH, "--startvirtualcam",
                    "--disable-updater",
                    "--minimize-to-tray",
                    "--collection", getConfiguration().getSceneCollection())
                    .directory(new File(WORKING_DIRECTORY))
                    .start();
        } catch (final IOException e) {
            log.error("An error occurred starting OBS", e);
        }

        // Initialize OBS websocket
        controller = new OBSRemoteController("ws://localhost:4444", false);
        controller.registerConnectCallback(response -> connected = true);
        controller.registerDisconnectCallback(() -> {
            connected = false;
            // If we disconnect we want to kill OBS
            try {
                Runtime.getRuntime().exec("taskkill /F /T /IM obs64.exe");
            } catch (final IOException e) {
                log.error("An error occurred stopping OBS", e);
            }
        });
    }

    private void stop() {
        controller.disconnect();
    }

    private boolean isUnknown(final String name) {
        for (final UsbDevice device : lastDevices) {
            if (device.getName().equals(name)) return false;
        }
        return true;
    }
}

package de.waldorfaugsburg.lessoncontrol.client.service.obs;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.client.usb.UsbListener;
import de.waldorfaugsburg.lessoncontrol.client.util.CommandExecutionUtil;
import de.waldorfaugsburg.lessoncontrol.common.service.OBSServiceConfiguration;
import lombok.extern.slf4j.Slf4j;
import net.twasi.obsremotejava.OBSRemoteController;
import oshi.hardware.UsbDevice;

import java.io.File;

@Slf4j
public final class OBSService extends AbstractService<OBSServiceConfiguration> {

    private static final String WORKING_DIRECTORY = "C:\\Program Files\\obs-studio\\bin\\64bit\\";
    private static final String EXECUTABLE_PATH = WORKING_DIRECTORY + "obs64.exe";

    private OBSRemoteController controller;

    public OBSService(final LessonControlClientApplication application, final OBSServiceConfiguration configuration) {
        super(application, configuration);
    }

    @Override
    public void enable() {
        getApplication().getEventDistributor().addListener(UsbListener.class, new UsbListener() {
            @Override
            public void deviceConnected(final UsbDevice device) {
                for (final OBSServiceConfiguration.MonitoredCamera camera : getConfiguration().getMonitoredCameras()) {
                    if (device.getName().equals(camera.getName())) {
                        restart();
                    }
                }
            }

            @Override
            public void deviceDisconnected(final UsbDevice device) {
                checkSourceVisibility();
            }
        });
        start();
    }

    @Override
    public void disable(final boolean shutdown) {
        stop();
    }

    private void restart() {
        stop();
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ignored) {
        }
        start();
    }

    private void start() {
        CommandExecutionUtil.run(new File(WORKING_DIRECTORY), EXECUTABLE_PATH,
                "--startvirtualcam",
                "--disable-updater",
                "--minimize-to-tray",
                "--profile", getConfiguration().getProfile(),
                "--collection", getConfiguration().getCollection());

        // Initialize OBS websocket
        controller = new OBSRemoteController("ws://localhost:4444", false);
        controller.registerConnectCallback(response -> checkSourceVisibility());
        controller.registerDisconnectCallback(() -> {
            // If we disconnect we want to kill OBS
            CommandExecutionUtil.run("taskkill", "/F", "/T", "/IM", "obs64.exe");
        });
    }

    private void stop() {
        controller.disconnect();
    }

    private void checkSourceVisibility() {
        for (final OBSServiceConfiguration.MonitoredCamera camera : getConfiguration().getMonitoredCameras()) {
            final OBSServiceConfiguration.SourceVisibility visibility = camera.getVisibility();
            if (visibility != null) {
                final boolean connected = getApplication().getUsbManager().isConnected(camera.getName());
                controller.setSourceVisibility(visibility.getScene(), visibility.getSource(), connected, r -> {
                });
            }
        }
    }
}

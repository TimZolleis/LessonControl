package de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.client.usb.UsbListener;
import de.waldorfaugsburg.lessoncontrol.client.util.CommandExecutionUtil;
import de.waldorfaugsburg.lessoncontrol.common.service.VoicemeeterServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;
import lombok.extern.slf4j.Slf4j;
import oshi.hardware.UsbDevice;

import java.io.File;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public final class VoicemeeterService extends AbstractService<VoicemeeterServiceConfiguration> {

    private ScheduledFuture<?> task;
    private boolean antiHowlEnabled;
    private boolean antiHowlMute;
    private int lastInputDeviceNumber = -1;

    public VoicemeeterService(final LessonControlClientApplication application, final VoicemeeterServiceConfiguration configuration) {
        super(application, configuration);
    }

    @Override
    public void enable() {
        CommandExecutionUtil.run(new File(Voicemeeter.WORKING_DIRECTORY), Voicemeeter.EXECUTABLE_PATH);
        try {
            Thread.sleep(6000);
        } catch (final InterruptedException ignored) {
        }
        Voicemeeter.init();
        loadConfig();
        setAntiHowlEnabled(getConfiguration().getAntiHowl().isEnabled());
        getApplication().getEventDistributor().addListener(UsbListener.class, new UsbListener() {
            @Override
            public void devicesChecked() {
                final int currentInputDeviceNumber = Voicemeeter.getNumberOfInputDevices();
                if (lastInputDeviceNumber != -1) {
                    if (currentInputDeviceNumber > lastInputDeviceNumber) {
                        loadConfig();
                        Voicemeeter.setParameterFloat("Command.Restart", 1);
                    }
                }
                lastInputDeviceNumber = currentInputDeviceNumber;
            }
        });
        task = Scheduler.schedule(() -> {
            if (antiHowlEnabled) {
                final float volume = Voicemeeter.getLevel(0, getConfiguration().getAntiHowl().getMonitoredChannel());
                if (volume >= 5 && !antiHowlMute) {
                    muteStrips(true);
                    antiHowlMute = true;
                } else if (antiHowlMute && volume < 4) {
                    muteStrips(false);
                    antiHowlMute = false;
                }
            }
        }, 100);
    }

    @Override
    public void disable(final boolean shutdown) {
        task.cancel(true);
    }

    public boolean isAntiHowlEnabled() {
        return antiHowlEnabled;
    }

    public void setAntiHowlEnabled(final boolean antiHowlEnabled) {
        this.antiHowlEnabled = antiHowlEnabled;
        this.antiHowlMute = false;

        if (!antiHowlEnabled) {
            muteStrips(false);
        }
    }

    private void loadConfig() {
        final File file = new File(getConfiguration().getConfigPath());
        if (!file.exists()) throw new IllegalStateException("Config is missing");

        Voicemeeter.setParameterString("Command.Load", file.getAbsolutePath());
    }

    private void muteStrips(final boolean muted) {
        for (final int strip : getConfiguration().getAntiHowl().getMuteStrips()) {
            Voicemeeter.setParameterFloat("Strip(" + strip + ").Mute", muted ? 1 : 0);
        }
    }
}

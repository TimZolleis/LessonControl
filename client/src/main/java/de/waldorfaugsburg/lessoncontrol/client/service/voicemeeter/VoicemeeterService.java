package de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.client.usb.UsbListener;
import de.waldorfaugsburg.lessoncontrol.client.util.CommandExecutionUtil;
import de.waldorfaugsburg.lessoncontrol.common.service.VoicemeeterServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

@Slf4j
public final class VoicemeeterService extends AbstractService<VoicemeeterServiceConfiguration> {

    private final Multimap<Integer, Consumer<Boolean>> stripMuteListeners = ArrayListMultimap.create();
    private final Set<Consumer<Boolean>> antiHowlListeners = new HashSet<>();
    private final Map<Integer, Boolean> muteMap = new HashMap<>();

    private ScheduledFuture<?> task;

    private boolean antiHowlEnabled;
    private long nextAntiHowlCheckTime;
    private boolean antiHowlBlocking;

    private boolean stripsDisabled;

    private boolean noiseReductionEnabled;
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
        setNoiseReductionEnabled(getConfiguration().getNoiseReduction().isEnabled());
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
                if (nextAntiHowlCheckTime < System.currentTimeMillis()) {
                    final boolean previousState = antiHowlBlocking;
                    for (final int channelId : getConfiguration().getAntiHowl().getMonitoredChannels()) {
                        final float level = Voicemeeter.getLevel(1, channelId);
                        disableStrips(antiHowlBlocking = level > 3);

                        if (antiHowlBlocking) {
                            // Causes howling protecting to skip some secs of checking time
                            nextAntiHowlCheckTime = System.currentTimeMillis() + getConfiguration().getAntiHowl().getReleaseTime();
                            break;
                        }
                    }

                    // Only consider rendering when there's change
                    if (previousState != antiHowlBlocking) {
                        antiHowlListeners.forEach(l -> l.accept(antiHowlBlocking));
                    }
                }
            }

            if (noiseReductionEnabled) {
                final int[] channels = getConfiguration().getNoiseReduction().getChannels();
                float highestLevel = getConfiguration().getNoiseReduction().getThreshold();
                int loudestStrip = -1;

                for (int i = 0; i < channels.length; i++) {
                    final int channelId = channels[i];
                    final int strip = getConfiguration().getNoiseReduction().getStrips()[i];
                    // Don't do anything if we're muted anyways
                    if (antiHowlBlocking) {
                        continue;
                    }

                    // Determining loudest channel
                    final float level = Voicemeeter.getLevel(1, channelId);
                    if (level > highestLevel) {
                        highestLevel = level;
                        loudestStrip = strip;
                    }
                }

                // If there's one very loud channel -> mute others; if there's none mute all
                for (final int stripId : getConfiguration().getNoiseReduction().getStrips()) {
                    muteStrip(stripId, loudestStrip == -1 || stripId != loudestStrip);
                }
            }
        }, 1);
    }

    @Override
    public void disable(final boolean shutdown) {
        task.cancel(true);
    }

    public void addAntiHowlListener(final Consumer<Boolean> consumer) {
        antiHowlListeners.add(consumer);
    }

    public void addMuteListener(final int strip, final Consumer<Boolean> consumer) {
        stripMuteListeners.put(strip, consumer);
    }

    public boolean isAntiHowlEnabled() {
        return antiHowlEnabled;
    }

    public void setAntiHowlEnabled(final boolean antiHowlEnabled) {
        this.antiHowlEnabled = antiHowlEnabled;
        this.stripsDisabled = false;

        if (!antiHowlEnabled) {
            antiHowlListeners.forEach(l -> l.accept(false));
            disableStrips(false);
        }
    }

    public boolean isNoiseReductionEnabled() {
        return noiseReductionEnabled;
    }

    public void setNoiseReductionEnabled(final boolean noiseReductionEnabled) {
        this.noiseReductionEnabled = noiseReductionEnabled;
    }

    private void loadConfig() {
        final File file = new File(getConfiguration().getConfigPath());
        if (!file.exists()) throw new IllegalStateException("Config is missing");

        Voicemeeter.setParameterString("Command.Load", file.getAbsolutePath());
    }

    private void disableStrips(final boolean disabled) {
        if (stripsDisabled == disabled) return;

        stripsDisabled = disabled;
        for (final int strip : getConfiguration().getAntiHowl().getMuteStrips()) {
            Voicemeeter.setParameterFloat("Strip(" + strip + ").B1", disabled ? 0 : 1);
        }
    }

    private void muteStrip(final int stripId, final boolean muted) {
        final Boolean result = muteMap.put(stripId, muted);

        // Only consider spamming vm when there's change
        if (result == null || result != muted) {
            Voicemeeter.setParameterFloat("Strip(" + stripId + ").Mute", muted ? 1 : 0);
            stripMuteListeners.get(stripId).forEach(consumer -> consumer.accept(muted));
        }
    }
}

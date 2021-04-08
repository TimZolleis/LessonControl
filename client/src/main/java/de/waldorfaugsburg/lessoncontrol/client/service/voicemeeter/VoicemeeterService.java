package de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter;

import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.common.service.VoicemeeterServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;

import java.io.File;
import java.util.concurrent.ScheduledFuture;

public final class VoicemeeterService extends AbstractService<VoicemeeterServiceConfiguration> {

    private final Voicemeeter voicemeeter;

    private ScheduledFuture<?> antiHowlTask;
    private boolean antiHowlEnabled;
    private boolean antiHowlMute;

    public VoicemeeterService(final VoicemeeterServiceConfiguration configuration) {
        super(configuration);

        this.voicemeeter = new Voicemeeter(this);
    }

    @Override
    public void enable() {
        voicemeeter.login();
        voicemeeter.runVoicemeeter();
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ignored) {
        }

        voicemeeter.areParametersDirty();

        setAntiHowlEnabled(getConfiguration().getAntiHowl().isEnabled());
    }

    @Override
    public void disable() {
        setAntiHowlEnabled(false);
        voicemeeter.logout();
    }

    public boolean isAntiHowlEnabled() {
        return antiHowlEnabled;
    }

    public void setAntiHowlEnabled(final boolean antiHowlEnabled) {
        this.antiHowlEnabled = antiHowlEnabled;

        if (antiHowlEnabled) {
            if (antiHowlTask != null) return;

            runAntiHowlTask();
        } else {
            antiHowlTask.cancel(true);
            muteStrips(false);
        }
    }

    public Voicemeeter getVoicemeeter() {
        return voicemeeter;
    }

    private void loadConfig() {
        final File file = new File(getConfiguration().getConfigPath());
        if (!file.exists()) throw new IllegalStateException("Config is missing");

        voicemeeter.setParameterString("Command.Load", file.getAbsolutePath());
    }

    private void runAntiHowlTask() {
        antiHowlTask = Scheduler.schedule(() -> {
            if (!antiHowlEnabled) return;

            /*final float volume = voicemeeter.getLevel(0, getConfiguration().getAntiHowl().getMonitoredChannel()) * 230;
            if (volume >= 5 && !antiHowlMute) {
                muteStrips(true);
                antiHowlMute = true;
            } else if (antiHowlMute && volume < 4) {
                muteStrips(false);
                antiHowlMute = false;
            }*/
        }, 10);
    }

    private void muteStrips(final boolean muted) {
        for (final int strip : getConfiguration().getAntiHowl().getMuteStrips()) {
            voicemeeter.setParameterFloat("Strip(" + strip + ").Mute", muted ? 1 : 0);
        }
    }
}

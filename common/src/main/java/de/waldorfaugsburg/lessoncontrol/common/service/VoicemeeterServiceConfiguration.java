package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public final class VoicemeeterServiceConfiguration extends AbstractServiceConfiguration {
    private String configPath;
    private NoiseReduction noiseReduction;
    private AntiHowl antiHowl;

    @NoArgsConstructor
    @Getter
    public static final class NoiseReduction {
        private boolean enabled;

        private int[] channels;
        private int[] strips;
        private int threshold;
    }

    @NoArgsConstructor
    @Getter
    public static final class AntiHowl {
        private boolean enabled;

        private int[] monitoredChannels;
        private int[] muteStrips;
        private int releaseTime;
    }
}

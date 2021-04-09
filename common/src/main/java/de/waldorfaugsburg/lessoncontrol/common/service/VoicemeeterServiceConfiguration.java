package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public final class VoicemeeterServiceConfiguration extends AbstractServiceConfiguration {
    private String configPath;
    private AntiHowl antiHowl;

    @NoArgsConstructor
    @Getter
    public static final class AntiHowl {
        private boolean enabled;
        private int monitoredChannel;
        private int[] muteStrips;
    }
}

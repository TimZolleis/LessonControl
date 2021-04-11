package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public final class VoicemeeterServiceConfiguration extends AbstractServiceConfiguration {
    private String configPath;
    private Set<String> monitoredDevices;
    private AntiHowl antiHowl;

    @NoArgsConstructor
    @Getter
    public static final class AntiHowl {
        private boolean enabled;
        private int monitoredChannel;
        private int[] muteStrips;
    }
}

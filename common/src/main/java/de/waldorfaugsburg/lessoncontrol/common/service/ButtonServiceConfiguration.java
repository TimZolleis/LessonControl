package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public final class ButtonServiceConfiguration extends AbstractServiceConfiguration {
    private Set<StripButton> stripButtons;
    private Set<ProfileButton> profileButtons;
    private boolean changeOpacity;

    @NoArgsConstructor
    @Getter
    public static final class StripButton {
        private String label;
        private int strip;
        private boolean enabled;
    }

    @NoArgsConstructor
    @Getter
    public static final class ProfileButton {
        private String label;
        private float bass;
        private float medium;
        private float high;
        private float gain;
    }
}

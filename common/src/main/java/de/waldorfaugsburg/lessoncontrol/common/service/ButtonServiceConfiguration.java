package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public final class ButtonServiceConfiguration extends AbstractServiceConfiguration {
    private Set<StripButton> stripButtons;

    @NoArgsConstructor
    @Getter
    public static final class StripButton {
        private String label;
        private int strip;
        private boolean enabled;
    }
}

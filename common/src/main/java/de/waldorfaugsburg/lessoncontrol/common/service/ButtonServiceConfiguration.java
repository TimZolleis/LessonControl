package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public final class ButtonServiceConfiguration extends AbstractServiceConfiguration {
    private int rows;
    private int columns;
    private Set<Button> buttons;

    @NoArgsConstructor
    @Getter
    public static final class Button {
        private String text;
        private int textSize;
        private boolean enabled;
    }
}

package de.waldorfaugsburg.lessoncontrol.server.util;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public final class CustomPromptProvider implements PromptProvider {

    @Override
    public AttributedString getPrompt() {
        return new AttributedString(">", AttributedStyle.DEFAULT);
    }
}
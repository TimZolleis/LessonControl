package de.waldorfaugsburg.lessoncontrol.server.util;

import org.jline.reader.impl.history.DefaultHistory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public final class NoSaveHistory extends DefaultHistory {
    @Override
    public void save() throws IOException {

    }
}
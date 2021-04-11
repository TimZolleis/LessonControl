package de.waldorfaugsburg.lessoncontrol.client.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public final class CommandExecutionUtil {

    private CommandExecutionUtil() {
    }

    public static void run(final String... args) {
        run(null, args);
    }

    public static void run(final File workingDirectory, final String... args) {
        try {
            final ProcessBuilder builder = new ProcessBuilder(args);
            if (workingDirectory != null) {
                builder.directory(workingDirectory);
            }
            builder.start();
        } catch (final IOException e) {
            log.error("An error occured running command", e);
        }
    }
}

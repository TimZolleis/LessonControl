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
            final Process process = builder.start();
            final String command = process.info().command().orElse(null);
            if (command != null) {
                log.info("Started sub-process with PID {} ({})", process.pid(), command);
            }
        } catch (final IOException e) {
            log.error("An error occurred running command", e);
        }
    }
}

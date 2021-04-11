package de.waldorfaugsburg.lessoncontrol.server.command;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Server Commands")
public final class ServerCommands {

    @ShellMethod("Stopping the server")
    public void shutdown() {
        System.exit(0);
    }
}

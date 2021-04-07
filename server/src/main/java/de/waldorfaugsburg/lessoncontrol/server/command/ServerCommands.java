package de.waldorfaugsburg.lessoncontrol.server.command;

import de.waldorfaugsburg.lessoncontrol.server.device.DeviceService;
import de.waldorfaugsburg.lessoncontrol.server.profile.ProfileService;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Server Commands")
public final class ServerCommands {

    private final ProfileService profileService;

    public ServerCommands(final ProfileService profileService) {
        this.profileService = profileService;
    }

    @ShellMethod("Stopping the server")
    public void shutdown() {
        System.exit(0);
    }

    @ShellMethod("Reloads configurations")
    public void reload() {
        profileService.loadFromConfiguration();
    }
}

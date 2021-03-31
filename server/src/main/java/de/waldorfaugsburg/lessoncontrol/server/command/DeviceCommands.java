package de.waldorfaugsburg.lessoncontrol.server.command;

import de.waldorfaugsburg.lessoncontrol.server.device.Device;
import de.waldorfaugsburg.lessoncontrol.server.device.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Collection;

@ShellComponent
@Slf4j
@ShellCommandGroup("Device Commands")
public class DeviceCommands {

    private final DeviceService service;

    public DeviceCommands(final DeviceService service) {
        this.service = service;
    }

    @ShellMethod(key = "devices list", value = "Lists all devices")
    public void list() {
        final Collection<Device> devices = service.getDevices();
        log.info("All devices ({}):", devices.size());
        for (final Device device : devices) {
            log.info(" * {}", device.getName());
            log.info("  - State: {}", device.getState());
            log.info("  - Load: {}", device.getCpuUsage());
            log.info("  - Memory: {}/{}", device.getUsedMemory(), device.getTotalMemory());
        }
    }
}
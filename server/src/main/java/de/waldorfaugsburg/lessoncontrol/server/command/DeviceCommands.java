package de.waldorfaugsburg.lessoncontrol.server.command;

import de.waldorfaugsburg.lessoncontrol.server.device.Device;
import de.waldorfaugsburg.lessoncontrol.server.device.DeviceService;
import de.waldorfaugsburg.lessoncontrol.server.device.DeviceState;
import de.waldorfaugsburg.lessoncontrol.server.util.FormatUtil;
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
            if (device.getState() == DeviceState.ONLINE) {
                log.info("  - Load: {}", device.getLoad());
                log.info("  - Memory: {}/{}", FormatUtil.formatMemory(device.getTotalMemory() - device.getFreeMemory()), FormatUtil.formatMemory(device.getTotalMemory()));
            }
        }
    }
}
package de.waldorfaugsburg.lessoncontrol.server.command;

import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import de.waldorfaugsburg.lessoncontrol.server.device.Device;
import de.waldorfaugsburg.lessoncontrol.server.device.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ShellComponent
@Slf4j
@ShellCommandGroup("Device Commands")
public final class DeviceCommands {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final int GB = 1024 * 1024 * 1024;

    private final DeviceService service;

    public DeviceCommands(final DeviceService service) {
        this.service = service;
    }

    @ShellMethod(key = "devices", value = "Lists all devices")
    public String list() {
        final List<Device> devices = new ArrayList<>(service.getDevices());
        devices.sort(Comparator.comparing(Device::getName));

        log.info("All devices ({}):", devices.size());
        final AsciiTable table = new AsciiTable(new AT_Context().setWidth(150));
        table.addRule();
        table.addRow("Name", "Location", "Profile", "Connected", "CPU", "Memory");
        table.addRule();
        table.setTextAlignment(TextAlignment.CENTER);
        devices.forEach(device -> {
            if (device.isConnected()) {
                table.addRow(device.getName(),
                        device.getInfo().getLocation(),
                        device.getInfo().getProfile(),
                        "YES (" + DateFormatUtils.format(device.getConnectedAt(), "dd.MM. HH:mm") + ")",
                        DECIMAL_FORMAT.format(device.getLoad()) + " %",
                        formatMemory(device.getTotalMemory(), device.getFreeMemory()));
            } else {
                table.addRow(device.getName(), device.getInfo().getLocation(), device.getInfo().getProfile(), "NO", "", "");
            }
            table.addRule();
        });
        return table.render();
    }

    private String formatMemory(final long totalMemory, final long freeMemory) {
        return ((totalMemory - freeMemory) / GB) + " GB / " + (totalMemory / GB) + " GB";
    }
}
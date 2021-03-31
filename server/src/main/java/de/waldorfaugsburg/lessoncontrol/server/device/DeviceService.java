package de.waldorfaugsburg.lessoncontrol.server.device;

import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public final class DeviceService {

    private final DeviceConfiguration configuration;
    private final Map<String, Device> deviceMap = new HashMap<>();

    public DeviceService(final DeviceConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    private void init() {
        for (final DeviceConfiguration.DeviceInfo info : configuration.getDevices()) {
            deviceMap.put(info.getName(), new Device(info));
            log.info("Registered device '{}'", info.getName());
        }
    }

    public Device getDevice(final String name) {
        return deviceMap.get(name);
    }

    public Collection<Device> getDevices() {
        return deviceMap.values();
    }
}

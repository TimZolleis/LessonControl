package de.waldorfaugsburg.lessoncontrol.server.device;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import de.waldorfaugsburg.lessoncontrol.common.event.EventDistributor;
import de.waldorfaugsburg.lessoncontrol.common.network.server.DenyPacket;
import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.network.NetworkListener;
import de.waldorfaugsburg.lessoncontrol.server.profile.Profile;
import de.waldorfaugsburg.lessoncontrol.server.profile.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public final class DeviceService {

    private final Gson gson;
    private final ProfileService profileService;
    private final EventDistributor eventDistributor;
    private final Map<String, Device> deviceMap = new HashMap<>();

    private DeviceConfiguration configuration;

    public DeviceService(final Gson gson, final ProfileService profileService, final EventDistributor eventDistributor) {
        this.gson = gson;
        this.profileService = profileService;
        this.eventDistributor = eventDistributor;
    }

    @PostConstruct
    private void loadFromConfiguration() {
        // Parse 'devices.json'
        try (final JsonReader reader = new JsonReader(new BufferedReader(new FileReader("devices.json")))) {
            configuration = gson.fromJson(reader, DeviceConfiguration.class);
        } catch (final IOException e) {
            log.error("An error occurred while reading devices", e);
        }

        // Create devices from infos
        deviceMap.clear();
        for (final DeviceConfiguration.DeviceInfo info : configuration.getDevices()) {
            deviceMap.put(info.getName(), new Device(info));
            log.info("Registered device '{}'", info.getName());
        }

        // Add network event listener
        eventDistributor.addListener(NetworkListener.class, (connection, packet) -> {
            // Deny device if unknown
            final Device device = deviceMap.get(packet.getName());
            if (device == null) {
                connection.deny(DenyPacket.Reason.UNKNOWN_DEVICE, "Device '" + packet.getName() + "' is unknown");
                return;
            }

            // Accept client
            device.handleConnect(connection, packet);
            connection.accept(device);

            // Find matching profile
            final DeviceConfiguration.DeviceInfo deviceInfo = device.getInfo();
            Profile profile = profileService.getProfile(deviceInfo.getProfile());
            if (profile == null) {
                profile = profileService.getDefaultProfile();
                log.warn("Device '{}' requested unknown profile '{}' - using '{}' as default",
                        device.getName(), deviceInfo.getProfile(), profile.getName());
            }

            // Transferring profile
            profileService.transferProfile(device, profile);
        });
    }

    public Collection<Device> getDevices() {
        return deviceMap.values();
    }
}

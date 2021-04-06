package de.waldorfaugsburg.lessoncontrol.server.device;

import de.waldorfaugsburg.lessoncontrol.common.network.client.RegisterPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.DenyPacket;
import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.network.DeviceConnection;
import de.waldorfaugsburg.lessoncontrol.server.network.NetworkServer;
import de.waldorfaugsburg.lessoncontrol.server.network.NetworkServerListener;
import de.waldorfaugsburg.lessoncontrol.server.profile.Profile;
import de.waldorfaugsburg.lessoncontrol.server.profile.ProfileService;
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
    private final NetworkServer networkServer;
    private final ProfileService profileService;

    private final Map<String, Device> deviceMap = new HashMap<>();

    public DeviceService(final DeviceConfiguration configuration, final NetworkServer networkServer, final ProfileService profileService) {
        this.configuration = configuration;
        this.networkServer = networkServer;
        this.profileService = profileService;
    }

    @PostConstruct
    private void init() {
        for (final DeviceConfiguration.DeviceInfo info : configuration.getDevices()) {
            deviceMap.put(info.getName(), new Device(info));
            log.info("Registered device '{}'", info.getName());
        }

        networkServer.addListener(new NetworkServerListener() {
            @Override
            public void register(final DeviceConnection connection, final RegisterPacket packet) {
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
            }
        });
    }

    public Collection<Device> getDevices(){
        return deviceMap.values();
    }
}

package de.waldorfaugsburg.lessoncontrol.server.profile;

import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferFileChunkPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferProfilePacket;
import de.waldorfaugsburg.lessoncontrol.server.config.ProfileConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.device.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public final class ProfileService {

    private final ProfileConfiguration configuration;
    private final Map<String, Profile> profileMap = new HashMap<>();

    public ProfileService(final ProfileConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    private void init() {
        for (final ProfileConfiguration.ProfileInfo info : configuration.getProfiles()) {
            profileMap.put(info.getName(), new Profile(configuration, info));
            log.info("Registered profile '{}'", info.getName());
        }
    }

    public void transferProfile(final Device device, final Profile profile) {
        final int chunkCount = profile.getDataChunks().length;
        device.getConnection().sendTCP(new TransferProfilePacket(chunkCount));
        for (final byte[] chunk : profile.getDataChunks()) {
            device.getConnection().sendTCP(new TransferFileChunkPacket(chunk));
        }
        log.info("Transferred '{}' chunks to '{}'", chunkCount, device.getName());
    }

    public Profile getProfile(final String name) {
        return profileMap.get(name);
    }

    public Profile getDefaultProfile() {
        return getProfile(configuration.getDefaultProfile());
    }

    public Collection<Profile> getProfiles() {
        return profileMap.values();
    }
}

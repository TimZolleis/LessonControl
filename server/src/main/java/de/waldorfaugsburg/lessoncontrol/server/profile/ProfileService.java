package de.waldorfaugsburg.lessoncontrol.server.profile;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferFileChunkPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferProfilePacket;
import de.waldorfaugsburg.lessoncontrol.server.config.ProfileConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.device.Device;
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
public final class ProfileService {

    private final Gson gson;
    private final Map<String, Profile> profileMap = new HashMap<>();

    private ProfileConfiguration configuration;

    public ProfileService(final Gson gson) {
        this.gson = gson;
    }

    @PostConstruct
    public void loadFromConfiguration() {
        // Parsing 'profiles.json'
        try (final JsonReader reader = new JsonReader(new BufferedReader(new FileReader("profiles.json")))) {
            configuration = gson.fromJson(reader, ProfileConfiguration.class);
        } catch (final IOException e) {
            log.error("An error occurred while reading profiles", e);
        }

        // Create profiles from infos
        profileMap.clear();
        for (final ProfileConfiguration.ProfileInfo info : configuration.getProfiles()) {
            final Profile profile = new Profile(configuration, info);
            profileMap.put(info.getName(), profile);
            log.info("Registered profile '{}' ({} chunk(s) generated)", info.getName(), profile.getDataChunks().length);
        }
    }

    public void transferProfile(final Device device, final Profile profile) {
        final int chunkCount = profile.getDataChunks().length;
        device.getConnection().sendTCP(new TransferProfilePacket(profile.getInfo().getConfigurations(), chunkCount));
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

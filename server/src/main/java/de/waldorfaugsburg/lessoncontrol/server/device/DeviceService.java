package de.waldorfaugsburg.lessoncontrol.server.device;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import de.waldorfaugsburg.lessoncontrol.common.event.EventDistributor;
import de.waldorfaugsburg.lessoncontrol.common.network.server.DenyPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferFileChunkPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferProfilePacket;
import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.config.ServerConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.network.NetworkListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public final class DeviceService {

    private final Gson gson;
    private final ServerConfiguration configuration;
    private final EventDistributor eventDistributor;
    private final Map<String, Device> deviceMap = new HashMap<>();

    public DeviceService(final Gson gson, final ServerConfiguration configuration, final EventDistributor eventDistributor) {
        this.gson = gson;
        this.configuration = configuration;
        this.eventDistributor = eventDistributor;
    }

    @PostConstruct
    private void loadFromConfiguration() {
        final File devicesFolder = new File(configuration.getDevicesFolder());
        final File[] deviceConfigurationFiles = devicesFolder.listFiles();
        if (deviceConfigurationFiles == null) return;

        deviceMap.clear();

        for (final File file : deviceConfigurationFiles) {
            if (!file.getName().endsWith(".json")) continue;

            try (final JsonReader reader = new JsonReader(new BufferedReader(new FileReader(file)))) {
                final DeviceConfiguration deviceConfiguration = gson.fromJson(reader, DeviceConfiguration.class);
                deviceMap.put(deviceConfiguration.getName(), new Device(configuration, deviceConfiguration));
                log.info("Registered device '{}'", deviceConfiguration.getName());
            } catch (final IOException e) {
                log.error("An error occurred while reading '{}'", file.getName(), e);
            }
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

            // Transfer device data
            final int chunkCount = device.getDataChunks().length;
            device.getConnection().sendTCP(new TransferProfilePacket(device.getConfiguration().getServices(), chunkCount));
            for (final byte[] chunk : device.getDataChunks()) {
                device.getConnection().sendTCP(new TransferFileChunkPacket(chunk));
                busyWaitMicros(50);
            }
            log.info("Transferred '{}' chunks to '{}'", chunkCount, device.getName());
        });
    }

    private void busyWaitMicros(final long micros) {
        final long waitUntil = System.nanoTime() + (micros * 1_000);
        while (waitUntil > System.nanoTime()) {
        }
    }

    public Collection<Device> getDevices() {
        return deviceMap.values();
    }
}

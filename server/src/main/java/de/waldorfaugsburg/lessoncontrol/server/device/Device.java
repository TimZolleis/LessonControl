package de.waldorfaugsburg.lessoncontrol.server.device;

import de.waldorfaugsburg.lessoncontrol.common.network.Network;
import de.waldorfaugsburg.lessoncontrol.common.network.client.RegisterPacket;
import de.waldorfaugsburg.lessoncontrol.server.config.DeviceConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.network.DeviceConnection;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public final class Device {

    private final DeviceConfiguration configuration;
    private final DeviceConfiguration.DeviceInfo info;

    private DeviceConnection connection;
    private long connectedAt;
    private long totalMemory;
    private long freeMemory;
    private double load;
    private byte[][] dataChunks;

    public Device(final DeviceConfiguration configuration, final DeviceConfiguration.DeviceInfo info) {
        this.configuration = configuration;
        this.info = info;
        convertAndCacheFiles();
    }

    public void handleConnect(final DeviceConnection connection, final RegisterPacket packet) {
        this.totalMemory = packet.getTotalMemory();
        this.connectedAt = System.currentTimeMillis();

        // We're connected after setting this field
        this.connection = connection;
    }

    public void updateSystemResources(final long freeMemory, final double load) {
        this.freeMemory = freeMemory;
        this.load = load;
    }

    public void handleDisconnect() {
        // We disconnect by nulling connection; then reset data fields
        this.connection = null;

        this.totalMemory = this.freeMemory = 0;
        this.load = 0D;
    }

    private void convertAndCacheFiles() {
        try {
            byte[] buffer = new byte[1024];
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ZipOutputStream stream = new ZipOutputStream(byteArrayOutputStream);
            for (final Map.Entry<String, String> entry : info.getFiles().entrySet()) {
                final File file = new File(configuration.getFilesFolder() + entry.getKey());
                writeFilesRecursively(file, entry.getValue(), buffer, stream);
            }
            stream.close();
            final byte[] data = byteArrayOutputStream.toByteArray();
            final int chunkCount = (int) Math.ceil((float) data.length / Network.FILE_CHUNK_SIZE);
            this.dataChunks = new byte[chunkCount][];

            int bytesLeft = data.length;
            for (int i = 0; i < chunkCount; i++) {
                final int size;
                final int afterChunk = bytesLeft - Network.FILE_CHUNK_SIZE;
                if (afterChunk > 0) {
                    size = Network.FILE_CHUNK_SIZE;
                    bytesLeft = afterChunk;
                } else {
                    size = bytesLeft;
                    bytesLeft = 0;
                }
                dataChunks[i] = new byte[size];
                for (int b = 0; b < Network.FILE_CHUNK_SIZE; b++) {
                    final int index = (i * Network.FILE_CHUNK_SIZE) + b;
                    if (index >= data.length) return;

                    dataChunks[i][b] = data[index];
                }
            }
        } catch (final IOException e) {
            log.error("An error occurred while zipping files", e);
        }
    }

    private void writeFilesRecursively(final File file, final String path, final byte[] buffer, final ZipOutputStream stream) throws IOException {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files == null) return;

            for (final File child : files) {
                writeFilesRecursively(child, path.isEmpty() ? child.getName() : path + File.separator + child.getName(), buffer, stream);
            }
        } else {
            try (final FileInputStream fileInputStream = new FileInputStream(file)) {
                stream.putNextEntry(new ZipEntry(path));
                int length;
                while ((length = fileInputStream.read(buffer)) > 0) {
                    stream.write(buffer, 0, length);
                }
                stream.closeEntry();
            }
        }
    }

    public String getName() {
        return info.getName();
    }

    public DeviceConfiguration.DeviceInfo getInfo() {
        return info;
    }

    public DeviceConnection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        return connection != null;
    }

    public long getConnectedAt() {
        return connectedAt;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public double getLoad() {
        return load;
    }

    public byte[][] getDataChunks() {
        return dataChunks;
    }
}

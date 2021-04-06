package de.waldorfaugsburg.lessoncontrol.client.profile;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkClient;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkState;
import de.waldorfaugsburg.lessoncontrol.common.network.Network;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferFileChunkPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferProfilePacket;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public final class ProfileManager {

    private final LessonControlClientApplication application;

    private byte[][] fileChunks;
    private int currentChunk;

    public ProfileManager(final LessonControlClientApplication application) {
        this.application = application;

        registerReceivers();
    }

    private void registerReceivers() {
        final NetworkClient networkClient = application.getNetworkClient();

        networkClient.getDistributor().addReceiver(TransferProfilePacket.class, (connection, packet) -> {
            if (networkClient.getState() != NetworkState.READY) {
                log.error("Client isn't ready yet to receive profile");
                return;
            }

            fileChunks = new byte[packet.getFileChunkCount()][Network.FILE_CHUNK_SIZE];
        });
        networkClient.getDistributor().addReceiver(TransferFileChunkPacket.class, (connection, packet) -> {
            if (fileChunks == null) {
                log.error("Client isn't ready yet to receive file chunks");
                return;
            }

            fileChunks[currentChunk] = packet.getChunk();
            if (++currentChunk == fileChunks.length) {
                assembleAndWriteChunks();
                currentChunk = 0;
            }
        });
    }

    private void assembleAndWriteChunks() {
        int totalLength = 0;
        for (byte[] chunk : fileChunks) {
            totalLength += chunk.length;
        }
        final byte[] data = new byte[totalLength];
        for (int i = 0; i < fileChunks.length; i++) {
            final byte[] chunk = fileChunks[i];
            System.arraycopy(chunk, 0, data, i * Network.FILE_CHUNK_SIZE, chunk.length);
        }

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        final byte[] buffer = new byte[1024];

        try (final ZipInputStream stream = new ZipInputStream(byteArrayInputStream)) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                final File file = new File(entry.getName());
                final Path path = file.toPath();
                if (file.exists()) Files.delete(path);
                Files.createDirectories(path.getParent());
                Files.createFile(path);

                try (final FileOutputStream outputStream = new FileOutputStream(file)) {
                    int length;
                    while ((length = stream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            }
            final int length = fileChunks.length;
            fileChunks = null;

            log.info("Received, assembled and wrote '{}' chunks with a total of '{} bytes'", length, totalLength);
        } catch (final IOException e) {
            log.error("An error occurred while unzipping profile", e);
        }
    }
}

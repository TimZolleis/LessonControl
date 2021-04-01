package de.waldorfaugsburg.lessoncontrol.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import de.waldorfaugsburg.lessoncontrol.client.config.ClientConfiguration;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkClient;
import de.waldorfaugsburg.lessoncontrol.client.tray.TrayManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

@Slf4j
public final class LessonControlClientApplication {

    private final File applicationDirectory = new File(System.getenv("APPDATA"), "LessonControl");
    private final Gson gson = new GsonBuilder().create();

    private String machineName;
    private ClientConfiguration configuration;

    private NetworkClient networkClient;
    private TrayManager trayManager;

    public void enable() {
        try {
            machineName = InetAddress.getLocalHost().getHostName();
        } catch (final IOException e) {
            log.error("An error occurred while retrieving machine name", e);
        }

        try {
            configuration = parseConfiguration();
        } catch (final IOException e) {
            log.error("An error occurred while parsing configuration", e);
            System.exit(1);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {
            log.error("An error occurred while setting look and feel", e);
        }

        // Initialize services
        networkClient = new NetworkClient(this);
        trayManager = new TrayManager(this);

        networkClient.connect();
    }

    public void disable() {

    }

    public void fatalError(final String title, final String message) {
        ((Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation")).run();
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, message + "\n\nBitte wenden Sie sich umgehend an Fachpersonal!", title, JOptionPane.ERROR_MESSAGE));
    }

    public File getApplicationDirectory() {
        return applicationDirectory;
    }

    public File getApplicationDirectoryFile(final String child) {
        return new File(applicationDirectory, child);
    }

    public Gson getGson() {
        return gson;
    }

    public String getMachineName() {
        return machineName;
    }

    public ClientConfiguration getConfiguration() {
        return configuration;
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    public TrayManager getTrayManager() {
        return trayManager;
    }

    private ClientConfiguration parseConfiguration() throws IOException {
        try (final JsonReader reader = new JsonReader(new BufferedReader(new FileReader("config.json")))) {
            return gson.fromJson(reader, ClientConfiguration.class);
        }
    }
}

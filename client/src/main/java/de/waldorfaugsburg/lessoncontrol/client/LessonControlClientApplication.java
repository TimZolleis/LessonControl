package de.waldorfaugsburg.lessoncontrol.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import de.waldorfaugsburg.lessoncontrol.client.config.ClientConfiguration;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkClient;
import de.waldorfaugsburg.lessoncontrol.client.profile.ProfileManager;
import de.waldorfaugsburg.lessoncontrol.client.service.ServiceManager;
import de.waldorfaugsburg.lessoncontrol.client.splash.SplashManager;
import de.waldorfaugsburg.lessoncontrol.client.tray.TrayManager;
import de.waldorfaugsburg.lessoncontrol.common.event.EventDistributor;
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

    private final EventDistributor eventDistributor = new EventDistributor();

    private NetworkClient networkClient;
    private ProfileManager profileManager;
    private ServiceManager serviceManager;
    private TrayManager trayManager;
    private SplashManager splashManager;

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
        profileManager = new ProfileManager(this);
        serviceManager = new ServiceManager(this);
        trayManager = new TrayManager(this);
        splashManager = new SplashManager(this);

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

    public EventDistributor getEventDistributor() {
        return eventDistributor;
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public TrayManager getTrayManager() {
        return trayManager;
    }

    public SplashManager getSplashManager() {
        return splashManager;
    }

    private ClientConfiguration parseConfiguration() throws IOException {
        try (final JsonReader reader = new JsonReader(new BufferedReader(new FileReader("config.json")))) {
            return gson.fromJson(reader, ClientConfiguration.class);
        }
    }
}

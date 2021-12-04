package de.waldorfaugsburg.lessoncontrol.client.tray;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkListener;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

@Slf4j
public final class TrayManager {

    private final LessonControlClientApplication application;
    private final Image defaultImage;
    private final Image errorImage;

    private TrayIcon trayIcon;

    public TrayManager(final LessonControlClientApplication application) {
        this.application = application;
        this.defaultImage = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/tray.png"));
        this.errorImage = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/tray_red.png"));

        initialize();
    }

    private void initialize() {
        trayIcon = new TrayIcon(defaultImage);
        trayIcon.setImageAutoSize(true);

        final PopupMenu menu = new PopupMenu();
        final MenuItem statusItem = new MenuItem();
        statusItem.setEnabled(false);
        menu.add(statusItem);
        menu.addSeparator();
        menu.add(createItem("Log anzeigen", e -> showLog()));
        menu.add(createItem("Erneut verbinden", e -> application.getNetworkClient().connect()));
        menu.add(createItem("Beenden", e -> System.exit(0)));
        trayIcon.setPopupMenu(menu);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (final AWTException e) {
            log.error("An error occurred while adding tray icon", e);
        }

        application.getEventDistributor().addListener(NetworkListener.class, (previousState, state) -> {
            if (!previousState.isRed() && state.isRed()) {
                trayIcon.setImage(errorImage);
            } else if(previousState.isRed() && !state.isRed()) {
                trayIcon.setImage(defaultImage);
            }

            statusItem.setLabel(state.getMessage());
            trayIcon.setToolTip("LessonControl\n" + state.getMessage());
        });
    }

    private MenuItem createItem(final String label, final ActionListener listener) {
        final MenuItem item = new MenuItem(label);
        item.addActionListener(listener);
        return item;
    }

    private void showLog() {
        final File file = application.getApplicationDirectoryFile("client.log");
        if (file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (final IOException ex) {
                application.fatalError("Logdatei öffnen", "Beim Öffnen der Logdatei ist ein Fehler aufgetreten! " +
                        "(" + ex.getClass().getSimpleName() + " - " + ex.getMessage() + ")");
            }
        } else {
            application.fatalError("Logdatei öffnen", "Keine Logdatei gefunden");
        }
    }
}

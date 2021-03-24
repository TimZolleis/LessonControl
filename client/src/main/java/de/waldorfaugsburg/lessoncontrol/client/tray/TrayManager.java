package de.waldorfaugsburg.lessoncontrol.client.tray;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkState;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

@Slf4j
public final class TrayManager {

    private final LessonControlClientApplication application;
    private TrayIcon trayIcon;

    public TrayManager(final LessonControlClientApplication application) {
        this.application = application;

        initialize();
    }

    private void initialize() {
        final Image trayImage = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/icon.png"));
        trayIcon = new TrayIcon(trayImage, "LessonControl");
        trayIcon.setImageAutoSize(true);

        final PopupMenu menu = new PopupMenu();
        final MenuItem statusItem = new MenuItem("");
        statusItem.setEnabled(false);
        menu.add(statusItem);
        menu.addSeparator();
        menu.add(createItem("Log anzeigen", e -> {
            try {
                Desktop.getDesktop().open(new File(System.getenv("APPDATA") + "/LessonControl/client.log"));
            } catch (final IOException ex) {
                log.error("An error occurred while opening log", ex);
            }
        }));
        menu.add(createItem("Erneut verbinden", e -> application.getNetworkClient().connect()));
        menu.add(createItem("Beenden", e -> System.exit(0)));
        trayIcon.setPopupMenu(menu);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (final AWTException e) {
            log.error("An error occurred while adding tray icon", e);
        }

        application.getNetworkClient().addListener(state -> {
            statusItem.setLabel(state.getMessage());
            trayIcon.setToolTip("LessonControl\n" + state.getMessage());

            if (state == NetworkState.READY) {
                trayIcon.displayMessage("LessonControl", "Verbunden!", TrayIcon.MessageType.INFO);
            } else if (state == NetworkState.FATAL) {
                trayIcon.displayMessage("LessonControl", "Ein schwerwiegender Fehler ist aufgetreten!", TrayIcon.MessageType.NONE);
            }
        });
    }

    private MenuItem createItem(final String label, final ActionListener listener) {
        final MenuItem item = new MenuItem(label);
        item.addActionListener(listener);
        return item;
    }
}

package de.waldorfaugsburg.lessoncontrol.client.tray;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
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

        SwingUtilities.invokeLater(this::initialize);
    }

    private void initialize() {
        final Image trayImage = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/tray.png"));
        trayIcon = new TrayIcon(trayImage, "LessonControl");
        trayIcon.setImageAutoSize(true);

        final PopupMenu menu = new PopupMenu();
        final MenuItem statusItem = new MenuItem("");
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

        application.getNetworkClient().addListener(state -> SwingUtilities.invokeLater(() -> {
            statusItem.setLabel(state.getMessage());
            trayIcon.setToolTip("LessonControl\n" + state.getMessage());
        }));
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
                JOptionPane.showMessageDialog(null, "Beim Öffnen der Logdatei ist ein Fehler aufgetreten!",
                        "Logdatei öffnen", JOptionPane.ERROR_MESSAGE);
                log.error("An error occurred while opening log", ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Keine Logdatei gefunden!",
                    "Logdatei öffnen", JOptionPane.ERROR_MESSAGE);
        }
    }
}

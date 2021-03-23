package de.waldorfaugsburg.lessoncontrol.client.tray;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import dorkbox.systemTray.Menu;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.Separator;
import dorkbox.systemTray.SystemTray;
import dorkbox.util.Desktop;
import dorkbox.util.SwingUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public final class TrayManager {

    private final LessonControlClientApplication application;
    private final SystemTray tray;

    public TrayManager(final LessonControlClientApplication application) {
        this.application = application;
        this.tray = SystemTray.get();

        initialize();
    }

    private void initialize() {
        SwingUtil.setLookAndFeel(null);

        final Menu menu = tray.getMenu();
        menu.add(new Separator());
        menu.add(new MenuItem("Log anzeigen", e -> {
            try {
                Desktop.open(new File(System.getenv("APPDATA") + "/LessonControl/client.log"));
            } catch (final IOException ex) {
                log.error("An error occurred while opening log", ex);
            }
        })).setShortcut('e');
        menu.add(new MenuItem("Erneut verbinden", e -> application.getNetworkClient().connect()));
        menu.add(new MenuItem("Beenden", e -> {
            tray.shutdown();
            System.exit(0);
        }));

        application.getNetworkClient().addListener(state -> {
            final String message = state.getMessage();
            tray.setTooltip("LessonControl - " + message);
            tray.setStatus(message);
        });
    }

    public void disable() {
        tray.shutdown();
    }
}

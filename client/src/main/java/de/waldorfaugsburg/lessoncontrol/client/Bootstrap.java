package de.waldorfaugsburg.lessoncontrol.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

@Slf4j
public final class Bootstrap {

    private Bootstrap() {

    }

    public static void main(final String[] args) {
        final LessonControlClientApplication application = new LessonControlClientApplication();

        // Shutdown hook for doing important things on CTRL + C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            application.disable();
            Configurator.shutdown((LoggerContext) LogManager.getContext());
        }));

        // Enabling the service in separate thread
        new Thread(application::enable).start();

        // Main thread is sleeping
        try {
            synchronized (application) {
                application.wait();
            }
        } catch (final InterruptedException e) {
            log.error("An error occurred while waiting", e);
        }
    }
}

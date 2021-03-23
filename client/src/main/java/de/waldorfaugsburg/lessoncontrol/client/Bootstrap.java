package de.waldorfaugsburg.lessoncontrol.client;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Bootstrap {

    private Bootstrap() {

    }

    public static void main(final String[] args) {
        final LessonControlClientApplication application = new LessonControlClientApplication();

        // Shutdown hook for doing important things on CTRL + C
        Runtime.getRuntime().addShutdownHook(new Thread(application::disable));

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

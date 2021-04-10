package de.waldorfaugsburg.lessoncontrol.client.service;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;

public abstract class AbstractService<C extends AbstractServiceConfiguration> {

    private final LessonControlClientApplication application;
    private final C configuration;

    protected AbstractService(final LessonControlClientApplication application, final C configuration) {
        this.application = application;
        this.configuration = configuration;
    }

    public abstract void enable() throws Exception;

    public abstract void disable(boolean shutdown) throws Exception;

    public LessonControlClientApplication getApplication() {
        return application;
    }

    public C getConfiguration() {
        return configuration;
    }
}

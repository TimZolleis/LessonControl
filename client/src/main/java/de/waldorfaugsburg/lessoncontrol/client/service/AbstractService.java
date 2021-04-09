package de.waldorfaugsburg.lessoncontrol.client.service;

import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;

import java.io.IOException;

public abstract class AbstractService<C extends AbstractServiceConfiguration> {

    private final C configuration;

    protected AbstractService(final C configuration) {
        this.configuration = configuration;
    }

    public abstract void enable() throws Exception;

    public abstract void disable() throws Exception;

    public C getConfiguration() {
        return configuration;
    }
}

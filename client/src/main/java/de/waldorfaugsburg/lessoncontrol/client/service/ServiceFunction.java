package de.waldorfaugsburg.lessoncontrol.client.service;

import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;

@FunctionalInterface
public interface ServiceFunction<C extends AbstractServiceConfiguration> {
    AbstractService<C> get(C config);
}

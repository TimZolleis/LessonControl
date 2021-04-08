package de.waldorfaugsburg.lessoncontrol.client.service;

import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class ServiceFunctionRegistry {

    private static final Map<Class<? extends AbstractServiceConfiguration>, ServiceFunction<?>> SERVICE_FUNCTION_MAP = new HashMap<>();

    public static <C extends AbstractServiceConfiguration> AbstractService<C> createService(final C configuration) {
        return ((ServiceFunction<C>) SERVICE_FUNCTION_MAP.get(configuration.getClass())).get(configuration);
    }

    public static <C extends AbstractServiceConfiguration, S extends AbstractService<C>> void registerService(final Class<C> configurationClass, final ServiceFunction<C> function) {
        SERVICE_FUNCTION_MAP.put(configurationClass, function);
    }
}

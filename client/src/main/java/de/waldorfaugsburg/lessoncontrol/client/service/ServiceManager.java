package de.waldorfaugsburg.lessoncontrol.client.service;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkClient;
import de.waldorfaugsburg.lessoncontrol.client.profile.FileTransferListener;
import de.waldorfaugsburg.lessoncontrol.client.service.type.GeneralService;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferProfilePacket;
import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.service.GeneralServiceConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ServiceManager {

    private final LessonControlClientApplication application;
    private final Map<Class<?>, AbstractService<?>> serviceMap = new HashMap<>();

    private Set<AbstractServiceConfiguration> configurations;

    public ServiceManager(final LessonControlClientApplication application) {
        this.application = application;
        ServiceFunctionRegistry.registerService(GeneralServiceConfiguration.class, GeneralService::new);
        registerListeners();
    }

    private void registerListeners() {
        final NetworkClient networkClient = application.getNetworkClient();
        networkClient.getDistributor().addListener(TransferProfilePacket.class, (connection, packet) -> this.configurations = packet.getServiceConfigurations());
        application.getEventDistributor().addListener(FileTransferListener.class, this::initializeServices);
    }

    private void initializeServices() {
        for (final AbstractServiceConfiguration configuration : configurations) {
            final AbstractService<?> service = ServiceFunctionRegistry.createService(configuration);
            serviceMap.put(service.getClass(), service);
        }
    }

    public <T extends AbstractService<?>> T getService(final Class<T> clazz) {
        return (T) serviceMap.get(clazz);
    }
}

package de.waldorfaugsburg.lessoncontrol.client.service;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkClient;
import de.waldorfaugsburg.lessoncontrol.client.profile.FileTransferListener;
import de.waldorfaugsburg.lessoncontrol.client.service.button.ButtonService;
import de.waldorfaugsburg.lessoncontrol.client.service.general.GeneralService;
import de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter.VoicemeeterService;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferProfilePacket;
import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.service.ButtonServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.service.GeneralServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.service.VoicemeeterServiceConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public final class ServiceManager {

    private final LessonControlClientApplication application;
    private final Map<Class<?>, AbstractService<?>> serviceMap = new HashMap<>();

    private Set<AbstractServiceConfiguration> configurations;

    public ServiceManager(final LessonControlClientApplication application) {
        this.application = application;

        // Register services
        ServiceFunctionRegistry.registerService(GeneralServiceConfiguration.class, GeneralService::new);
        ServiceFunctionRegistry.registerService(VoicemeeterServiceConfiguration.class, VoicemeeterService::new);
        ServiceFunctionRegistry.registerService(ButtonServiceConfiguration.class, ButtonService::new);

        registerListeners();
    }

    private void registerListeners() {
        final NetworkClient networkClient = application.getNetworkClient();
        networkClient.getDistributor().addListener(TransferProfilePacket.class, (connection, packet) -> this.configurations = packet.getServiceConfigurations());
        application.getEventDistributor().addListener(FileTransferListener.class, this::initializeServices);
    }

    private void initializeServices() {
        if (!serviceMap.isEmpty()) {
            for (final AbstractService<?> service : serviceMap.values()) {
                service.disable();
                log.info("Disabled service '{}'", service.getClass().getSimpleName());
            }
        }

        for (final AbstractServiceConfiguration configuration : configurations) {
            final AbstractService<?> service = ServiceFunctionRegistry.createService(configuration);
            serviceMap.put(service.getClass(), service);

            service.enable();
            log.info("Enabled service '{}'", service.getClass().getSimpleName());
        }
    }

    public <T extends AbstractService<?>> T getService(final Class<T> clazz) {
        return (T) serviceMap.get(clazz);
    }
}

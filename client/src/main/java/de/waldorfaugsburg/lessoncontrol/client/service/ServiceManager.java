package de.waldorfaugsburg.lessoncontrol.client.service;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkClient;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkListener;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkState;
import de.waldorfaugsburg.lessoncontrol.client.profile.FileTransferListener;
import de.waldorfaugsburg.lessoncontrol.client.service.button.ButtonService;
import de.waldorfaugsburg.lessoncontrol.client.service.obs.OBSService;
import de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter.VoicemeeterService;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferProfilePacket;
import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.service.ButtonServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.service.OBSServiceConfiguration;
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
        ServiceFunctionRegistry.registerService(VoicemeeterServiceConfiguration.class, config -> new VoicemeeterService(application, config));
        ServiceFunctionRegistry.registerService(ButtonServiceConfiguration.class, config -> new ButtonService(application, config));
        ServiceFunctionRegistry.registerService(OBSServiceConfiguration.class, config -> new OBSService(application, config));

        registerListeners();
    }

    private void registerListeners() {
        final NetworkClient networkClient = application.getNetworkClient();
        networkClient.getDistributor().addListener(TransferProfilePacket.class, (connection, packet) -> this.configurations = packet.getServiceConfigurations());
        application.getEventDistributor().addListener(FileTransferListener.class, this::enableServices);
        application.getEventDistributor().addListener(NetworkListener.class, (previousState, state) -> {
            if (state == NetworkState.CONNECTING) disableServices(false);
        });
    }

    public void disableServices(final boolean shutdown) {
        for (final AbstractService<?> service : serviceMap.values()) {
            try {
                service.disable(shutdown);
                log.info("Disabled service '{}'", service.getClass().getSimpleName());
            } catch (final Exception e) {
                log.error("An error occurred disabling service '{}'", service.getClass().getSimpleName(), e);
            }
        }
        serviceMap.clear();
    }

    private void enableServices() {
        disableServices(false);

        for (final AbstractServiceConfiguration configuration : configurations) {
            final AbstractService<?> service = ServiceFunctionRegistry.createService(configuration);
            serviceMap.put(service.getClass(), service);

            try {
                service.enable();
                log.info("Enabled service '{}'", service.getClass().getSimpleName());
            } catch (final Exception e) {
                log.error("An error occurred enabling service '{}'", service.getClass().getSimpleName(), e);
            }
        }
    }

    public <T extends AbstractService<?>> T getService(final Class<T> clazz) {
        return (T) serviceMap.get(clazz);
    }
}

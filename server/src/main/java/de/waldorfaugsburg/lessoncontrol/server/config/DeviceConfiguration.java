package de.waldorfaugsburg.lessoncontrol.server.config;

import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter
public final class DeviceConfiguration implements Serializable {

    private String filesFolder;
    private Set<DeviceInfo> devices;

    @NoArgsConstructor
    @Getter
    public final static class DeviceInfo {
        private String name;
        private Set<AbstractServiceConfiguration> configurations;
        private Map<String, String> files;
    }
}

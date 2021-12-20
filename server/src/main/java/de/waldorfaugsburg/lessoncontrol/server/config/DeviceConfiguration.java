package de.waldorfaugsburg.lessoncontrol.server.config;

import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter
public final class DeviceConfiguration {
    private String name;
    private Set<AbstractServiceConfiguration> services;
    private Map<String, String> files;
}
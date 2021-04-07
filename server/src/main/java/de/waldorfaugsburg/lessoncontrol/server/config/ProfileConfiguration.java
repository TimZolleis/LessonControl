package de.waldorfaugsburg.lessoncontrol.server.config;

import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter
public final class ProfileConfiguration implements Serializable {
    private String filesFolder;
    private String defaultProfile;
    private Set<ProfileInfo> profiles;

    @NoArgsConstructor
    @Getter
    public final static class ProfileInfo {
        private String name;
        private Set<AbstractServiceConfiguration> configurations;
        private Map<String, String> files;
    }
}

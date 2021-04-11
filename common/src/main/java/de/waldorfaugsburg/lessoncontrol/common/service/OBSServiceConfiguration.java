package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class OBSServiceConfiguration extends AbstractServiceConfiguration {
    private String collection;
    private String profile;
    private Set<MonitoredCamera> monitoredCameras;

    @NoArgsConstructor
    @Getter
    public static final class MonitoredCamera {
        private String name;
        private SourceVisibility visibility;
    }

    @NoArgsConstructor
    @Getter
    public static final class SourceVisibility {
        private String scene;
        private String source;
    }
}

package de.waldorfaugsburg.lessoncontrol.server.config;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@NoArgsConstructor
@Getter
public final class DeviceConfiguration implements Serializable {

    private Set<DeviceInfo> devices;

    @NoArgsConstructor
    @Getter
    public final static class DeviceInfo {
        private String name;
        private String address;
        private String group;
    }
}

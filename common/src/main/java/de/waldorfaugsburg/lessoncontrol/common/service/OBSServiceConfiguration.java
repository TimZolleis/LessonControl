package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class OBSServiceConfiguration extends AbstractServiceConfiguration {
    private Set<String> restartingCameras;
}

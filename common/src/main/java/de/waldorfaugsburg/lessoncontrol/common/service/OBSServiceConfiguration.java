package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class OBSServiceConfiguration extends AbstractServiceConfiguration {
    private String sceneCollection;
    private DocumentCamera documentCamera;

    @NoArgsConstructor
    @Getter
    public static final class DocumentCamera {
        private String scene;
        private String source;
        private String camera;
    }
}

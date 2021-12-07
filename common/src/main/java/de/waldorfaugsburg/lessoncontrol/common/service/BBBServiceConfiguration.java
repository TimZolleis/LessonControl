package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class BBBServiceConfiguration extends AbstractServiceConfiguration {

    private String driverPath;
    private String email;
    private String password;

    private Set<BBBSession> sessions;

    @NoArgsConstructor
    @Getter
    public static final class BBBSession {
        private String name;
        private String teacherEmail;
        private String courseId;

        private int participantCount;
        private int duration;
    }

}

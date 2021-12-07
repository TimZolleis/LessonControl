package de.waldorfaugsburg.lessoncontrol.client.service.bbb;

import de.waldorfaugsburg.lessoncontrol.common.event.Listener;
import de.waldorfaugsburg.lessoncontrol.common.service.BBBServiceConfiguration;

import java.util.Set;

public interface BBBListener extends Listener {
    void onSessionsReceived(Set<BBBServiceConfiguration.BBBSession> sessions);

    void onSessionStart(BBBServiceConfiguration.BBBSession session);

    void onSessionStop();
}

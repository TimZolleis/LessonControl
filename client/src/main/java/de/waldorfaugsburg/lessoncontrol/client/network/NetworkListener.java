package de.waldorfaugsburg.lessoncontrol.client.network;

import de.waldorfaugsburg.lessoncontrol.common.event.Listener;

public interface NetworkListener extends Listener {
    void onStateChange(NetworkState state);
}

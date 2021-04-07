package de.waldorfaugsburg.lessoncontrol.server.network;

import de.waldorfaugsburg.lessoncontrol.common.event.Listener;
import de.waldorfaugsburg.lessoncontrol.common.network.client.RegisterPacket;

public interface NetworkListener extends Listener {
    void onRegister(final DeviceConnection connection, final RegisterPacket packet);
}

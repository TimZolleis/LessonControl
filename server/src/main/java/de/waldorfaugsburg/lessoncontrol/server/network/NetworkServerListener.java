package de.waldorfaugsburg.lessoncontrol.server.network;

import de.waldorfaugsburg.lessoncontrol.common.network.client.RegisterPacket;

public interface NetworkServerListener {

    default void register(final DeviceConnection connection, final RegisterPacket packet) {

    }
}

package de.waldorfaugsburg.lessoncontrol.common.network;

import com.esotericsoftware.kryonet.Connection;

public interface PacketReceiver<C extends Connection, T extends Packet> {
    void receive(C connection, T packet);
}

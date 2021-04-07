package de.waldorfaugsburg.lessoncontrol.common.network;

import com.esotericsoftware.kryonet.Connection;

public interface PacketListener<C extends Connection, T extends Packet> {
    void receive(C connection, T packet);
}

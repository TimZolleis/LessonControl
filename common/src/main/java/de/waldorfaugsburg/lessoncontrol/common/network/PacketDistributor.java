package de.waldorfaugsburg.lessoncontrol.common.network;

import com.esotericsoftware.kryonet.Connection;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public final class PacketDistributor<C extends Connection> {

    private final Multimap<Class<?>, PacketListener<C, Packet>> packetReceiverMultimap = HashMultimap.create();

    public PacketDistributor() {
    }

    public void distribute(final C connection, final Packet packet) {
        for (final PacketListener<C, Packet> receiver : packetReceiverMultimap.get(packet.getClass())) {
            receiver.receive(connection, packet);
        }
    }

    public <T extends Packet> void addListener(final Class<T> clazz, final PacketListener<C, T> receiver) {
        packetReceiverMultimap.put(clazz, (PacketListener<C, Packet>) receiver);
    }
}

package de.waldorfaugsburg.lessoncontrol.common.network;

import com.esotericsoftware.kryonet.Connection;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public final class PacketDistributor<C extends Connection> {

    private final Multimap<Class<?>, PacketReceiver<C, Packet>> packetReceiverMultimap = HashMultimap.create();

    public PacketDistributor() {
    }

    public void distribute(final C connection, final Packet packet) {
        for (final PacketReceiver<C, Packet> receiver : packetReceiverMultimap.get(packet.getClass())) {
            receiver.receive(connection, packet);
        }
    }

    public <T extends Packet> void addReceiver(final Class<T> clazz, final PacketReceiver<C, T> receiver) {
        packetReceiverMultimap.put(clazz, (PacketReceiver<C, Packet>) receiver);
    }
}

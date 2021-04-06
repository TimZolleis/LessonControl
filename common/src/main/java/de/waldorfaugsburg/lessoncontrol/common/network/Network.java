package de.waldorfaugsburg.lessoncontrol.common.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.minlog.Log;
import de.waldorfaugsburg.lessoncontrol.common.network.client.RegisterPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.client.SystemResourcesPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.AcceptPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.DenyPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferFileChunkPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.TransferProfilePacket;

public final class Network {

    public static final int PROTOCOL_VERSION = 1;
    public static final int FILE_CHUNK_SIZE = 500;

    static {
        Log.set(Log.LEVEL_ERROR);
    }

    private Network() {
    }

    public static void registerPacketClasses(final EndPoint endPoint) {
        final Kryo kryo = endPoint.getKryo();

        // General
        kryo.register(byte[].class);

        // Bound to client
        kryo.register(RegisterPacket.class);
        kryo.register(SystemResourcesPacket.class);

        // Bound to server
        kryo.register(AcceptPacket.class);
        kryo.register(DenyPacket.class);
        kryo.register(DenyPacket.Reason.class);
        kryo.register(TransferProfilePacket.class);
        kryo.register(TransferFileChunkPacket.class);
    }
}

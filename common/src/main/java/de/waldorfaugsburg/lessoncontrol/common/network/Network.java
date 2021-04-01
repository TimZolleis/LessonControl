package de.waldorfaugsburg.lessoncontrol.common.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.minlog.Log;
import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientSystemResourcesPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientRegisterPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerClientAcceptPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerClientDenyPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerTransferChunkPacket;

public final class Network {

    public static final int PROTOCOL_VERSION = 1;

    static {
        Log.set(Log.LEVEL_ERROR);
    }

    private Network() {
    }

    public static void registerPacketClasses(final EndPoint endPoint) {
        final Kryo kryo = endPoint.getKryo();
        // Bound to client
        kryo.register(ClientRegisterPacket.class);
        kryo.register(ClientSystemResourcesPacket.class);

        // Bound to server
        kryo.register(ServerClientAcceptPacket.class);
        kryo.register(ServerClientDenyPacket.class);
        kryo.register(ServerClientDenyPacket.Reason.class);
        kryo.register(ServerTransferChunkPacket.class);
    }
}

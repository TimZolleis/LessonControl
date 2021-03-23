package de.waldorfaugsburg.lessoncontrol.common.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.minlog.Log;
import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientRegisterPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerClientAcceptPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerTransferChunkPacket;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
        registerPacketClass(kryo, ClientRegisterPacket.class);

        // Bound to server
        registerPacketClass(kryo, ServerClientAcceptPacket.class);
        registerPacketClass(kryo, ServerTransferChunkPacket.class);
    }

    private static void registerPacketClass(final Kryo kryo, final Class<?> clazz) {
        kryo.register(clazz);
        for (final Field field : clazz.getDeclaredFields()) {
            registerPacketClass(kryo, field.getType());
            final Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) genericType;
                for (final Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                    if (actualTypeArgument instanceof Class<?>) {
                        final Class<?> typeArgumentClass = (Class<?>) actualTypeArgument;
                        if (!typeArgumentClass.equals(clazz)) registerPacketClass(kryo, clazz);
                    }
                }
            }
        }
        for (final Class<?> innerClass : clazz.getClasses()) {
            registerPacketClass(kryo, innerClass);
        }
    }
}

package de.waldorfaugsburg.lessoncontrol.client.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.common.network.Network;
import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import de.waldorfaugsburg.lessoncontrol.common.network.PacketDistributor;
import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientRegisterPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerClientAcceptPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerClientDenyPacket;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public final class NetworkClient {

    private final LessonControlClientApplication application;
    private final Client client;

    private final PacketDistributor<Connection> distributor = new PacketDistributor<>();
    private final Set<NetworkStateListener> stateListeners = new HashSet<>();

    private NetworkState state;
    private String lastAddress;

    public NetworkClient(final LessonControlClientApplication application) {
        this.application = application;

        // Initialize stuff
        client = new Client();
        Network.registerPacketClasses(client);
        client.addListener(new ClientListener());
        client.start();

        // Registering internal receivers
        registerReceivers();
    }

    public void connect() {
        connect(null);
    }

    private void connect(String skipAddress) {
        final List<String> addresses = application.getConfiguration().getAddresses();
        if (skipAddress != null && addresses.size() == 1)
            skipAddress = null;

        for (final String address : addresses) {
            if (skipAddress != null && skipAddress.equals(address)) {
                log.info("Skipping {} ...", skipAddress);
                continue;
            }

            try {
                changeState(NetworkState.CONNECTING);
                client.connect(2000, address, application.getConfiguration().getPort());
                changeState(NetworkState.CONNECTED);
                lastAddress = client.getRemoteAddressTCP().getHostString();
                break;
            } catch (final IOException e) {
                log.error("An error occurred while connecting to server", e);
            }
        }

        if (!client.isConnected()) {
            // Retry connection if client isn't connected yet
            connect(null);
        }
    }

    public void addListener(final NetworkStateListener listener) {
        stateListeners.add(listener);
    }

    public void removeListener(final NetworkStateListener listener) {
        stateListeners.remove(listener);
    }

    private void registerReceivers() {
        distributor.addReceiver(ServerClientAcceptPacket.class, (connection, packet) -> {
            changeState(NetworkState.READY);
        });
        distributor.addReceiver(ServerClientDenyPacket.class, (connection, packet) -> {
            changeState(NetworkState.DENIED);
            log.error("Denied by server {}! Reason: {}; Message: {}", lastAddress, packet.getReason(), packet.getMessage());
        });
    }

    private void changeState(final NetworkState state) {
        this.state = state;
        stateListeners.forEach(listener -> listener.stateChange(state));
    }

    private final class ClientListener implements Listener {

        @Override
        public void connected(final Connection connection) {
            client.sendTCP(new ClientRegisterPacket(application.getMachineName(), Network.PROTOCOL_VERSION));
            changeState(NetworkState.REGISTERED);
        }

        @Override
        public void disconnected(final Connection connection) {
            if (state != NetworkState.DENIED) Scheduler.runLater(() -> connect(lastAddress), 1000);
        }

        @Override
        public void received(final Connection connection, final Object object) {
            if (object instanceof Packet) {
                final Packet packet = (Packet) object;
                distributor.distribute(connection, packet);
            }
        }
    }
}
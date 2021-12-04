package de.waldorfaugsburg.lessoncontrol.client.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.common.network.Network;
import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import de.waldorfaugsburg.lessoncontrol.common.network.PacketDistributor;
import de.waldorfaugsburg.lessoncontrol.common.network.client.RegisterPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.AcceptPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.DenyPacket;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public final class NetworkClient {

    private final LessonControlClientApplication application;
    private final Client client;

    private final PacketDistributor<Connection> distributor = new PacketDistributor<>();

    private NetworkState state = NetworkState.UNINITIALIZED;
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

    public void sendPacket(final Packet packet) {
        client.sendTCP(packet);
    }

    private void connect(String skipAddress) {
        // There is no need to connect if were already connected or in the urge of it
        if (state == NetworkState.CONNECTED || state == NetworkState.CONNECTING)
            return;

        final List<String> addresses = application.getConfiguration().getAddresses();
        if (skipAddress != null && addresses.size() == 1)
            skipAddress = null;

        for (final String address : addresses) {
            if (skipAddress != null && skipAddress.equals(address)) {
                log.info("Skipping '{}' ...", skipAddress);
                continue;
            }

            try {
                final String[] splitAddress = address.split(":");
                if (splitAddress.length == 1) {
                    log.warn("Address '{}' invalidly formatted! (Port missing)", address);
                    changeState(NetworkState.ERROR);
                    continue;
                }

                changeState(NetworkState.CONNECTING);
                client.connect(2000, splitAddress[0], Integer.parseInt(splitAddress[1]));
                break;
            } catch (final NumberFormatException e) {
                log.error("Address '{}' invalidly formatted!", address, e);
                changeState(NetworkState.ERROR);
            } catch (final IOException e) {
                log.error("An error occurred while connecting to server", e);
                changeState(NetworkState.FAILED);
            }
        }

        if (!client.isConnected() && state != NetworkState.ERROR) {
            // Retry connection if client isn't connected yet
            Scheduler.runLater(this::connect, 1000);
        }
    }

    private void registerReceivers() {
        distributor.addListener(AcceptPacket.class, (connection, packet) -> changeState(NetworkState.READY));
        distributor.addListener(DenyPacket.class, (connection, packet) -> {
            changeState(NetworkState.ERROR);
            if (packet.getMessage().isEmpty()) {
                log.error("Denied by server '{}' (Reason: {})", lastAddress, packet.getReason());
                application.fatalError("Verbindung abgelehnt", "Die Verbindung zum LCS wurde abgelehnt!\n   Grund: " + packet.getReason());
            } else {
                log.error("Denied by server '{}' (Reason: {}; Message: {})", lastAddress, packet.getReason(), packet.getMessage());
                application.fatalError("Verbindung abgelehnt", "Die Verbindung zum LCS wurde abgelehnt!\n   Grund: " + packet.getReason() + "\n   Weitere Informationen: " + packet.getMessage());
            }
        });

        application.getEventDistributor().addListener(NetworkListener.class, (previousState, state) -> log.info("Network state is now '{}'", state));
    }

    private void changeState(final NetworkState state) {
        final NetworkState previousState = this.state;
        this.state = state;
        application.getEventDistributor().call(NetworkListener.class, listener -> listener.onStateChange(previousState, state));
    }

    private final class ClientListener implements Listener {

        @Override
        public void connected(final Connection connection) {
            client.sendTCP(new RegisterPacket(application.getMachineName(), Network.PROTOCOL_VERSION));
            lastAddress = client.getRemoteAddressTCP().getHostString();
            changeState(NetworkState.CONNECTED);
        }

        @Override
        public void disconnected(final Connection connection) {
            if (state == NetworkState.READY) Scheduler.runLater(() -> connect(lastAddress), 1000);
        }

        @Override
        public void received(final Connection connection, final Object object) {
            if (object instanceof Packet) {
                final Packet packet = (Packet) object;
                distributor.distribute(connection, packet);
            }
        }
    }

    public PacketDistributor<Connection> getDistributor() {
        return distributor;
    }

    public NetworkState getState() {
        return state;
    }
}

package de.waldorfaugsburg.lessoncontrol.server.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import de.waldorfaugsburg.lessoncontrol.common.network.Network;
import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import de.waldorfaugsburg.lessoncontrol.common.network.PacketDistributor;
import de.waldorfaugsburg.lessoncontrol.common.network.client.ClientRegisterPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerClientAcceptPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.ServerClientDenyPacket;
import de.waldorfaugsburg.lessoncontrol.server.config.ServerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

@Slf4j
@Service
public final class NetworkServer {

    private final PacketDistributor<Client> distributor = new PacketDistributor<>();

    public NetworkServer(final ServerConfiguration configuration) {
        final Server server = new Server() {
            @Override
            protected Connection newConnection() {
                return new Client();
            }
        };

        // Initialize stuff
        Network.registerPacketClasses(server);
        server.addListener(new ServerListener());
        server.start();

        // Bind address
        try {
            final InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), configuration.getPort());
            server.bind(address, null);
            log.info("TCP-Server started! Listening for connections on " + address.getHostString());
        } catch (final IOException e) {
            log.error("An error occurred while starting network server", e);
        }

        // Registering internal receivers
        registerReceivers();
    }

    private void registerReceivers() {
        distributor.addReceiver(ClientRegisterPacket.class, (client, packet) -> {
            if (packet.getProtocolVersion() != Network.PROTOCOL_VERSION) {
                client.sendTCP(new ServerClientDenyPacket(ServerClientDenyPacket.Reason.OUTDATED_CLIENT, "Use: " + Network.PROTOCOL_VERSION));
                log.error("Denied {} due to unsupported protocol version! (PV: {})", client.getClientName(), client.getProtocolVersion());
                return;
            }

            client.setClientName(packet.getName());
            client.setProtocolVersion(packet.getProtocolVersion());
            client.sendTCP(new ServerClientAcceptPacket());
            log.info("{} registered as {} (PV: {})", client.getHostString(), client.getClientName(), client.getProtocolVersion());
        });
    }

    private final class ServerListener implements Listener {

        @Override
        public void connected(final Connection connection) {
            log.info("{} is trying to connect! Waiting for registration ...", connection.getRemoteAddressTCP().getHostString());
        }

        @Override
        public void disconnected(final Connection connection) {
            log.info("{} disconnected!", ((Client) connection).getEffectiveName());
        }

        @Override
        public void received(final Connection connection, final Object object) {
            final Client client = (Client) connection;
            if (object instanceof Packet) {
                final Packet packet = (Packet) object;
                distributor.distribute(client, packet);
            }
        }
    }

    public PacketDistributor<Client> getDistributor() {
        return distributor;
    }
}

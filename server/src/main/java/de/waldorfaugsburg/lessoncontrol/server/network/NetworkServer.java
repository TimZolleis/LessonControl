package de.waldorfaugsburg.lessoncontrol.server.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import de.waldorfaugsburg.lessoncontrol.common.event.EventDistributor;
import de.waldorfaugsburg.lessoncontrol.common.network.Network;
import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import de.waldorfaugsburg.lessoncontrol.common.network.PacketDistributor;
import de.waldorfaugsburg.lessoncontrol.common.network.client.RegisterPacket;
import de.waldorfaugsburg.lessoncontrol.common.network.server.DenyPacket;
import de.waldorfaugsburg.lessoncontrol.server.config.ServerConfiguration;
import de.waldorfaugsburg.lessoncontrol.server.device.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

@Slf4j
@Service
public final class NetworkServer {

    private final ServerConfiguration configuration;
    private final EventDistributor eventDistributor;

    private final Server server = new Server() {
        @Override
        protected Connection newConnection() {
            return new DeviceConnection();
        }
    };
    private final PacketDistributor<DeviceConnection> distributor = new PacketDistributor<>();

    public NetworkServer(final ServerConfiguration configuration, final EventDistributor eventDistributor) {
        this.configuration = configuration;
        this.eventDistributor = eventDistributor;
    }

    @PostConstruct
    private void init() {
        // Initialize stuff
        Network.registerPacketClasses(server);
        server.addListener(new ServerListener());
        server.start();

        // Bind address
        try {
            final InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), configuration.getPort());
            server.bind(address, null);
            log.info("TCP-Server started! Listening for connections on '{}'", address.getAddress().getHostAddress() + ":" + configuration.getPort());
        } catch (final IOException e) {
            log.error("An error occurred while starting network server", e);
        }

        // Registering internal receivers
        registerReceivers();
    }

    private void registerReceivers() {
        distributor.addListener(RegisterPacket.class, (connection, packet) -> {
            if (packet.getProtocolVersion() != Network.PROTOCOL_VERSION) {
                connection.deny(DenyPacket.Reason.OUTDATED_CLIENT, "Required protocol-version is " + Network.PROTOCOL_VERSION);
                return;
            }

            log.info("'{}' is trying to register ...", connection.getRemoteAddressTCP().getHostString());
            eventDistributor.call(NetworkListener.class, listener -> listener.onRegister(connection, packet));
        });
    }

    private final class ServerListener implements Listener {

        @Override
        public void connected(final Connection connection) {
            log.info("'{}' is trying to connect! Waiting for registration ...", connection.getRemoteAddressTCP().getHostString());
        }

        @Override
        public void disconnected(final Connection connection) {
            final DeviceConnection deviceConnection = (DeviceConnection) connection;
            final Device device = deviceConnection.getDevice();
            if (device == null) return;

            device.handleDisconnect();
            log.info("'{}' disconnected!", device.getName());
        }

        @Override
        public void received(final Connection connection, final Object object) {
            final DeviceConnection client = (DeviceConnection) connection;
            if (object instanceof Packet) {
                final Packet packet = (Packet) object;
                distributor.distribute(client, packet);
            }
        }
    }

    public PacketDistributor<DeviceConnection> getDistributor() {
        return distributor;
    }
}

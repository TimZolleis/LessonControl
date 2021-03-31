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
import de.waldorfaugsburg.lessoncontrol.server.device.Device;
import de.waldorfaugsburg.lessoncontrol.server.device.DeviceService;
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
    private final DeviceService deviceService;

    private final Server server = new Server() {
        @Override
        protected Connection newConnection() {
            return new DeviceConnection();
        }
    };
    private final PacketDistributor<DeviceConnection> distributor = new PacketDistributor<>();

    public NetworkServer(final ServerConfiguration configuration, final DeviceService deviceService) {
        this.configuration = configuration;
        this.deviceService = deviceService;
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
        distributor.addReceiver(ClientRegisterPacket.class, (connection, packet) -> {
            if (packet.getProtocolVersion() != Network.PROTOCOL_VERSION) {
                connection.sendTCP(new ServerClientDenyPacket(ServerClientDenyPacket.Reason.OUTDATED_CLIENT, "use " + Network.PROTOCOL_VERSION));
                log.error("Denied '{}' due to unsupported protocol version 'v{}'", connection.getRemoteAddressTCP().getHostString(), packet.getProtocolVersion());
                return;
            }

            final Device device = deviceService.getDevice(packet.getName());
            if (device == null) {
                connection.sendTCP(new ServerClientDenyPacket(ServerClientDenyPacket.Reason.UNKNOWN_DEVICE, ""));
                log.error("Denied '{}' due to unknown device '{}'", connection.getRemoteAddressTCP().getHostString(), packet.getName());
                return;
            }

            device.handleRegistration(connection, packet);
            connection.setDevice(device);
            connection.sendTCP(new ServerClientAcceptPacket());
            log.info("'{}' registered as '{}'", connection.getRemoteAddressTCP().getHostString(), packet.getName());
        });
    }

    private final class ServerListener implements Listener {

        @Override
        public void connected(final Connection connection) {
            log.info("'{}' is trying to connect! Waiting for registration ...", connection.getRemoteAddressTCP().getHostString());
        }

        @Override
        public void disconnected(final Connection connection) {
            log.info("'{}' disconnected!", ((DeviceConnection) connection).getDevice().getName());
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

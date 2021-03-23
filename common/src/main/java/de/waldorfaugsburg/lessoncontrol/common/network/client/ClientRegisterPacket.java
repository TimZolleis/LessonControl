package de.waldorfaugsburg.lessoncontrol.common.network.client;

import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class ClientRegisterPacket extends Packet {
    private String name;
    private int protocolVersion;
}

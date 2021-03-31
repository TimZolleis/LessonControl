package de.waldorfaugsburg.lessoncontrol.common.network.server;

import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class ServerClientDenyPacket extends Packet {
    private Reason reason;
    private String message;

    public enum Reason {
        OUTDATED_CLIENT,
        UNKNOWN_DEVICE
    }
}

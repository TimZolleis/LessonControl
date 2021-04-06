package de.waldorfaugsburg.lessoncontrol.common.network.server;

import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class TransferFileChunkPacket extends Packet {
    private byte[] chunk;
}

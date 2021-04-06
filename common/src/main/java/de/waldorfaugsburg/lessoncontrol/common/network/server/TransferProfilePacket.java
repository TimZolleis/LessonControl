package de.waldorfaugsburg.lessoncontrol.common.network.server;

import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class TransferProfilePacket extends Packet {
    private int fileChunkCount;
}

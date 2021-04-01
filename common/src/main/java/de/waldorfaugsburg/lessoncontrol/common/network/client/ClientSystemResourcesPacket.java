package de.waldorfaugsburg.lessoncontrol.common.network.client;

import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class ClientSystemResourcesPacket extends Packet {
    private long freeMemory;
    private double load;
}

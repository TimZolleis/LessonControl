package de.waldorfaugsburg.lessoncontrol.common.network.server;

import de.waldorfaugsburg.lessoncontrol.common.network.Packet;
import de.waldorfaugsburg.lessoncontrol.common.service.AbstractServiceConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class TransferProfilePacket extends Packet {
    private Set<AbstractServiceConfiguration> services;
    private int fileChunkCount;
}

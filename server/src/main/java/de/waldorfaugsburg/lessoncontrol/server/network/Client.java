package de.waldorfaugsburg.lessoncontrol.server.network;

import com.esotericsoftware.kryonet.Connection;
import lombok.Data;

@Data
public final class Client extends Connection {
    private String clientName;
    private int protocolVersion;

    public String getEffectiveName() {
        return clientName != null ? clientName : getHostString();
    }

    public String getHostString() {
        return getRemoteAddressTCP().getHostString();
    }
}

package de.waldorfaugsburg.lessoncontrol.client.config;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Getter
public final class ClientConfiguration implements Serializable {
    private List<String> addresses;
    private int port;
}

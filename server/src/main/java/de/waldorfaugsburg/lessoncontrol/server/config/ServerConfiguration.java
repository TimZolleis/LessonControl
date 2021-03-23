package de.waldorfaugsburg.lessoncontrol.server.config;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Getter
public final class ServerConfiguration implements Serializable {

    private int port;

}

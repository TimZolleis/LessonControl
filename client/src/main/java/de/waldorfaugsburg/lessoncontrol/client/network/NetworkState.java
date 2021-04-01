package de.waldorfaugsburg.lessoncontrol.client.network;

public enum NetworkState {

    CONNECTING("Verbinde ..."),
    FAILED("Fehlgeschlagen!"),
    CONNECTED("Verbunden!"),
    READY("Bereit!"),
    ERROR("Fehler!");

    private final String message;

    NetworkState(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

package de.waldorfaugsburg.lessoncontrol.client.network;

public enum NetworkState {

    CONNECTING("Verbinde ..."),
    FAILED("Verbindung fehlgeschlagen!"),
    CONNECTED("Verbunden!"),
    REGISTERED("Registriert!"),
    READY("Bereit!"),
    FATAL("Schwerwiegender Fehler!");

    private final String message;

    NetworkState(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

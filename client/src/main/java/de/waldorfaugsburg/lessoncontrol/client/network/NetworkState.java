package de.waldorfaugsburg.lessoncontrol.client.network;

public enum NetworkState {

    CONNECTING("Verbinde ..."),
    CONNECTED("Verbunden!"),
    REGISTERED("Registriert!"),
    READY("Bereit!"),
    DENIED("Verbindung abgelehnt!");

    private final String message;

    NetworkState(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

package de.waldorfaugsburg.lessoncontrol.client.network;

public enum NetworkState {

    CONNECTING("Verbinde ..."),
    FAILED("Fehlgeschlagen!"),
    CONNECTED("Verbunden! Vorbereitung läuft ..."),
    READY("Bereit!"),
    ERROR("Fehler! Bitte wenden Sie sich an den Support!");

    private final String message;

    NetworkState(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

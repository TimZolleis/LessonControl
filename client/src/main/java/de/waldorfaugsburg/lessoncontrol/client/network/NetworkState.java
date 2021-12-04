package de.waldorfaugsburg.lessoncontrol.client.network;

public enum NetworkState {

    UNINITIALIZED("...", false),
    CONNECTING("Verbinde ...", false),
    FAILED("Fehlgeschlagen!", true),
    CONNECTED("Verbunden! Vorbereitung läuft ...", false),
    READY("Bereit!", false),
    ERROR("Fehler! Bitte wenden Sie sich an den Support!", true);

    private final String message;
    private final boolean red;

    NetworkState(final String message, boolean red) {
        this.message = message;
        this.red = red;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRed() {
        return red;
    }
}

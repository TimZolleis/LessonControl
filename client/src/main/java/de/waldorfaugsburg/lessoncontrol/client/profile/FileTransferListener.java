package de.waldorfaugsburg.lessoncontrol.client.profile;

import de.waldorfaugsburg.lessoncontrol.common.event.Listener;

public interface FileTransferListener extends Listener {
    void onTransferComplete();
}

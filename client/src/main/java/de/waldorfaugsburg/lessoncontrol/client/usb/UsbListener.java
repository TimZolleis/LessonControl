package de.waldorfaugsburg.lessoncontrol.client.usb;

import de.waldorfaugsburg.lessoncontrol.common.event.Listener;
import oshi.hardware.UsbDevice;

public interface UsbListener extends Listener {
    default void devicesChecked() {

    }

    default void deviceConnected(final UsbDevice device) {

    }

    default void deviceDisconnected(final UsbDevice device) {
    }
}

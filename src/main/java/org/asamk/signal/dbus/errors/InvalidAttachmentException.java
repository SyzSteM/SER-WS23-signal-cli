package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class InvalidAttachmentException extends DBusExecutionException {

    public InvalidAttachmentException(String message) {
        super("Invalid attachment: " + message);
    }

}

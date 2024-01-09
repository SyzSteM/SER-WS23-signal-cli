package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class InvalidGroupIdException extends DBusExecutionException {

    public InvalidGroupIdException(String message) {
        super("Invalid group id: " + message);
    }

}

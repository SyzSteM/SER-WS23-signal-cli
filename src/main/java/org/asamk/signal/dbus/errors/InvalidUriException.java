package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class InvalidUriException extends DBusExecutionException {

    public InvalidUriException(String message) {
        super("Invalid uri: " + message);
    }

}

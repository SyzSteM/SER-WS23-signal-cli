package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class NumberInvalidException extends DBusExecutionException {

    public NumberInvalidException(String message) {
        super("Invalid number: " + message);
    }

}

package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class ControlNumberInvalidException extends DBusExecutionException {

    public ControlNumberInvalidException(String _message) {
        super(_message);
    }

}

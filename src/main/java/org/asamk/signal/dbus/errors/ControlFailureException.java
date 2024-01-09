package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class ControlFailureException extends DBusExecutionException {

    public ControlFailureException(final String _message) {
        super(_message);
    }

}

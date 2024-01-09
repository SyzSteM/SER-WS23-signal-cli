package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class FailureException extends DBusExecutionException {

    public FailureException(final Exception e) {
        super("Failure: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
    }

    public FailureException(final String message) {
        super("Failure: " + message);
    }

}

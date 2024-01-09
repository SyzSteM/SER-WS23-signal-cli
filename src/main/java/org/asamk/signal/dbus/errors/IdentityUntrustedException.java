package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class IdentityUntrustedException extends DBusExecutionException {

    public IdentityUntrustedException(String message) {
        super("Untrusted identity: " + message);
    }

}

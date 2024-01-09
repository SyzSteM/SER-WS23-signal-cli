package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class GroupException extends DBusExecutionException {

    public GroupException(String message) {
        super("Group exception: " + message);
    }

}

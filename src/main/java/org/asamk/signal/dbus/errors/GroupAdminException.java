package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class GroupAdminException extends DBusExecutionException {

    public GroupAdminException(String message) {
        super("Last group admin: " + message);
    }

}

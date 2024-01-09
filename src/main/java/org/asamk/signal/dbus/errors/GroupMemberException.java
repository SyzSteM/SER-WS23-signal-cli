package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class GroupMemberException extends DBusExecutionException {

    public GroupMemberException(String message) {
        super("Not a group member: " + message);
    }

}

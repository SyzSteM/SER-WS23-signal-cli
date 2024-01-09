package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class DeviceNotFoundException extends DBusExecutionException {

    public DeviceNotFoundException(String message) {
        super("Device not found: " + message);
    }

}

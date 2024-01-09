package org.asamk.signal.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class ControlRequiresCaptchaException extends DBusExecutionException {

    public ControlRequiresCaptchaException(String _message) {
        super(_message);
    }

}

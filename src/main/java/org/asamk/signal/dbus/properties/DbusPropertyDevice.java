package org.asamk.signal.dbus.properties;

import org.asamk.signal.dbus.errors.FailureException;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.interfaces.Properties;

@DBusProperty(name = "Id", type = Integer.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "Name", type = String.class)
@DBusProperty(name = "Created", type = String.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "LastSeen", type = String.class, access = DBusProperty.Access.READ)
public interface DbusPropertyDevice extends Properties {

    void removeDevice() throws FailureException;
}

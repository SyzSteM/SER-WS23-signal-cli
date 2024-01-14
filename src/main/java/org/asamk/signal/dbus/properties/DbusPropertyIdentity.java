package org.asamk.signal.dbus.properties;

import org.asamk.signal.dbus.errors.FailureException;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.interfaces.Properties;

@DBusProperty(name = "Number", type = String.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "Uuid", type = String.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "Fingerprint", type = Byte[].class, access = DBusProperty.Access.READ)
@DBusProperty(name = "SafetyNumber", type = String.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "TrustLevel", type = String.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "AddedDate", type = Integer.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "ScannableSafetyNumber", type = Byte[].class, access = DBusProperty.Access.READ)
public interface DbusPropertyIdentity extends Properties {

    void trust() throws FailureException;

    void trustVerified(String safetyNumber) throws FailureException;

}

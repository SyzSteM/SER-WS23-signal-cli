package org.asamk.signal.dbus.properties;

import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.interfaces.Properties;

@DBusProperty(name = "ReadReceipts", type = Boolean.class)
@DBusProperty(name = "UnidentifiedDeliveryIndicators", type = Boolean.class)
@DBusProperty(name = "TypingIndicators", type = Boolean.class)
@DBusProperty(name = "LinkPreviews", type = Boolean.class)
public interface DbusPropertyConfiguration extends Properties {}

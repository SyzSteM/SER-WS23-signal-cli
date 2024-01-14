package org.asamk;

import org.asamk.signal.dbus.service.AccountService;
import org.asamk.signal.dbus.service.ConfigurationService;
import org.asamk.signal.dbus.service.ContactService;
import org.asamk.signal.dbus.service.DeviceService;
import org.asamk.signal.dbus.service.GroupService;
import org.asamk.signal.dbus.service.MessageService;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * DBus interface for the org.asamk.Signal service.
 */
public interface Signal extends AutoCloseable, DBusInterface, AccountService, ConfigurationService, ContactService, DeviceService, GroupService, MessageService {

    void initObjects();

    String version();

}

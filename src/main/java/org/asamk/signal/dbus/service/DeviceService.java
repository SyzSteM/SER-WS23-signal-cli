package org.asamk.signal.dbus.service;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.InvalidUriException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.dbus.structs.DbusStructDevice;
import org.freedesktop.dbus.DBusPath;

import java.util.List;

public interface DeviceService {

    @Deprecated
    boolean isRegistered() throws FailureException, NumberInvalidException;

    boolean isRegistered(String number) throws FailureException, NumberInvalidException;

    List<Boolean> isRegistered(List<String> numbers) throws FailureException, NumberInvalidException;

    void unregister() throws FailureException;

    void addDevice(String uri) throws InvalidUriException;

    DBusPath getDevice(long deviceId);

    DBusPath getThisDevice();

    List<DbusStructDevice> listDevices() throws FailureException;

    void updateDevices();

    void unExportDevices();

}

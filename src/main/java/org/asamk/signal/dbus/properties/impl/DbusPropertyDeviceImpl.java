package org.asamk.signal.dbus.properties.impl;

import org.asamk.signal.dbus.DbusInterfacePropertiesHandler;
import org.asamk.signal.dbus.DbusProperties;
import org.asamk.signal.dbus.DbusProperty;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.properties.DbusPropertyDevice;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.Device;
import org.asamk.signal.util.Util;

import java.io.IOException;
import java.util.List;

public final class DbusPropertyDeviceImpl extends DbusProperties implements DbusPropertyDevice {

    private final Manager manager;
    private final String objectPath;
    private final Device device;

    public DbusPropertyDeviceImpl(Manager manager, String objectPath, Device device) {
        this.manager = manager;
        this.objectPath = objectPath;
        this.device = device;

        addPropertiesHandler(new DbusInterfacePropertiesHandler("org.asamk.Signal.DbusPropertyDevice",
                List.of(new DbusProperty<>("Id", device::id),
                        new DbusProperty<>("Name", () -> Util.emptyIfNull(device.name()), this::setDeviceName),
                        new DbusProperty<>("Created", device::created),
                        new DbusProperty<>("LastSeen", device::lastSeen))));
    }

    @Override
    public String getObjectPath() {
        return String.format("%s/Devices/%s", objectPath, device.id());
    }

    @Override
    public void removeDevice() throws FailureException {
        try {
            manager.removeLinkedDevices(device.id());
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

    private void setDeviceName(String name) {
        if (!device.isThisDevice()) {
            throw new FailureException("Only the name of this device can be changed");
        }
        try {
            manager.updateAccountAttributes(name);
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

}

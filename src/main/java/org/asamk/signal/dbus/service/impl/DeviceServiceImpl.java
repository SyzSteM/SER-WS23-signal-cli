package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.errors.DeviceNotFoundException;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.InvalidUriException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.dbus.properties.impl.DbusPropertyDeviceImpl;
import org.asamk.signal.dbus.service.DeviceService;
import org.asamk.signal.dbus.structs.DbusStructDevice;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.Device;
import org.asamk.signal.manager.api.DeviceLinkUrl;
import org.asamk.signal.manager.api.InvalidDeviceLinkException;
import org.asamk.signal.manager.api.NotPrimaryDeviceException;
import org.asamk.signal.manager.api.RateLimitException;
import org.asamk.signal.manager.api.UserStatus;
import org.asamk.signal.util.DateUtils;
import org.asamk.signal.util.Util;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DeviceServiceImpl implements DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);

    private final Manager manager;
    private final DBusConnection connection;
    private final String objectPath;

    private final List<DbusStructDevice> devices;

    private DBusPath thisDevice;

    public DeviceServiceImpl(Manager manager, DBusConnection connection, String objectPath) {
        this.manager = manager;
        this.connection = connection;
        this.objectPath = objectPath;

        devices = new ArrayList<>();
    }
    
    @Deprecated
    @Override
    public boolean isRegistered() throws FailureException, NumberInvalidException {
        return true;
    }

    @Override
    public boolean isRegistered(String number) throws FailureException, NumberInvalidException {
        var result = isRegistered(List.of(number));
        return result.getFirst();
    }

    @Override
    public List<Boolean> isRegistered(List<String> numbers) throws FailureException, NumberInvalidException {
        if (numbers.isEmpty()) {
            return List.of();
        }

        Map<String, UserStatus> registered;
        try {
            registered = manager.getUserStatus(new HashSet<>(numbers));
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (RateLimitException e) {
            throw new FailureException(e.getMessage()
                    + ", retry at "
                    + DateUtils.formatTimestamp(e.getNextAttemptTimestamp()));
        }

        return numbers.stream().map(number -> registered.get(number).uuid() != null).toList();
    }

    @Override
    public void unregister() throws FailureException {
        try {
            manager.unregister();
        } catch (IOException e) {
            throw new FailureException("Failed to unregister: " + e.getMessage());
        }
    }

    @Override
    public void addDevice(String uri) throws InvalidUriException {
        try {
            var deviceLinkUrl = DeviceLinkUrl.parseDeviceLinkUri(new URI(uri));
            manager.addDeviceLink(deviceLinkUrl);
        } catch (IOException | InvalidDeviceLinkException e) {
            throw new FailureException(e.getClass().getSimpleName() + " Add device link failed. " + e.getMessage());
        } catch (NotPrimaryDeviceException e) {
            throw new FailureException("This command doesn't work on linked devices.");
        } catch (URISyntaxException e) {
            throw new InvalidUriException(e.getClass().getSimpleName()
                    + " DbusPropertyDevice link uri has invalid format: "
                    + e.getMessage());
        }
    }

    @Override
    public DBusPath getDevice(long deviceId) {
        updateDevices();

        Optional<DbusStructDevice> deviceOptional = devices.stream()
                .filter(g -> g.getId().equals(deviceId))
                .findFirst();

        if (deviceOptional.isEmpty()) {
            throw new DeviceNotFoundException("DbusPropertyDevice not found");
        }

        return deviceOptional.get().getObjectPath();
    }

    @Override
    public DBusPath getThisDevice() {
        updateDevices();

        return thisDevice;
    }

    @Override
    public List<DbusStructDevice> listDevices() throws FailureException {
        updateDevices();

        return Collections.unmodifiableList(devices);
    }

    @Override
    public void updateDevices() {
        List<Device> linkedDevices;
        try {
            linkedDevices = manager.getLinkedDevices();
        } catch (IOException e) {
            throw new FailureException("Failed to get linked devices: " + e.getMessage());
        }

        unExportDevices();

        linkedDevices.forEach(device -> {
            var object = new DbusPropertyDeviceImpl(manager, objectPath, device);
            var deviceObjectPath = object.getObjectPath();
            exportObject(object);
            if (device.isThisDevice()) {
                thisDevice = new DBusPath(deviceObjectPath);
            }
            devices.add(new DbusStructDevice(new DBusPath(deviceObjectPath),
                    (long) device.id(),
                    Util.emptyIfNull(device.name())));
        });
    }

    @Override
    public void unExportDevices() {
        devices.stream()
                .map(DbusStructDevice::getObjectPath)
                .map(DBusPath::getPath)
                .forEach(connection::unExportObject);
        devices.clear();
    }

    private void exportObject(DBusInterface object) {
        try {
            connection.exportObject(object);
            logger.debug("Exported dbus object: {}", object.getObjectPath());
        } catch (DBusException e) {
            logger.warn("Failed to export dbus object ({}): {}", object.getObjectPath(), e.getMessage());
        }
    }

}

package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.properties.impl.DbusPropertyConfigurationImpl;
import org.asamk.signal.dbus.service.ConfigurationService;
import org.asamk.signal.manager.Manager;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    private final Manager manager;
    private final DBusConnection connection;
    private final String objectPath;

    public ConfigurationServiceImpl(Manager manager, DBusConnection connection, String objectPath) {
        this.manager = manager;
        this.connection = connection;
        this.objectPath = objectPath;
    }

    private static String getConfigurationObjectPath(String basePath) {
        return basePath + "/DbusPropertyConfiguration";
    }

    @Override
    public void updateConfiguration() {
        unExportConfiguration();
        var object = new DbusPropertyConfigurationImpl(manager, objectPath);
        exportObject(object);
    }

    @Override
    public void unExportConfiguration() {
        var objectPath = getConfigurationObjectPath(this.objectPath);
        connection.unExportObject(objectPath);
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

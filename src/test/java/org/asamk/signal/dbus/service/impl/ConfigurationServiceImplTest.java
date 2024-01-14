package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.service.ConfigurationService;
import org.asamk.signal.manager.Manager;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceImplTest {

    @Mock
    private Manager manager;

    @Mock
    private DBusConnection connection;

    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        configurationService = new ConfigurationServiceImpl(manager, connection, "objectPath");
    }

    @Test
    void updateConfiguration() throws DBusException {
        configurationService.updateConfiguration();

        then(connection).should().unExportObject(anyString());
        then(connection).should().exportObject(any());
    }

    @Test
    void unExportConfiguration() {
        configurationService.unExportConfiguration();

        then(connection).should().unExportObject(anyString());
    }

}
package org.asamk.signal.dbus.structs;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.Position;

public class DbusStructDevice extends Struct {

    @Position(0)
    private final DBusPath objectPath;

    @Position(1)
    private final Long id;

    @Position(2)
    private final String name;

    public DbusStructDevice(DBusPath objectPath, Long id, String name) {
        this.objectPath = objectPath;
        this.id = id;
        this.name = name;
    }

    public DBusPath getObjectPath() {
        return objectPath;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}

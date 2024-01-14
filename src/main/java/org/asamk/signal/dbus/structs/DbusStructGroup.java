package org.asamk.signal.dbus.structs;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.Position;

public class DbusStructGroup extends Struct {

    @Position(0)
    private final DBusPath objectPath;

    @Position(1)
    private final byte[] id;

    @Position(2)
    private final String name;

    public DbusStructGroup(DBusPath objectPath, byte[] id, String name) {
        this.objectPath = objectPath;
        this.id = id;
        this.name = name;
    }

    public DBusPath getObjectPath() {
        return objectPath;
    }

    public byte[] getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}

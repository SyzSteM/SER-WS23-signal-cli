package org.asamk.signal.dbus.structs;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.Position;

public class DbusStructIdentity extends Struct {

    @Position(0)
    private final DBusPath objectPath;

    @Position(1)
    private final String uuid;

    @Position(2)
    private final String number;

    public DbusStructIdentity(DBusPath objectPath, String uuid, String number) {
        this.objectPath = objectPath;
        this.uuid = uuid;
        this.number = number;
    }

    public DBusPath getObjectPath() {
        return objectPath;
    }

    public String getUuid() {
        return uuid;
    }

    public String getNumber() {
        return number;
    }

}

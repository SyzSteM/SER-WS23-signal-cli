package org.asamk.signal.dbus.events;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public class SyncMessageReceivedV2 extends DBusSignal {

    private final long timestamp;
    private final String source;
    private final String destination;
    private final byte[] groupId;
    private final String message;
    private final Map<String, Variant<?>> extras;

    public SyncMessageReceivedV2(
            String objectpath,
            long timestamp,
            String source,
            String destination,
            byte[] groupId,
            String message,
            final Map<String, Variant<?>> extras
    ) throws DBusException {
        super(objectpath, timestamp, source, destination, groupId, message, extras);
        this.timestamp = timestamp;
        this.source = source;
        this.destination = destination;
        this.groupId = groupId;
        this.message = message;
        this.extras = extras;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public byte[] getGroupId() {
        return groupId;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Variant<?>> getExtras() {
        return extras;
    }
}

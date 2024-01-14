package org.asamk.signal.dbus.events;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public class MessageReceivedV2 extends DBusSignal {

    private final long timestamp;
    private final String sender;
    private final byte[] groupId;
    private final String message;
    private final Map<String, Variant<?>> extras;

    public MessageReceivedV2(
            String objectpath,
            long timestamp,
            String sender,
            byte[] groupId,
            String message,
            final Map<String, Variant<?>> extras
    ) throws DBusException {
        super(objectpath, timestamp, sender, groupId, message, extras);
        this.timestamp = timestamp;
        this.sender = sender;
        this.groupId = groupId;
        this.message = message;
        this.extras = extras;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
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

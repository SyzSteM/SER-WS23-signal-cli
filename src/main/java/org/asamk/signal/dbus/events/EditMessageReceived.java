package org.asamk.signal.dbus.events;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public class EditMessageReceived extends DBusSignal {

    private final long timestamp;
    private final long targetSentTimestamp;
    private final String sender;
    private final byte[] groupId;
    private final String message;
    private final Map<String, Variant<?>> extras;

    public EditMessageReceived(
            String objectpath,
            long timestamp,
            final long targetSentTimestamp,
            String sender,
            byte[] groupId,
            String message,
            final Map<String, Variant<?>> extras
    ) throws DBusException {
        super(objectpath, timestamp, targetSentTimestamp, sender, groupId, message, extras);
        this.timestamp = timestamp;
        this.targetSentTimestamp = targetSentTimestamp;
        this.sender = sender;
        this.groupId = groupId;
        this.message = message;
        this.extras = extras;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTargetSentTimestamp() {
        return targetSentTimestamp;
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

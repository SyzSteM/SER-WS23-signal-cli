package org.asamk.signal.dbus.events;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;

import java.util.List;

public class SyncMessageReceived extends DBusSignal {

    private final long timestamp;
    private final String source;
    private final String destination;
    private final byte[] groupId;
    private final String message;
    private final List<String> attachments;

    public SyncMessageReceived(
            String objectpath,
            long timestamp,
            String source,
            String destination,
            byte[] groupId,
            String message,
            List<String> attachments
    ) throws DBusException {
        super(objectpath, timestamp, source, destination, groupId, message, attachments);
        this.timestamp = timestamp;
        this.source = source;
        this.destination = destination;
        this.groupId = groupId;
        this.message = message;
        this.attachments = attachments;
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

    public List<String> getAttachments() {
        return attachments;
    }
}

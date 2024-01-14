package org.asamk.signal.dbus.events;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;

import java.util.List;

public class MessageReceived extends DBusSignal {

    private final long timestamp;
    private final String sender;
    private final byte[] groupId;
    private final String message;
    private final List<String> attachments;

    public MessageReceived(
            String objectpath, long timestamp, String sender, byte[] groupId, String message, List<String> attachments
    ) throws DBusException {
        super(objectpath, timestamp, sender, groupId, message, attachments);
        this.timestamp = timestamp;
        this.sender = sender;
        this.groupId = groupId;
        this.message = message;
        this.attachments = attachments;
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

    public List<String> getAttachments() {
        return attachments;
    }
}

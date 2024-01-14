package org.asamk.signal.dbus.events;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;

public class ReceiptReceived extends DBusSignal {

    private final long timestamp;
    private final String sender;

    public ReceiptReceived(String objectpath, long timestamp, String sender) throws DBusException {
        super(objectpath, timestamp, sender);
        this.timestamp = timestamp;
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }
}

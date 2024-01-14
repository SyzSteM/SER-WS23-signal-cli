package org.asamk.signal.dbus.events;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public class ReceiptReceivedV2 extends DBusSignal {

    private final long timestamp;
    private final String sender;
    private final String type;
    private final Map<String, Variant<?>> extras;

    public ReceiptReceivedV2(
            String objectpath, long timestamp, String sender, final String type, final Map<String, Variant<?>> extras
    ) throws DBusException {
        super(objectpath, timestamp, sender, type, extras);
        this.timestamp = timestamp;
        this.sender = sender;
        this.type = type;
        this.extras = extras;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiptType() {
        return type;
    }

    public Map<String, Variant<?>> getExtras() {
        return extras;
    }
}

package org.asamk.signal.dbus.util;

import org.asamk.signal.dbus.errors.InvalidGroupIdException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.manager.api.GroupId;
import org.asamk.signal.manager.api.InvalidNumberException;
import org.asamk.signal.manager.api.RecipientAddress;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.freedesktop.dbus.exceptions.DBusExecutionException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RecipientUtils {

    private RecipientUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<String> getRecipientStrings(Set<RecipientAddress> members) {
        return members.stream().map(RecipientAddress::getLegacyIdentifier).toList();
    }

    public static Set<RecipientIdentifier.Single> getSingleRecipientIdentifiers(
            Collection<String> recipientStrings, String localNumber
    ) throws DBusExecutionException {
        var identifiers = new HashSet<RecipientIdentifier.Single>();
        for (var recipientString : recipientStrings) {
            identifiers.add(getSingleRecipientIdentifier(recipientString, localNumber));
        }
        return identifiers;
    }

    public static RecipientIdentifier.Single getSingleRecipientIdentifier(
            String recipientString, String localNumber
    ) throws DBusExecutionException {
        try {
            return RecipientIdentifier.Single.fromString(recipientString, localNumber);
        } catch (InvalidNumberException e) {
            throw new NumberInvalidException(e.getMessage());
        }
    }

    public static RecipientIdentifier.Group getGroupRecipientIdentifier(byte[] groupId) {
        return new RecipientIdentifier.Group(getGroupId(groupId));
    }

    public static GroupId getGroupId(byte[] groupId) throws DBusExecutionException {
        try {
            return GroupId.unknownVersion(groupId);
        } catch (Throwable e) {
            throw new InvalidGroupIdException("Invalid group id: " + e.getMessage());
        }
    }

}

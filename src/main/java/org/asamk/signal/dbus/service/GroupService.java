package org.asamk.signal.dbus.service;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.GroupException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.InvalidAttachmentException;
import org.asamk.signal.dbus.errors.InvalidGroupIdException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.dbus.structs.DbusStructGroup;
import org.freedesktop.dbus.DBusPath;

import java.util.List;

public interface GroupService {

    long sendGroupMessage(
            String message, List<String> attachments, byte[] groupId
    ) throws GroupException, FailureException, InvalidAttachmentException, InvalidGroupIdException;

    void sendGroupTyping(
            byte[] groupId, boolean stop
    ) throws FailureException, GroupException, IdentityUntrustedException;

    long sendGroupRemoteDeleteMessage(
            long targetSentTimestamp, byte[] groupId
    ) throws FailureException, GroupException, InvalidGroupIdException;

    long sendGroupMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, byte[] groupId
    ) throws GroupException, FailureException, NumberInvalidException, InvalidGroupIdException;

    @Deprecated
    void setGroupBlocked(byte[] groupId, boolean blocked) throws GroupException, InvalidGroupIdException;

    @Deprecated
    List<byte[]> getGroupIds();

    DBusPath getGroup(byte[] groupId);

    List<DbusStructGroup> listGroups();

    @Deprecated
    String getGroupName(byte[] groupId) throws InvalidGroupIdException;

    @Deprecated
    List<String> getGroupMembers(byte[] groupId) throws InvalidGroupIdException;

    byte[] createGroup(
            String name, List<String> members, String avatar
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException;

    @Deprecated
    byte[] updateGroup(
            byte[] groupId, String name, List<String> members, String avatar
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, GroupException, InvalidGroupIdException;

    @Deprecated
    void quitGroup(byte[] groupId) throws GroupException, FailureException, InvalidGroupIdException;

    @Deprecated
    boolean isGroupBlocked(byte[] groupId) throws InvalidGroupIdException;

    @Deprecated
    boolean isMember(byte[] groupId) throws InvalidGroupIdException;

    byte[] joinGroup(String groupLink) throws FailureException;

    void updateGroups();

    void unExportGroups();
}

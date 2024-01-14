package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.GroupException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.InvalidAttachmentException;
import org.asamk.signal.dbus.errors.InvalidGroupIdException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.dbus.properties.impl.DbusPropertyGroupImpl;
import org.asamk.signal.dbus.service.GroupService;
import org.asamk.signal.dbus.structs.DbusStructGroup;
import org.asamk.signal.dbus.util.RecipientUtils;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.AttachmentInvalidException;
import org.asamk.signal.manager.api.Group;
import org.asamk.signal.manager.api.GroupInviteLinkUrl;
import org.asamk.signal.manager.api.GroupNotFoundException;
import org.asamk.signal.manager.api.GroupSendingNotAllowedException;
import org.asamk.signal.manager.api.InactiveGroupLinkException;
import org.asamk.signal.manager.api.InvalidStickerException;
import org.asamk.signal.manager.api.LastGroupAdminException;
import org.asamk.signal.manager.api.Message;
import org.asamk.signal.manager.api.NotAGroupMemberException;
import org.asamk.signal.manager.api.NotPrimaryDeviceException;
import org.asamk.signal.manager.api.PendingAdminApprovalException;
import org.asamk.signal.manager.api.TypingAction;
import org.asamk.signal.manager.api.UnregisteredRecipientException;
import org.asamk.signal.manager.api.UpdateGroup;
import org.asamk.signal.util.SendMessageResultUtils;
import org.asamk.signal.util.Util;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GroupServiceImpl implements GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private final Manager manager;
    private final DBusConnection connection;
    private final String objectPath;

    private final List<DbusStructGroup> groups = new ArrayList<>();

    public GroupServiceImpl(Manager manager, DBusConnection connection, String objectPath) {
        this.manager = manager;
        this.connection = connection;
        this.objectPath = objectPath;
    }

    @Override
    public long sendGroupMessage(
            String message, List<String> attachments, byte[] groupId
    ) throws GroupException, FailureException, InvalidAttachmentException, InvalidGroupIdException {
        try {
            var results = manager.sendMessage(new Message(message,
                    attachments,
                    List.of(),
                    Optional.empty(),
                    Optional.empty(),
                    List.of(),
                    Optional.empty(),
                    List.of()), Set.of(RecipientUtils.getGroupRecipientIdentifier(groupId)));
            SendMessageResultUtils.checkSendMessageResults(results);
            return results.timestamp();
        } catch (IOException | InvalidStickerException e) {
            throw new FailureException(e.getMessage());
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        } catch (AttachmentInvalidException e) {
            throw new InvalidAttachmentException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }

    }

    @Override
    public void sendGroupTyping(
            byte[] groupId, boolean stop
    ) throws FailureException, GroupException, IdentityUntrustedException {
        try {
            var results = manager.sendTypingMessage(stop ? TypingAction.STOP : TypingAction.START,
                    Set.of(RecipientUtils.getGroupRecipientIdentifier(groupId)));
            SendMessageResultUtils.checkSendMessageResults(results);
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        }
    }

    @Override
    public long sendGroupRemoteDeleteMessage(
            long targetSentTimestamp, byte[] groupId
    ) throws FailureException, GroupException, InvalidGroupIdException {
        try {
            var results = manager.sendRemoteDeleteMessage(targetSentTimestamp,
                    Set.of(RecipientUtils.getGroupRecipientIdentifier(groupId)));
            SendMessageResultUtils.checkSendMessageResults(results);
            return results.timestamp();
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        }
    }

    @Override
    public long sendGroupMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, byte[] groupId
    ) throws GroupException, FailureException, NumberInvalidException, InvalidGroupIdException {
        try {
            var results = manager.sendMessageReaction(emoji,
                    remove,
                    RecipientUtils.getSingleRecipientIdentifier(targetAuthor, manager.getSelfNumber()),
                    targetSentTimestamp,
                    Set.of(RecipientUtils.getGroupRecipientIdentifier(groupId)),
                    false);
            SendMessageResultUtils.checkSendMessageResults(results);
            return results.timestamp();
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Deprecated
    @Override
    public void setGroupBlocked(
            byte[] groupId, boolean blocked
    ) throws GroupException, InvalidGroupIdException {
        try {
            manager.setGroupsBlocked(List.of(RecipientUtils.getGroupId(groupId)), blocked);
        } catch (NotPrimaryDeviceException e) {
            throw new FailureException("This command doesn't work on linked devices.");
        } catch (GroupNotFoundException e) {
            throw new GroupException(e.getMessage());
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

    @Override
    @Deprecated
    public List<byte[]> getGroupIds() {
        var groups = manager.getGroups();
        return groups.stream().map(g -> g.groupId().serialize()).toList();
    }

    @Override
    public DBusPath getGroup(byte[] groupId) {
        updateGroups();
        var groupOptional = groups.stream().filter(g -> Arrays.equals(g.getId(), groupId)).findFirst();
        if (groupOptional.isEmpty()) {
            throw new GroupException("DbusPropertyGroup not found");
        }
        return groupOptional.get().getObjectPath();
    }

    @Override
    public List<DbusStructGroup> listGroups() {
        updateGroups();
        return groups;
    }

    @Override
    @Deprecated
    public String getGroupName(byte[] groupId) {
        var group = manager.getGroup(RecipientUtils.getGroupId(groupId));
        if (group == null || group.title() == null) {
            return "";
        } else {
            return group.title();
        }
    }

    @Override
    @Deprecated
    public List<String> getGroupMembers(byte[] groupId) {
        var group = manager.getGroup(RecipientUtils.getGroupId(groupId));
        if (group == null) {
            return List.of();
        } else {
            var members = group.members();
            return RecipientUtils.getRecipientStrings(members);
        }
    }

    @Override
    public byte[] createGroup(
            String name, List<String> members, String avatar
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException {
        return updateGroupInternal(new byte[0], name, members, avatar);
    }

    @Deprecated
    @Override
    public byte[] updateGroup(
            byte[] groupId, String name, List<String> members, String avatar
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, GroupException, InvalidGroupIdException {
        return updateGroupInternal(groupId, name, members, avatar);
    }

    @Override
    @Deprecated
    public void quitGroup(byte[] groupId) {
        var group = RecipientUtils.getGroupId(groupId);
        try {
            manager.quitGroup(group, Set.of());
        } catch (GroupNotFoundException | NotAGroupMemberException e) {
            throw new GroupException(e.getMessage());
        } catch (IOException | LastGroupAdminException e) {
            throw new FailureException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Deprecated
    @Override
    public boolean isGroupBlocked(byte[] groupId) throws InvalidGroupIdException {
        var group = manager.getGroup(RecipientUtils.getGroupId(groupId));
        if (group == null) {
            return false;
        } else {
            return group.isBlocked();
        }

    }

    @Deprecated
    @Override
    public boolean isMember(byte[] groupId) throws InvalidGroupIdException {
        var group = manager.getGroup(RecipientUtils.getGroupId(groupId));
        if (group == null) {
            return false;
        } else {
            return group.isMember();
        }
    }

    @Override
    public byte[] joinGroup(String groupLink) throws FailureException {
        try {
            var linkUrl = GroupInviteLinkUrl.fromUri(groupLink);
            if (linkUrl == null) {
                throw new FailureException("DbusPropertyGroup link is invalid:");
            }
            var result = manager.joinGroup(linkUrl);
            return result.first().serialize();
        } catch (PendingAdminApprovalException e) {
            throw new FailureException("Pending admin approval: " + e.getMessage());
        } catch (GroupInviteLinkUrl.InvalidGroupLinkException | InactiveGroupLinkException e) {
            throw new FailureException("DbusPropertyGroup link is invalid: " + e.getMessage());
        } catch (GroupInviteLinkUrl.UnknownGroupLinkVersionException e) {
            throw new FailureException("DbusPropertyGroup link was created with an incompatible version: "
                    + e.getMessage());
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

    @Override
    public void updateGroups() {
        List<Group> groups = manager.getGroups();

        unExportGroups();

        groups.forEach(g -> {
            var object = new DbusPropertyGroupImpl(manager, objectPath, g.groupId());
            exportObject(object);
            this.groups.add(new DbusStructGroup(new DBusPath(object.getObjectPath()),
                    g.groupId().serialize(),
                    Util.emptyIfNull(g.title())));
        });
    }

    @Override
    public void unExportGroups() {
        groups.stream().map(DbusStructGroup::getObjectPath).map(DBusPath::getPath).forEach(connection::unExportObject);
        groups.clear();
    }

    private void exportObject(DBusInterface object) {
        try {
            connection.exportObject(object);
            logger.debug("Exported dbus object: {}", object.getObjectPath());
        } catch (DBusException e) {
            logger.warn("Failed to export dbus object ({}): {}", object.getObjectPath(), e.getMessage());
        }
    }

    private byte[] updateGroupInternal(byte[] groupId, String name, List<String> members, String avatar) {
        try {
            groupId = Util.nullIfEmpty(groupId);
            name = Util.nullIfEmpty(name);
            avatar = Util.nullIfEmpty(avatar);
            var memberIdentifiers = RecipientUtils.getSingleRecipientIdentifiers(members, manager.getSelfNumber());
            if (groupId == null) {
                var results = manager.createGroup(name, memberIdentifiers, avatar);
                updateGroups();
                SendMessageResultUtils.checkGroupSendMessageResults(results.second().timestamp(),
                        results.second().results());
                return results.first().serialize();
            } else {
                var results = manager.updateGroup(RecipientUtils.getGroupId(groupId),
                        UpdateGroup.newBuilder()
                                .withName(name)
                                .withMembers(memberIdentifiers)
                                .withAvatarFile(avatar)
                                .build());
                if (results != null) {
                    SendMessageResultUtils.checkGroupSendMessageResults(results.timestamp(), results.results());
                }
                return groupId;
            }
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        } catch (AttachmentInvalidException e) {
            throw new InvalidAttachmentException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

}

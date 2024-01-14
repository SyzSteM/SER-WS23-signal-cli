package org.asamk.signal.dbus.properties.impl;

import org.asamk.signal.dbus.DbusInterfacePropertiesHandler;
import org.asamk.signal.dbus.DbusProperties;
import org.asamk.signal.dbus.DbusProperty;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.GroupAdminException;
import org.asamk.signal.dbus.errors.GroupException;
import org.asamk.signal.dbus.errors.GroupMemberException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.InvalidAttachmentException;
import org.asamk.signal.dbus.properties.DbusPropertyGroup;
import org.asamk.signal.dbus.util.RecipientUtils;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.AttachmentInvalidException;
import org.asamk.signal.manager.api.GroupId;
import org.asamk.signal.manager.api.GroupLinkState;
import org.asamk.signal.manager.api.GroupNotFoundException;
import org.asamk.signal.manager.api.GroupPermission;
import org.asamk.signal.manager.api.GroupSendingNotAllowedException;
import org.asamk.signal.manager.api.LastGroupAdminException;
import org.asamk.signal.manager.api.NotAGroupMemberException;
import org.asamk.signal.manager.api.NotPrimaryDeviceException;
import org.asamk.signal.manager.api.UnregisteredRecipientException;
import org.asamk.signal.manager.api.UpdateGroup;
import org.asamk.signal.util.Util;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.asamk.signal.dbus.util.DbusUtils.makeValidObjectPathElement;

public final class DbusPropertyGroupImpl extends DbusProperties implements DbusPropertyGroup {

    private static final Logger logger = LoggerFactory.getLogger(DbusPropertyGroupImpl.class);

    private final Manager manager;
    private final String objectPath;
    private final GroupId groupId;

    public DbusPropertyGroupImpl(Manager manager, String objectPath, GroupId groupId) {
        this.manager = manager;
        this.objectPath = objectPath;
        this.groupId = groupId;

        addPropertiesHandler(new DbusInterfacePropertiesHandler("org.asamk.signal.dbus.properties.DbusPropertyGroup",
                List.of(new DbusProperty<>("Id", groupId::serialize),
                        new DbusProperty<>("Name", () -> Util.emptyIfNull(getGroup().title()), this::setGroupName),
                        new DbusProperty<>("Description",
                                () -> Util.emptyIfNull(getGroup().description()),
                                this::setGroupDescription),
                        new DbusProperty<>("Avatar", this::setGroupAvatar),
                        new DbusProperty<>("IsBlocked", () -> getGroup().isBlocked(), this::setIsBlocked),
                        new DbusProperty<>("IsMember", () -> getGroup().isMember()),
                        new DbusProperty<>("IsAdmin", () -> getGroup().isAdmin()),
                        new DbusProperty<>("MessageExpirationTimer",
                                () -> getGroup().messageExpirationTimer(),
                                this::setMessageExpirationTime),
                        new DbusProperty<>("Members",
                                () -> new Variant<>(RecipientUtils.getRecipientStrings(getGroup().members()), "as")),
                        new DbusProperty<>("PendingMembers",
                                () -> new Variant<>(RecipientUtils.getRecipientStrings(getGroup().pendingMembers()),
                                        "as")),
                        new DbusProperty<>("RequestingMembers",
                                () -> new Variant<>(RecipientUtils.getRecipientStrings(getGroup().requestingMembers()),
                                        "as")),
                        new DbusProperty<>("Admins",
                                () -> new Variant<>(RecipientUtils.getRecipientStrings(getGroup().adminMembers()),
                                        "as")),
                        new DbusProperty<>("Banned",
                                () -> new Variant<>(RecipientUtils.getRecipientStrings(getGroup().bannedMembers()),
                                        "as")),
                        new DbusProperty<>("PermissionAddMember",
                                () -> getGroup().permissionAddMember().name(),
                                this::setGroupPermissionAddMember),
                        new DbusProperty<>("PermissionEditDetails",
                                () -> getGroup().permissionEditDetails().name(),
                                this::setGroupPermissionEditDetails),
                        new DbusProperty<>("PermissionSendMessage",
                                () -> getGroup().permissionSendMessage().name(),
                                this::setGroupPermissionSendMessage),
                        new DbusProperty<>("GroupInviteLink", () -> {
                            var groupInviteLinkUrl = getGroup().groupInviteLinkUrl();
                            return groupInviteLinkUrl == null ? "" : groupInviteLinkUrl.getUrl();
                        }))));
    }

    private static String getGroupObjectPath(String basePath, byte[] groupId) {
        return basePath + "/Groups/" + makeValidObjectPathElement(Base64.getEncoder().encodeToString(groupId));
    }

    @Override
    public String getObjectPath() {
        return getGroupObjectPath(objectPath, groupId.serialize());
    }

    @Override
    public void quitGroup() throws FailureException {
        try {
            manager.quitGroup(groupId, Set.of());
        } catch (GroupNotFoundException e) {
            throw new GroupException(e.getMessage());
        } catch (NotAGroupMemberException e) {
            throw new GroupMemberException(e.getMessage());
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (LastGroupAdminException e) {
            throw new GroupAdminException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Override
    public void deleteGroup() throws FailureException, GroupAdminException {
        try {
            manager.deleteGroup(groupId);
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

    @Override
    public void addMembers(List<String> recipients) throws FailureException {
        var memberIdentifiers = RecipientUtils.getSingleRecipientIdentifiers(recipients, manager.getSelfNumber());
        updateGroup(UpdateGroup.newBuilder().withMembers(memberIdentifiers).build());
    }

    @Override
    public void removeMembers(List<String> recipients) throws FailureException {
        var memberIdentifiers = RecipientUtils.getSingleRecipientIdentifiers(recipients, manager.getSelfNumber());
        updateGroup(UpdateGroup.newBuilder().withRemoveMembers(memberIdentifiers).build());
    }

    @Override
    public void addAdmins(List<String> recipients) throws FailureException {
        var memberIdentifiers = RecipientUtils.getSingleRecipientIdentifiers(recipients, manager.getSelfNumber());
        updateGroup(UpdateGroup.newBuilder().withAdmins(memberIdentifiers).build());
    }

    @Override
    public void removeAdmins(List<String> recipients) throws FailureException {
        var memberIdentifiers = RecipientUtils.getSingleRecipientIdentifiers(recipients, manager.getSelfNumber());
        updateGroup(UpdateGroup.newBuilder().withRemoveAdmins(memberIdentifiers).build());
    }

    @Override
    public void resetLink() throws FailureException {
        updateGroup(UpdateGroup.newBuilder().withResetGroupLink(true).build());
    }

    @Override
    public void disableLink() throws FailureException {
        updateGroup(UpdateGroup.newBuilder().withGroupLinkState(GroupLinkState.DISABLED).build());
    }

    @Override
    public void enableLink(boolean requiresApproval) throws FailureException {
        updateGroup(UpdateGroup.newBuilder()
                .withGroupLinkState(requiresApproval ? GroupLinkState.ENABLED_WITH_APPROVAL : GroupLinkState.ENABLED)
                .build());
    }

    private org.asamk.signal.manager.api.Group getGroup() {
        return manager.getGroup(groupId);
    }

    private void setGroupName(String name) {
        updateGroup(UpdateGroup.newBuilder().withName(name).build());
    }

    private void setGroupDescription(String description) {
        updateGroup(UpdateGroup.newBuilder().withDescription(description).build());
    }

    private void setGroupAvatar(String avatar) {
        updateGroup(UpdateGroup.newBuilder().withAvatarFile(avatar).build());
    }

    private void setMessageExpirationTime(int expirationTime) {
        updateGroup(UpdateGroup.newBuilder().withExpirationTimer(expirationTime).build());
    }

    private void setGroupPermissionAddMember(String permission) {
        updateGroup(UpdateGroup.newBuilder().withAddMemberPermission(GroupPermission.valueOf(permission)).build());
    }

    private void setGroupPermissionEditDetails(String permission) {
        updateGroup(UpdateGroup.newBuilder().withEditDetailsPermission(GroupPermission.valueOf(permission)).build());
    }

    private void setGroupPermissionSendMessage(String permission) {
        updateGroup(UpdateGroup.newBuilder()
                .withIsAnnouncementGroup(GroupPermission.valueOf(permission) == GroupPermission.ONLY_ADMINS)
                .build());
    }

    private void setIsBlocked(boolean isBlocked) {
        try {
            manager.setGroupsBlocked(List.of(groupId), isBlocked);
        } catch (NotPrimaryDeviceException e) {
            throw new FailureException("This command doesn't work on linked devices.");
        } catch (GroupNotFoundException e) {
            throw new GroupException(e.getMessage());
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

    private void updateGroup(UpdateGroup updateGroup) {
        try {
            manager.updateGroup(groupId, updateGroup);
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

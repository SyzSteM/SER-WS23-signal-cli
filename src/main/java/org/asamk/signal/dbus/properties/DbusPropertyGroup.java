package org.asamk.signal.dbus.properties;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.manager.api.LastGroupAdminException;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.interfaces.Properties;

import java.util.List;

@DBusProperty(name = "Id", type = Byte[].class, access = DBusProperty.Access.READ)
@DBusProperty(name = "Name", type = String.class)
@DBusProperty(name = "Description", type = String.class)
@DBusProperty(name = "Avatar", type = String.class, access = DBusProperty.Access.WRITE)
@DBusProperty(name = "IsBlocked", type = Boolean.class)
@DBusProperty(name = "IsMember", type = Boolean.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "IsAdmin", type = Boolean.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "MessageExpirationTimer", type = Integer.class)
@DBusProperty(name = "Members", type = String[].class, access = DBusProperty.Access.READ)
@DBusProperty(name = "PendingMembers", type = String[].class, access = DBusProperty.Access.READ)
@DBusProperty(name = "RequestingMembers", type = String[].class, access = DBusProperty.Access.READ)
@DBusProperty(name = "Admins", type = String[].class, access = DBusProperty.Access.READ)
@DBusProperty(name = "Banned", type = String[].class, access = DBusProperty.Access.READ)
@DBusProperty(name = "PermissionAddMember", type = String.class)
@DBusProperty(name = "PermissionEditDetails", type = String.class)
@DBusProperty(name = "PermissionSendMessage", type = String.class)
@DBusProperty(name = "GroupInviteLink", type = String.class, access = DBusProperty.Access.READ)
public interface DbusPropertyGroup extends Properties {

    void quitGroup() throws FailureException, LastGroupAdminException;

    void deleteGroup() throws FailureException;

    void addMembers(List<String> recipients) throws FailureException;

    void removeMembers(List<String> recipients) throws FailureException;

    void addAdmins(List<String> recipients) throws FailureException;

    void removeAdmins(List<String> recipients) throws FailureException;

    void resetLink() throws FailureException;

    void disableLink() throws FailureException;

    void enableLink(boolean requiresApproval) throws FailureException;

}

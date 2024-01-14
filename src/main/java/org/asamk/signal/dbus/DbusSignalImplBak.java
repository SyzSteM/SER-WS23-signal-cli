//package org.asamk.signal.dbus;
//
//import org.asamk.Signal;
//import org.asamk.signal.BaseConfig;
//import org.asamk.signal.dbus.errors.DeviceNotFoundException;
//import org.asamk.signal.dbus.errors.FailureException;
//import org.asamk.signal.dbus.errors.GroupException;
//import org.asamk.signal.dbus.errors.IdentityUntrustedException;
//import org.asamk.signal.dbus.errors.InvalidAttachmentException;
//import org.asamk.signal.dbus.errors.InvalidUriException;
//import org.asamk.signal.dbus.errors.NumberInvalidException;
//import org.asamk.signal.dbus.properties.impl.DbusPropertyConfigurationImpl;
//import org.asamk.signal.dbus.properties.impl.DbusPropertyDeviceImpl;
//import org.asamk.signal.dbus.properties.impl.DbusPropertyGroupImpl;
//import org.asamk.signal.dbus.properties.impl.DbusPropertyIdentityImpl;
//import org.asamk.signal.dbus.structs.DbusStructDevice;
//import org.asamk.signal.dbus.structs.DbusStructGroup;
//import org.asamk.signal.dbus.structs.DbusStructIdentity;
//import org.asamk.signal.dbus.util.RecipientUtils;
//import org.asamk.signal.manager.Manager;
//import org.asamk.signal.manager.api.AttachmentInvalidException;
//import org.asamk.signal.manager.api.Device;
//import org.asamk.signal.manager.api.DeviceLinkUrl;
//import org.asamk.signal.manager.api.GroupInviteLinkUrl;
//import org.asamk.signal.manager.api.GroupNotFoundException;
//import org.asamk.signal.manager.api.GroupSendingNotAllowedException;
//import org.asamk.signal.manager.api.Identity;
//import org.asamk.signal.manager.api.InactiveGroupLinkException;
//import org.asamk.signal.manager.api.InvalidDeviceLinkException;
//import org.asamk.signal.manager.api.InvalidStickerException;
//import org.asamk.signal.manager.api.LastGroupAdminException;
//import org.asamk.signal.manager.api.Message;
//import org.asamk.signal.manager.api.NotAGroupMemberException;
//import org.asamk.signal.manager.api.NotPrimaryDeviceException;
//import org.asamk.signal.manager.api.PendingAdminApprovalException;
//import org.asamk.signal.manager.api.RateLimitException;
//import org.asamk.signal.manager.api.RecipientIdentifier;
//import org.asamk.signal.manager.api.StickerPackInvalidException;
//import org.asamk.signal.manager.api.TypingAction;
//import org.asamk.signal.manager.api.UnregisteredRecipientException;
//import org.asamk.signal.manager.api.UpdateGroup;
//import org.asamk.signal.manager.api.UpdateProfile;
//import org.asamk.signal.manager.api.UserStatus;
//import org.asamk.signal.util.DateUtils;
//import org.asamk.signal.util.SendMessageResultUtils;
//import org.asamk.signal.util.Util;
//import org.freedesktop.dbus.DBusPath;
//import org.freedesktop.dbus.connections.impl.DBusConnection;
//import org.freedesktop.dbus.exceptions.DBusException;
//import org.freedesktop.dbus.interfaces.DBusInterface;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//public class DbusSignalImplBak implements Signal, AutoCloseable {
//
//    private static final Logger logger = LoggerFactory.getLogger(DbusSignalImplBak.class);
//
//    private final Manager m;
//    private final DBusConnection connection;
//    private final String objectPath;
//    private final boolean noReceiveOnStart;
//    private final List<DbusStructDevice> devices = new ArrayList<>();
//    private final List<DbusStructGroup> groups = new ArrayList<>();
//    private final List<DbusStructIdentity> identities = new ArrayList<>();
//
//    private DBusPath thisDevice;
//    private DbusReceiveMessageHandler dbusMessageHandler;
//    private int subscriberCount;
//
//    public DbusSignalImplBak(
//            Manager m, DBusConnection connection, String objectPath, boolean noReceiveOnStart
//    ) {
//        this.m = m;
//        this.connection = connection;
//        this.objectPath = objectPath;
//        this.noReceiveOnStart = noReceiveOnStart;
//
//        m.addAddressChangedListener(() -> {
//            unExportObjects();
//            exportObjects();
//        });
//    }
//
//    private static String getConfigurationObjectPath(String basePath) {
//        return basePath + "/DbusPropertyConfiguration";
//    }
//
//    public void initObjects() {
//        exportObjects();
//        if (!noReceiveOnStart) {
//            subscribeReceive();
//        }
//    }
//
//    private void exportObjects() {
//        exportObject(this);
//
//        updateDevices();
//        updateGroups();
//        updateConfiguration();
//        updateIdentities();
//    }
//
//    @Override
//    public void close() {
//        if (dbusMessageHandler != null) {
//            m.removeReceiveHandler(dbusMessageHandler);
//            dbusMessageHandler = null;
//        }
//        unExportObjects();
//    }
//
//    private void unExportObjects() {
//        unExportDevices();
//        unExportGroups();
//        unExportConfiguration();
//        unExportIdentities();
//        connection.unExportObject(objectPath);
//    }
//
//    @Override
//    public String getObjectPath() {
//        return objectPath;
//    }
//
//    @Override
//    public String getSelfNumber() {
//        return m.getSelfNumber();
//    }
//
//    @Override
//    public void subscribeReceive() {
//        if (dbusMessageHandler == null) {
//            dbusMessageHandler = new DbusReceiveMessageHandler(connection, objectPath);
//            m.addReceiveHandler(dbusMessageHandler);
//        }
//        subscriberCount++;
//    }
//
//    @Override
//    public void unsubscribeReceive() {
//        subscriberCount = Math.max(0, subscriberCount - 1);
//        if (subscriberCount == 0 && dbusMessageHandler != null) {
//            m.removeReceiveHandler(dbusMessageHandler);
//            dbusMessageHandler = null;
//        }
//    }
//
//    @Override
//    public long sendMessage(String message, List<String> attachments, String recipient) {
//        return sendMessage(message, attachments, List.of(recipient));
//    }
//
//    @Override
//    public long sendMessage(String message, List<String> attachments, List<String> recipients) {
//        try {
//            var results = m.sendMessage(new Message(message,
//                            attachments,
//                            List.of(),
//                            Optional.empty(),
//                            Optional.empty(),
//                            List.of(),
//                            Optional.empty(),
//                            List.of()),
//                    RecipientUtils.getSingleRecipientIdentifiers(recipients, m.getSelfNumber())
//                            .stream()
//                            .map(RecipientIdentifier.class::cast)
//                            .collect(Collectors.toSet()));
//
//            SendMessageResultUtils.checkSendMessageResults(results);
//            return results.timestamp();
//        } catch (AttachmentInvalidException e) {
//            throw new InvalidAttachmentException(e.getMessage());
//        } catch (IOException | InvalidStickerException e) {
//            throw new FailureException(e);
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    @Override
//    public void sendTyping(
//            String recipient, boolean stop
//    ) throws FailureException, GroupException, IdentityUntrustedException {
//        try {
//            var results = m.sendTypingMessage(stop ? TypingAction.STOP : TypingAction.START,
//                    RecipientUtils.getSingleRecipientIdentifiers(List.of(recipient), m.getSelfNumber())
//                            .stream()
//                            .map(RecipientIdentifier.class::cast)
//                            .collect(Collectors.toSet()));
//            SendMessageResultUtils.checkSendMessageResults(results);
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        }
//    }
//
//    @Override
//    public void sendReadReceipt(
//            String recipient, List<Long> messageIds
//    ) throws FailureException, IdentityUntrustedException {
//        var results = m.sendReadReceipt(RecipientUtils.getSingleRecipientIdentifier(recipient, m.getSelfNumber()),
//                messageIds);
//        SendMessageResultUtils.checkSendMessageResults(results);
//    }
//
//    @Override
//    public void sendViewedReceipt(
//            String recipient, List<Long> messageIds
//    ) throws FailureException, IdentityUntrustedException {
//        var results = m.sendViewedReceipt(RecipientUtils.getSingleRecipientIdentifier(recipient, m.getSelfNumber()),
//                messageIds);
//        SendMessageResultUtils.checkSendMessageResults(results);
//    }
//
//    @Override
//    public long sendRemoteDeleteMessage(
//            long targetSentTimestamp, String recipient
//    ) {
//        return sendRemoteDeleteMessage(targetSentTimestamp, List.of(recipient));
//    }
//
//    @Override
//    public long sendRemoteDeleteMessage(
//            long targetSentTimestamp, List<String> recipients
//    ) {
//        try {
//            var results = m.sendRemoteDeleteMessage(targetSentTimestamp,
//                    RecipientUtils.getSingleRecipientIdentifiers(recipients, m.getSelfNumber())
//                            .stream()
//                            .map(RecipientIdentifier.class::cast)
//                            .collect(Collectors.toSet()));
//            SendMessageResultUtils.checkSendMessageResults(results);
//            return results.timestamp();
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        }
//    }
//
//    @Override
//    public long sendMessageReaction(
//            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, String recipient
//    ) {
//        return sendMessageReaction(emoji, remove, targetAuthor, targetSentTimestamp, List.of(recipient));
//    }
//
//    @Override
//    public long sendMessageReaction(
//            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, List<String> recipients
//    ) {
//        try {
//            var results = m.sendMessageReaction(emoji,
//                    remove,
//                    RecipientUtils.getSingleRecipientIdentifier(targetAuthor, m.getSelfNumber()),
//                    targetSentTimestamp,
//                    RecipientUtils.getSingleRecipientIdentifiers(recipients, m.getSelfNumber())
//                            .stream()
//                            .map(RecipientIdentifier.class::cast)
//                            .collect(Collectors.toSet()),
//                    false);
//            SendMessageResultUtils.checkSendMessageResults(results);
//            return results.timestamp();
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    @Override
//    public long sendPaymentNotification(
//            byte[] receipt, String note, String recipient
//    ) throws FailureException {
//        try {
//            var results = m.sendPaymentNotificationMessage(receipt,
//                    note,
//                    RecipientUtils.getSingleRecipientIdentifier(recipient, m.getSelfNumber()));
//            SendMessageResultUtils.checkSendMessageResults(results);
//            return results.timestamp();
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        }
//    }
//
//    @Override
//    public void sendContacts() {
//        try {
//            m.sendContacts();
//        } catch (IOException e) {
//            throw new FailureException("SendContacts error: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void sendSyncRequest() {
//        try {
//            m.requestAllSyncData();
//        } catch (IOException e) {
//            throw new FailureException("Request sync data error: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public long sendNoteToSelfMessage(
//            String message, List<String> attachments
//    ) throws InvalidAttachmentException, FailureException, IdentityUntrustedException {
//        try {
//            var results = m.sendMessage(new Message(message,
//                    attachments,
//                    List.of(),
//                    Optional.empty(),
//                    Optional.empty(),
//                    List.of(),
//                    Optional.empty(),
//                    List.of()), Set.of(RecipientIdentifier.NoteToSelf.INSTANCE));
//            SendMessageResultUtils.checkSendMessageResults(results);
//            return results.timestamp();
//        } catch (AttachmentInvalidException e) {
//            throw new InvalidAttachmentException(e.getMessage());
//        } catch (IOException | InvalidStickerException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    @Override
//    public void sendEndSessionMessage(List<String> recipients) {
//        try {
//            var results = m.sendEndSessionMessage(RecipientUtils.getSingleRecipientIdentifiers(recipients,
//                    m.getSelfNumber()));
//            SendMessageResultUtils.checkSendMessageResults(results);
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        }
//    }
//
//    @Override
//    public void deleteRecipient(String recipient) throws FailureException {
//        m.deleteRecipient(RecipientUtils.getSingleRecipientIdentifier(recipient, m.getSelfNumber()));
//    }
//
//    @Override
//    public void deleteContact(String recipient) throws FailureException {
//        m.deleteContact(RecipientUtils.getSingleRecipientIdentifier(recipient, m.getSelfNumber()));
//    }
//
//    @Override
//    public long sendGroupMessage(String message, List<String> attachments, byte[] groupId) {
//        try {
//            var results = m.sendMessage(new Message(message,
//                    attachments,
//                    List.of(),
//                    Optional.empty(),
//                    Optional.empty(),
//                    List.of(),
//                    Optional.empty(),
//                    List.of()), Set.of(RecipientUtils.getGroupRecipientIdentifier(groupId)));
//            SendMessageResultUtils.checkSendMessageResults(results);
//            return results.timestamp();
//        } catch (IOException | InvalidStickerException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        } catch (AttachmentInvalidException e) {
//            throw new InvalidAttachmentException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    @Override
//    public void sendGroupTyping(
//            byte[] groupId, boolean stop
//    ) throws FailureException, GroupException, IdentityUntrustedException {
//        try {
//            var results = m.sendTypingMessage(stop ? TypingAction.STOP : TypingAction.START,
//                    Set.of(RecipientUtils.getGroupRecipientIdentifier(groupId)));
//            SendMessageResultUtils.checkSendMessageResults(results);
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        }
//    }
//
//    @Override
//    public long sendGroupRemoteDeleteMessage(
//            long targetSentTimestamp, byte[] groupId
//    ) {
//        try {
//            var results = m.sendRemoteDeleteMessage(targetSentTimestamp,
//                    Set.of(RecipientUtils.getGroupRecipientIdentifier(groupId)));
//            SendMessageResultUtils.checkSendMessageResults(results);
//            return results.timestamp();
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        }
//    }
//
//    @Override
//    public long sendGroupMessageReaction(
//            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, byte[] groupId
//    ) {
//        try {
//            var results = m.sendMessageReaction(emoji,
//                    remove,
//                    RecipientUtils.getSingleRecipientIdentifier(targetAuthor, m.getSelfNumber()),
//                    targetSentTimestamp,
//                    Set.of(RecipientUtils.getGroupRecipientIdentifier(groupId)),
//                    false);
//            SendMessageResultUtils.checkSendMessageResults(results);
//            return results.timestamp();
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    // Since contact names might be empty if not defined, also potentially return
//    // the profile name
//    @Override
//    public String getContactName(String number) {
//        var name = m.getContactOrProfileName(RecipientUtils.getSingleRecipientIdentifier(number, m.getSelfNumber()));
//        return name == null ? "" : name;
//    }
//
//    @Override
//    public void setContactName(String number, String name) {
//        try {
//            m.setContactName(RecipientUtils.getSingleRecipientIdentifier(number, m.getSelfNumber()), name, "");
//        } catch (NotPrimaryDeviceException e) {
//            throw new FailureException("This command doesn't work on linked devices.");
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    @Override
//    public void setExpirationTimer(String number, int expiration) {
//        try {
//            m.setExpirationTimer(RecipientUtils.getSingleRecipientIdentifier(number, m.getSelfNumber()), expiration);
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    @Override
//    public void setContactBlocked(String number, boolean blocked) {
//        try {
//            m.setContactsBlocked(List.of(RecipientUtils.getSingleRecipientIdentifier(number, m.getSelfNumber())),
//                    blocked);
//        } catch (NotPrimaryDeviceException e) {
//            throw new FailureException("This command doesn't work on linked devices.");
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    @Override
//    @Deprecated
//    public void setGroupBlocked(byte[] groupId, boolean blocked) {
//        try {
//            m.setGroupsBlocked(List.of(RecipientUtils.getGroupId(groupId)), blocked);
//        } catch (NotPrimaryDeviceException e) {
//            throw new FailureException("This command doesn't work on linked devices.");
//        } catch (GroupNotFoundException e) {
//            throw new GroupException(e.getMessage());
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        }
//    }
//
//    @Override
//    @Deprecated
//    public List<byte[]> getGroupIds() {
//        var groups = m.getGroups();
//        return groups.stream().map(g -> g.groupId().serialize()).toList();
//    }
//
//    @Override
//    public DBusPath getGroup(byte[] groupId) {
//        updateGroups();
//        var groupOptional = groups.stream().filter(g -> Arrays.equals(g.getId(), groupId)).findFirst();
//        if (groupOptional.isEmpty()) {
//            throw new GroupException("DbusPropertyGroup not found");
//        }
//        return groupOptional.get().getObjectPath();
//    }
//
//    @Override
//    public List<DbusStructGroup> listGroups() {
//        updateGroups();
//        return groups;
//    }
//
//    @Override
//    @Deprecated
//    public String getGroupName(byte[] groupId) {
//        var group = m.getGroup(RecipientUtils.getGroupId(groupId));
//        if (group == null || group.title() == null) {
//            return "";
//        } else {
//            return group.title();
//        }
//    }
//
//    @Override
//    @Deprecated
//    public List<String> getGroupMembers(byte[] groupId) {
//        var group = m.getGroup(RecipientUtils.getGroupId(groupId));
//        if (group == null) {
//            return List.of();
//        } else {
//            var members = group.members();
//            return RecipientUtils.getRecipientStrings(members);
//        }
//    }
//
//    @Override
//    public byte[] createGroup(
//            String name, List<String> members, String avatar
//    ) throws InvalidAttachmentException, FailureException, NumberInvalidException {
//        return updateGroupInternal(new byte[0], name, members, avatar);
//    }
//
//    @Override
//    @Deprecated
//    public byte[] updateGroup(byte[] groupId, String name, List<String> members, String avatar) {
//        return updateGroupInternal(groupId, name, members, avatar);
//    }
//
//    @Override
//    @Deprecated
//    public boolean isRegistered() {
//        return true;
//    }
//
//    @Override
//    public boolean isRegistered(String number) {
//        var result = isRegistered(List.of(number));
//        return result.getFirst();
//    }
//
//    @Override
//    public List<Boolean> isRegistered(List<String> numbers) {
//        if (numbers.isEmpty()) {
//            return List.of();
//        }
//
//        Map<String, UserStatus> registered;
//        try {
//            registered = m.getUserStatus(new HashSet<>(numbers));
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (RateLimitException e) {
//            throw new FailureException(e.getMessage()
//                    + ", retry at "
//                    + DateUtils.formatTimestamp(e.getNextAttemptTimestamp()));
//        }
//
//        return numbers.stream().map(number -> registered.get(number).uuid() != null).toList();
//    }
//
//    @Override
//    public void addDevice(String uri) {
//        try {
//            var deviceLinkUrl = DeviceLinkUrl.parseDeviceLinkUri(new URI(uri));
//            m.addDeviceLink(deviceLinkUrl);
//        } catch (IOException | InvalidDeviceLinkException e) {
//            throw new FailureException(e.getClass().getSimpleName() + " Add device link failed. " + e.getMessage());
//        } catch (NotPrimaryDeviceException e) {
//            throw new FailureException("This command doesn't work on linked devices.");
//        } catch (URISyntaxException e) {
//            throw new InvalidUriException(e.getClass().getSimpleName()
//                    + " DbusPropertyDevice link uri has invalid format: "
//                    + e.getMessage());
//        }
//    }
//
//    @Override
//    public DBusPath getDevice(long deviceId) {
//        updateDevices();
//        var deviceOptional = devices.stream().filter(g -> g.getId().equals(deviceId)).findFirst();
//        if (deviceOptional.isEmpty()) {
//            throw new DeviceNotFoundException("DbusPropertyDevice not found");
//        }
//        return deviceOptional.get().getObjectPath();
//    }
//
//    @Override
//    public DBusPath getIdentity(String number) throws FailureException {
//        var found = identities.stream()
//                .filter(identity -> identity.getNumber().equals(number) || identity.getUuid().equals(number))
//                .findFirst();
//
//        if (found.isEmpty()) {
//            throw new FailureException("DbusPropertyIdentity for " + number + " unknown");
//        }
//        return found.get().getObjectPath();
//    }
//
//    @Override
//    public List<DbusStructIdentity> listIdentities() {
//        updateIdentities();
//        return identities;
//    }
//
//    @Override
//    public List<DbusStructDevice> listDevices() {
//        updateDevices();
//        return devices;
//    }
//
//    @Override
//    public DBusPath getThisDevice() {
//        updateDevices();
//        return thisDevice;
//    }
//
//    @Override
//    public void updateProfile(
//            String givenName,
//            String familyName,
//            String about,
//            String aboutEmoji,
//            String avatarPath,
//            boolean removeAvatar
//    ) {
//        try {
//            givenName = Util.nullIfEmpty(givenName);
//            familyName = Util.nullIfEmpty(familyName);
//            about = Util.nullIfEmpty(about);
//            aboutEmoji = Util.nullIfEmpty(aboutEmoji);
//            avatarPath = Util.nullIfEmpty(avatarPath);
//            var avatarFile = removeAvatar || avatarPath == null ? null : avatarPath;
//            m.updateProfile(UpdateProfile.newBuilder()
//                    .withGivenName(givenName)
//                    .withFamilyName(familyName)
//                    .withAbout(about)
//                    .withAboutEmoji(aboutEmoji)
//                    .withAvatar(avatarFile)
//                    .withDeleteAvatar(removeAvatar)
//                    .build());
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        }
//    }
//
//    @Override
//    public void updateProfile(
//            String name, String about, String aboutEmoji, String avatarPath, boolean removeAvatar
//    ) {
//        updateProfile(name, "", about, aboutEmoji, avatarPath, removeAvatar);
//    }
//
//    @Override
//    public void removePin() {
//        try {
//            m.setRegistrationLockPin(Optional.empty());
//        } catch (IOException e) {
//            throw new FailureException("Remove pin error: " + e.getMessage());
//        } catch (NotPrimaryDeviceException e) {
//            throw new FailureException("This command doesn't work on linked devices.");
//        }
//    }
//
//    @Override
//    public void setPin(String registrationLockPin) {
//        try {
//            m.setRegistrationLockPin(Optional.of(registrationLockPin));
//        } catch (IOException e) {
//            throw new FailureException("Set pin error: " + e.getMessage());
//        } catch (NotPrimaryDeviceException e) {
//            throw new FailureException("This command doesn't work on linked devices.");
//        }
//    }
//
//    // Provide option to query a version string in order to react on potential
//    // future interface changes
//    @Override
//    public String version() {
//        return BaseConfig.PROJECT_VERSION;
//    }
//
//    // Create a unique list of Numbers from Identities and Contacts to really get
//    // all numbers the system knows
//    @Override
//    public List<String> listNumbers() {
//        return m.getRecipients(false, Optional.empty(), Set.of(), Optional.empty())
//                .stream()
//                .map(r -> r.getAddress().number().orElse(null))
//                .filter(Objects::nonNull)
//                .distinct()
//                .toList();
//    }
//
//    @Override
//    public List<String> getContactNumber(String name) {
//        return m.getRecipients(false, Optional.empty(), Set.of(), Optional.of(name))
//                .stream()
//                .map(r -> r.getAddress().getLegacyIdentifier())
//                .toList();
//    }
//
//    @Override
//    @Deprecated
//    public void quitGroup(byte[] groupId) {
//        var group = RecipientUtils.getGroupId(groupId);
//        try {
//            m.quitGroup(group, Set.of());
//        } catch (GroupNotFoundException | NotAGroupMemberException e) {
//            throw new GroupException(e.getMessage());
//        } catch (IOException | LastGroupAdminException e) {
//            throw new FailureException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    @Override
//    public boolean isContactBlocked(String number) {
//        return m.isContactBlocked(RecipientUtils.getSingleRecipientIdentifier(number, m.getSelfNumber()));
//    }
//
//    @Override
//    @Deprecated
//    public boolean isGroupBlocked(byte[] groupId) {
//        var group = m.getGroup(RecipientUtils.getGroupId(groupId));
//        if (group == null) {
//            return false;
//        } else {
//            return group.isBlocked();
//        }
//    }
//
//    @Override
//    @Deprecated
//    public boolean isMember(byte[] groupId) {
//        var group = m.getGroup(RecipientUtils.getGroupId(groupId));
//        if (group == null) {
//            return false;
//        } else {
//            return group.isMember();
//        }
//    }
//
//    @Override
//    public byte[] joinGroup(String groupLink) {
//        try {
//            var linkUrl = GroupInviteLinkUrl.fromUri(groupLink);
//            if (linkUrl == null) {
//                throw new FailureException("DbusPropertyGroup link is invalid:");
//            }
//            var result = m.joinGroup(linkUrl);
//            return result.first().serialize();
//        } catch (PendingAdminApprovalException e) {
//            throw new FailureException("Pending admin approval: " + e.getMessage());
//        } catch (GroupInviteLinkUrl.InvalidGroupLinkException | InactiveGroupLinkException e) {
//            throw new FailureException("DbusPropertyGroup link is invalid: " + e.getMessage());
//        } catch (GroupInviteLinkUrl.UnknownGroupLinkVersionException e) {
//            throw new FailureException("DbusPropertyGroup link was created with an incompatible version: "
//                    + e.getMessage());
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        }
//    }
//
//    @Override
//    public String uploadStickerPack(String stickerPackPath) {
//        File path = new File(stickerPackPath);
//        try {
//            return m.uploadStickerPack(path).toString();
//        } catch (IOException e) {
//            throw new FailureException("Upload error (maybe image size is too large):" + e.getMessage());
//        } catch (StickerPackInvalidException e) {
//            throw new FailureException("Invalid sticker pack: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void submitRateLimitChallenge(String challenge, String captcha) {
//        try {
//            m.submitRateLimitRecaptchaChallenge(challenge, captcha);
//        } catch (IOException e) {
//            throw new FailureException("Submit challenge error: " + e.getMessage());
//        }
//
//    }
//
//    @Override
//    public void unregister() throws FailureException {
//        try {
//            m.unregister();
//        } catch (IOException e) {
//            throw new FailureException("Failed to unregister: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void deleteAccount() throws FailureException {
//        try {
//            m.deleteAccount();
//        } catch (IOException e) {
//            throw new FailureException("Failed to delete account: " + e.getMessage());
//        }
//    }
//
//    public byte[] updateGroupInternal(byte[] groupId, String name, List<String> members, String avatar) {
//        try {
//            groupId = Util.nullIfEmpty(groupId);
//            name = Util.nullIfEmpty(name);
//            avatar = Util.nullIfEmpty(avatar);
//            var memberIdentifiers = RecipientUtils.getSingleRecipientIdentifiers(members, m.getSelfNumber());
//            if (groupId == null) {
//                var results = m.createGroup(name, memberIdentifiers, avatar);
//                updateGroups();
//                SendMessageResultUtils.checkGroupSendMessageResults(results.second().timestamp(),
//                        results.second().results());
//                return results.first().serialize();
//            } else {
//                var results = m.updateGroup(RecipientUtils.getGroupId(groupId),
//                        UpdateGroup.newBuilder()
//                                .withName(name)
//                                .withMembers(memberIdentifiers)
//                                .withAvatarFile(avatar)
//                                .build());
//                if (results != null) {
//                    SendMessageResultUtils.checkGroupSendMessageResults(results.timestamp(), results.results());
//                }
//                return groupId;
//            }
//        } catch (IOException e) {
//            throw new FailureException(e.getMessage());
//        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
//            throw new GroupException(e.getMessage());
//        } catch (AttachmentInvalidException e) {
//            throw new InvalidAttachmentException(e.getMessage());
//        } catch (UnregisteredRecipientException e) {
//            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
//        }
//    }
//
//    public Manager getManager() {
//        return m;
//    }
//
//    public void updateDevices() {
//        List<Device> linkedDevices;
//        try {
//            linkedDevices = m.getLinkedDevices();
//        } catch (IOException e) {
//            throw new FailureException("Failed to get linked devices: " + e.getMessage());
//        }
//
//        unExportDevices();
//
//        linkedDevices.forEach(d -> {
//            var object = new DbusPropertyDeviceImpl(this, d);
//            var deviceObjectPath = object.getObjectPath();
//            exportObject(object);
//            if (d.isThisDevice()) {
//                thisDevice = new DBusPath(deviceObjectPath);
//            }
//            devices.add(new DbusStructDevice(new DBusPath(deviceObjectPath),
//                    (long) d.id(),
//                    Util.emptyIfNull(d.name())));
//        });
//    }
//
//    private void unExportDevices() {
//        devices.stream()
//                .map(DbusStructDevice::getObjectPath)
//                .map(DBusPath::getPath)
//                .forEach(connection::unExportObject);
//        devices.clear();
//    }
//
//    public void updateGroups() {
//        List<Group> groups;
//        groups = m.getGroups();
//
//        unExportGroups();
//
//        groups.forEach(g -> {
//            var object = new DbusPropertyGroupImpl(this, g.groupId());
//            exportObject(object);
//            this.groups.add(new DbusStructGroup(new DBusPath(object.getObjectPath()),
//                    g.groupId().serialize(),
//                    Util.emptyIfNull(g.title())));
//        });
//    }
//
//    private void unExportGroups() {
//        groups.stream().map(DbusStructGroup::getObjectPath).map(DBusPath::getPath).forEach(connection::unExportObject);
//        groups.clear();
//    }
//
//    private void updateConfiguration() {
//        unExportConfiguration();
//        var object = new DbusPropertyConfigurationImpl(this);
//        exportObject(object);
//    }
//
//    private void unExportConfiguration() {
//        var objectPath = getConfigurationObjectPath(this.objectPath);
//        connection.unExportObject(objectPath);
//    }
//
//    private void exportObject(DBusInterface object) {
//        try {
//            connection.exportObject(object);
//            logger.debug("Exported dbus object: {}", object.getObjectPath());
//        } catch (DBusException e) {
//            logger.warn("Failed to export dbus object ({}): {}", object.getObjectPath(), e.getMessage());
//        }
//    }
//
//    public void updateIdentities() {
//        List<Identity> identities;
//        identities = m.getIdentities();
//
//        unExportIdentities();
//
//        identities.forEach(i -> {
//            var object = new DbusPropertyIdentityImpl(this, i);
//            exportObject(object);
//            this.identities.add(new DbusStructIdentity(new DBusPath(object.getObjectPath()),
//                    i.recipient().uuid().map(UUID::toString).orElse(""),
//                    i.recipient().number().orElse("")));
//        });
//    }
//
//    private void unExportIdentities() {
//        identities.stream()
//                .map(DbusStructIdentity::getObjectPath)
//                .map(DBusPath::getPath)
//                .forEach(connection::unExportObject);
//        identities.clear();
//    }
//
//}

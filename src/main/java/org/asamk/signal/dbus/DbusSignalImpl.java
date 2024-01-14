package org.asamk.signal.dbus;

import org.asamk.Signal;
import org.asamk.signal.BaseConfig;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.GroupException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.InvalidAttachmentException;
import org.asamk.signal.dbus.errors.InvalidGroupIdException;
import org.asamk.signal.dbus.errors.InvalidUriException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.dbus.service.AccountService;
import org.asamk.signal.dbus.service.ConfigurationService;
import org.asamk.signal.dbus.service.ContactService;
import org.asamk.signal.dbus.service.DeviceService;
import org.asamk.signal.dbus.service.GroupService;
import org.asamk.signal.dbus.service.MessageService;
import org.asamk.signal.dbus.service.impl.AccountServiceImpl;
import org.asamk.signal.dbus.service.impl.ConfigurationServiceImpl;
import org.asamk.signal.dbus.service.impl.ContactServiceImpl;
import org.asamk.signal.dbus.service.impl.DeviceServiceImpl;
import org.asamk.signal.dbus.service.impl.GroupServiceImpl;
import org.asamk.signal.dbus.service.impl.MessageServiceImpl;
import org.asamk.signal.dbus.structs.DbusStructDevice;
import org.asamk.signal.dbus.structs.DbusStructGroup;
import org.asamk.signal.dbus.structs.DbusStructIdentity;
import org.asamk.signal.manager.Manager;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DbusSignalImpl implements Signal {

    private static final Logger logger = LoggerFactory.getLogger(DbusSignalImpl.class);

    private final DBusConnection connection;
    private final String objectPath;
    private final boolean noReceiveOnStart;

    private final AccountService accountService;
    private final ConfigurationService configurationService;
    private final ContactService contactService;
    private final DeviceService deviceService;
    private final GroupService groupService;
    private final MessageService messageService;

    public DbusSignalImpl(
            Manager manager, DBusConnection connection, String objectPath, boolean noReceiveOnStart
    ) {
        this.connection = connection;
        this.objectPath = objectPath;
        this.noReceiveOnStart = noReceiveOnStart;

        accountService = new AccountServiceImpl(manager, connection, objectPath);
        contactService = new ContactServiceImpl(manager);
        configurationService = new ConfigurationServiceImpl(manager, connection, objectPath);
        deviceService = new DeviceServiceImpl(manager, connection, objectPath);
        groupService = new GroupServiceImpl(manager, connection, objectPath);
        messageService = new MessageServiceImpl(manager, connection, objectPath);

        manager.addAddressChangedListener(() -> {
            unExportObjects();
            exportObjects();
        });
    }

    @Override
    public void initObjects() {
        exportObjects();
        if (!noReceiveOnStart) {
            subscribeReceive();
        }
    }

    /**
     * Provide option to query a version string in order to react on potential
     * future interface changes
     *
     * @return a version string
     */
    @Override
    public String version() {
        return BaseConfig.PROJECT_VERSION;
    }

    private void unExportObjects() {
        unExportDevices();
        unExportGroups();
        unExportConfiguration();
        unExportIdentities();
        connection.unExportObject(objectPath);
    }

    private void exportObjects() {
        exportObject(this);

        updateDevices();
        updateGroups();
        updateConfiguration();
        updateIdentities();
    }

    private void exportObject(DBusInterface object) {
        try {
            connection.exportObject(object);
            logger.debug("Exported dbus object: {}", object.getObjectPath());
        } catch (DBusException e) {
            logger.warn("Failed to export dbus object ({}): {}", object.getObjectPath(), e.getMessage());
        }
    }

    @Override
    public DBusPath getIdentity(String number) {
        return accountService.getIdentity(number);
    }

    @Override
    public String getSelfNumber() {
        return accountService.getSelfNumber();
    }

    @Override
    public List<DbusStructIdentity> listIdentities() {
        return accountService.listIdentities();
    }

    @Override
    public List<String> listNumbers() {
        return accountService.listNumbers();
    }

    @Override
    public void updateProfile(
            String givenName,
            String familyName,
            String about,
            String aboutEmoji,
            String avatarPath,
            boolean removeAvatar
    ) throws FailureException {
        accountService.updateProfile(givenName, familyName, about, aboutEmoji, avatarPath, removeAvatar);
    }

    @Override
    public void updateProfile(
            String name, String about, String aboutEmoji, String avatarPath, boolean removeAvatar
    ) throws FailureException {
        accountService.updateProfile(name, about, aboutEmoji, avatarPath, removeAvatar);
    }

    @Override
    public void setExpirationTimer(String number, int expiration) throws FailureException {
        accountService.setExpirationTimer(number, expiration);
    }

    @Override
    public void setPin(String registrationLockPin) {
        accountService.setPin(registrationLockPin);
    }

    @Override
    public void submitRateLimitChallenge(String challenge, String captchaString) throws FailureException {
        accountService.submitRateLimitChallenge(challenge, captchaString);
    }

    @Override
    public String uploadStickerPack(String stickerPackPath) throws FailureException {
        return accountService.uploadStickerPack(stickerPackPath);
    }

    @Override
    public void removePin() {
        accountService.removePin();
    }

    @Override
    public void deleteAccount() throws FailureException {
        accountService.deleteAccount();
    }

    @Override
    public void updateIdentities() {
        accountService.updateIdentities();
    }

    @Override
    public void unExportIdentities() {
        accountService.unExportIdentities();
    }

    @Override
    public void updateConfiguration() {
        configurationService.updateConfiguration();
    }

    @Override
    public void unExportConfiguration() {
        configurationService.unExportConfiguration();
    }

    @Override
    public void sendContacts() throws FailureException {
        contactService.sendContacts();
    }

    @Override
    public void deleteContact(String recipient) throws FailureException {
        contactService.deleteContact(recipient);
    }

    @Override
    public String getContactName(String number) throws NumberInvalidException {
        return contactService.getContactName(number);
    }

    @Override
    public void setContactName(String number, String name) throws NumberInvalidException {
        contactService.setContactName(number, name);

    }

    @Override
    public void setContactBlocked(String number, boolean blocked) throws NumberInvalidException {
        contactService.setContactBlocked(number, blocked);
    }

    @Override
    public List<String> getContactNumber(String name) throws FailureException {
        return contactService.getContactNumber(name);
    }

    @Override
    public boolean isContactBlocked(String number) throws NumberInvalidException {
        return contactService.isContactBlocked(number);
    }

    @Deprecated
    @Override
    public boolean isRegistered() throws FailureException, NumberInvalidException {
        return deviceService.isRegistered();
    }

    @Override
    public boolean isRegistered(String number) throws FailureException, NumberInvalidException {
        return deviceService.isRegistered(number);
    }

    @Override
    public List<Boolean> isRegistered(List<String> numbers) throws FailureException, NumberInvalidException {
        return deviceService.isRegistered(numbers);
    }

    @Override
    public void unregister() throws FailureException {
        deviceService.unregister();
    }

    @Override
    public void addDevice(String uri) throws InvalidUriException {
        deviceService.addDevice(uri);
    }

    @Override
    public DBusPath getDevice(long deviceId) {
        return deviceService.getDevice(deviceId);
    }

    @Override
    public DBusPath getThisDevice() {
        return deviceService.getThisDevice();
    }

    @Override
    public List<DbusStructDevice> listDevices() throws FailureException {
        return deviceService.listDevices();
    }

    @Override
    public void updateDevices() {
        deviceService.updateDevices();
    }

    @Override
    public void unExportDevices() {
        deviceService.unExportDevices();
    }

    @Override
    public long sendGroupMessage(
            String message, List<String> attachments, byte[] groupId
    ) throws GroupException, FailureException, InvalidAttachmentException, InvalidGroupIdException {
        return groupService.sendGroupMessage(message, attachments, groupId);
    }

    @Override
    public void sendGroupTyping(
            byte[] groupId, boolean stop
    ) throws FailureException, GroupException, IdentityUntrustedException {
        groupService.sendGroupTyping(groupId, stop);
    }

    @Override
    public long sendGroupRemoteDeleteMessage(
            long targetSentTimestamp, byte[] groupId
    ) throws FailureException, GroupException, InvalidGroupIdException {
        return groupService.sendGroupRemoteDeleteMessage(targetSentTimestamp, groupId);
    }

    @Override
    public long sendGroupMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, byte[] groupId
    ) throws GroupException, FailureException, NumberInvalidException, InvalidGroupIdException {
        return groupService.sendGroupMessageReaction(emoji, remove, targetAuthor, targetSentTimestamp, groupId);
    }

    @Deprecated
    @Override
    public void setGroupBlocked(
            byte[] groupId, boolean blocked
    ) throws GroupException, InvalidGroupIdException {
        groupService.setGroupBlocked(groupId, blocked);
    }

    @Deprecated
    @Override
    public List<byte[]> getGroupIds() {
        return groupService.getGroupIds();
    }

    @Override
    public DBusPath getGroup(byte[] groupId) {
        return groupService.getGroup(groupId);
    }

    @Override
    public List<DbusStructGroup> listGroups() {
        return groupService.listGroups();
    }

    @Deprecated
    @Override
    public String getGroupName(byte[] groupId) throws InvalidGroupIdException {
        return groupService.getGroupName(groupId);
    }

    @Deprecated
    @Override
    public List<String> getGroupMembers(byte[] groupId) throws InvalidGroupIdException {
        return groupService.getGroupMembers(groupId);
    }

    @Override
    public byte[] createGroup(
            String name, List<String> members, String avatar
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException {
        return groupService.createGroup(name, members, avatar);
    }

    @Deprecated
    @Override
    public byte[] updateGroup(
            byte[] groupId, String name, List<String> members, String avatar
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, GroupException, InvalidGroupIdException {
        return groupService.updateGroup(groupId, name, members, avatar);
    }

    @Deprecated
    @Override
    public void quitGroup(byte[] groupId) throws GroupException, FailureException, InvalidGroupIdException {
        groupService.quitGroup(groupId);
    }

    @Deprecated
    @Override
    public boolean isGroupBlocked(byte[] groupId) throws InvalidGroupIdException {
        return groupService.isGroupBlocked(groupId);
    }

    @Deprecated
    @Override
    public boolean isMember(byte[] groupId) throws InvalidGroupIdException {
        return groupService.isMember(groupId);
    }

    @Override
    public byte[] joinGroup(String groupLink) throws FailureException {
        return groupService.joinGroup(groupLink);
    }

    @Override
    public void updateGroups() {
        groupService.updateGroups();
    }

    @Override
    public void unExportGroups() {
        groupService.unExportGroups();
    }

    @Override
    public long sendMessage(
            String message, List<String> attachments, String recipient
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, IdentityUntrustedException {
        return messageService.sendMessage(message, attachments, recipient);
    }

    @Override
    public long sendMessage(
            String message, List<String> attachments, List<String> recipients
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, IdentityUntrustedException {
        return messageService.sendMessage(message, attachments, recipients);
    }

    @Override
    public void sendTyping(
            String recipient, boolean stop
    ) throws FailureException, IdentityUntrustedException {
        messageService.sendTyping(recipient, stop);
    }

    @Override
    public void sendReadReceipt(
            String recipient, List<Long> messageIds
    ) throws FailureException, IdentityUntrustedException {
        messageService.sendReadReceipt(recipient, messageIds);
    }

    @Override
    public void sendViewedReceipt(
            String recipient, List<Long> messageIds
    ) throws FailureException, IdentityUntrustedException {
        messageService.sendViewedReceipt(recipient, messageIds);
    }

    @Override
    public long sendRemoteDeleteMessage(
            long targetSentTimestamp, String recipient
    ) throws FailureException, NumberInvalidException {
        return messageService.sendRemoteDeleteMessage(targetSentTimestamp, recipient);
    }

    @Override
    public long sendRemoteDeleteMessage(
            long targetSentTimestamp, List<String> recipients
    ) throws FailureException, NumberInvalidException {
        return messageService.sendRemoteDeleteMessage(targetSentTimestamp, recipients);
    }

    @Override
    public long sendMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, String recipient
    ) throws NumberInvalidException, FailureException {
        return messageService.sendMessageReaction(emoji, remove, targetAuthor, targetSentTimestamp, recipient);
    }

    @Override
    public long sendMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, List<String> recipients
    ) throws NumberInvalidException, FailureException {
        return messageService.sendMessageReaction(emoji, remove, targetAuthor, targetSentTimestamp, recipients);
    }

    @Override
    public long sendNoteToSelfMessage(
            String message, List<String> attachments
    ) throws InvalidAttachmentException, FailureException {
        return messageService.sendNoteToSelfMessage(message, attachments);
    }

    @Override
    public void sendEndSessionMessage(List<String> recipients) throws FailureException, NumberInvalidException, IdentityUntrustedException {
        messageService.sendEndSessionMessage(recipients);
    }

    @Override
    public void deleteRecipient(String recipient) throws FailureException {
        messageService.deleteRecipient(recipient);
    }

    @Override
    public long sendPaymentNotification(
            byte[] receipt, String note, String recipient
    ) throws FailureException {
        return messageService.sendPaymentNotification(receipt, note, recipient);
    }

    @Override
    public void subscribeReceive() {
        messageService.subscribeReceive();
    }

    @Override
    public void unsubscribeReceive() {
        messageService.unsubscribeReceive();
    }

    @Override
    public void sendSyncRequest() throws FailureException {
        messageService.sendSyncRequest();
    }

    @Override
    public void close() {
        messageService.close();
        unExportObjects();
    }

    /**
     * Returns the path of this object.
     *
     * @return string
     */
    @Override
    public String getObjectPath() {
        return objectPath;
    }

}

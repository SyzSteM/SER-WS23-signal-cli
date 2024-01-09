package org.asamk;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.GroupException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.InvalidAttachmentException;
import org.asamk.signal.dbus.errors.InvalidGroupIdException;
import org.asamk.signal.dbus.errors.InvalidUriException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.manager.api.LastGroupAdminException;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

import java.util.List;
import java.util.Map;

/**
 * DBus interface for the org.asamk.Signal service.
 * Including emitted Signals and returned Errors.
 */
public interface Signal extends DBusInterface {

    String getSelfNumber();

    void subscribeReceive();

    void unsubscribeReceive();

    long sendMessage(
            String message, List<String> attachments, String recipient
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, IdentityUntrustedException;

    long sendMessage(
            String message, List<String> attachments, List<String> recipients
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, IdentityUntrustedException;

    void sendTyping(
            String recipient, boolean stop
    ) throws FailureException, IdentityUntrustedException;

    void sendReadReceipt(
            String recipient, List<Long> messageIds
    ) throws FailureException, IdentityUntrustedException;

    void sendViewedReceipt(
            String recipient, List<Long> messageIds
    ) throws FailureException, IdentityUntrustedException;

    long sendRemoteDeleteMessage(
            long targetSentTimestamp, String recipient
    ) throws FailureException, NumberInvalidException;

    long sendRemoteDeleteMessage(
            long targetSentTimestamp, List<String> recipients
    ) throws FailureException, NumberInvalidException;

    long sendMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, String recipient
    ) throws NumberInvalidException, FailureException;

    long sendMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, List<String> recipients
    ) throws NumberInvalidException, FailureException;

    long sendPaymentNotification(byte[] receipt, String note, String recipient) throws FailureException;

    void sendContacts() throws FailureException;

    void sendSyncRequest() throws FailureException;

    long sendNoteToSelfMessage(
            String message, List<String> attachments
    ) throws InvalidAttachmentException, FailureException;

    void sendEndSessionMessage(List<String> recipients) throws FailureException, NumberInvalidException, IdentityUntrustedException;

    void deleteRecipient(final String recipient) throws FailureException;

    void deleteContact(final String recipient) throws FailureException;

    long sendGroupMessage(
            String message, List<String> attachments, byte[] groupId
    ) throws GroupException, FailureException, InvalidAttachmentException, InvalidGroupIdException;

    void sendGroupTyping(
            final byte[] groupId, final boolean stop
    ) throws FailureException, GroupException, IdentityUntrustedException;

    long sendGroupRemoteDeleteMessage(
            long targetSentTimestamp, byte[] groupId
    ) throws FailureException, GroupException, InvalidGroupIdException;

    long sendGroupMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, byte[] groupId
    ) throws GroupException, FailureException, NumberInvalidException, InvalidGroupIdException;

    String getContactName(String number) throws NumberInvalidException;

    void setContactName(String number, String name) throws NumberInvalidException;

    void setExpirationTimer(final String number, final int expiration) throws FailureException;

    void setContactBlocked(String number, boolean blocked) throws NumberInvalidException;

    @Deprecated
    void setGroupBlocked(byte[] groupId, boolean blocked) throws GroupException, InvalidGroupIdException;

    @Deprecated
    List<byte[]> getGroupIds();

    DBusPath getGroup(byte[] groupId);

    List<StructGroup> listGroups();

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
    boolean isRegistered() throws FailureException, NumberInvalidException;

    boolean isRegistered(String number) throws FailureException, NumberInvalidException;

    List<Boolean> isRegistered(List<String> numbers) throws FailureException, NumberInvalidException;

    void addDevice(String uri) throws InvalidUriException;

    DBusPath getDevice(long deviceId);

    DBusPath getIdentity(String number);

    List<StructIdentity> listIdentities();

    List<StructDevice> listDevices() throws FailureException;

    DBusPath getThisDevice();

    void updateProfile(
            String givenName,
            String familyName,
            String about,
            String aboutEmoji,
            String avatarPath,
            boolean removeAvatar
    ) throws FailureException;

    void updateProfile(
            String name, String about, String aboutEmoji, String avatarPath, boolean removeAvatar
    ) throws FailureException;

    void removePin();

    void setPin(String registrationLockPin);

    String version();

    List<String> listNumbers();

    List<String> getContactNumber(final String name) throws FailureException;

    @Deprecated
    void quitGroup(final byte[] groupId) throws GroupException, FailureException, InvalidGroupIdException;

    boolean isContactBlocked(final String number) throws NumberInvalidException;

    @Deprecated
    boolean isGroupBlocked(final byte[] groupId) throws InvalidGroupIdException;

    @Deprecated
    boolean isMember(final byte[] groupId) throws InvalidGroupIdException;

    byte[] joinGroup(final String groupLink) throws FailureException;

    String uploadStickerPack(String stickerPackPath) throws FailureException;

    void submitRateLimitChallenge(String challenge, String captchaString) throws FailureException;

    void unregister() throws FailureException;

    void deleteAccount() throws FailureException;

    class MessageReceivedV2 extends DBusSignal {

        private final long timestamp;
        private final String sender;
        private final byte[] groupId;
        private final String message;
        private final Map<String, Variant<?>> extras;

        public MessageReceivedV2(
                String objectpath,
                long timestamp,
                String sender,
                byte[] groupId,
                String message,
                final Map<String, Variant<?>> extras
        ) throws DBusException {
            super(objectpath, timestamp, sender, groupId, message, extras);
            this.timestamp = timestamp;
            this.sender = sender;
            this.groupId = groupId;
            this.message = message;
            this.extras = extras;
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

        public Map<String, Variant<?>> getExtras() {
            return extras;
        }
    }

    class EditMessageReceived extends DBusSignal {

        private final long timestamp;
        private final long targetSentTimestamp;
        private final String sender;
        private final byte[] groupId;
        private final String message;
        private final Map<String, Variant<?>> extras;

        public EditMessageReceived(
                String objectpath,
                long timestamp,
                final long targetSentTimestamp,
                String sender,
                byte[] groupId,
                String message,
                final Map<String, Variant<?>> extras
        ) throws DBusException {
            super(objectpath, timestamp, targetSentTimestamp, sender, groupId, message, extras);
            this.timestamp = timestamp;
            this.targetSentTimestamp = targetSentTimestamp;
            this.sender = sender;
            this.groupId = groupId;
            this.message = message;
            this.extras = extras;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getTargetSentTimestamp() {
            return targetSentTimestamp;
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

        public Map<String, Variant<?>> getExtras() {
            return extras;
        }
    }

    class MessageReceived extends DBusSignal {

        private final long timestamp;
        private final String sender;
        private final byte[] groupId;
        private final String message;
        private final List<String> attachments;

        public MessageReceived(
                String objectpath,
                long timestamp,
                String sender,
                byte[] groupId,
                String message,
                List<String> attachments
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

    class ReceiptReceived extends DBusSignal {

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

    class ReceiptReceivedV2 extends DBusSignal {

        private final long timestamp;
        private final String sender;
        private final String type;
        private final Map<String, Variant<?>> extras;

        public ReceiptReceivedV2(
                String objectpath,
                long timestamp,
                String sender,
                final String type,
                final Map<String, Variant<?>> extras
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

    class SyncMessageReceived extends DBusSignal {

        private final long timestamp;
        private final String source;
        private final String destination;
        private final byte[] groupId;
        private final String message;
        private final List<String> attachments;

        public SyncMessageReceived(
                String objectpath,
                long timestamp,
                String source,
                String destination,
                byte[] groupId,
                String message,
                List<String> attachments
        ) throws DBusException {
            super(objectpath, timestamp, source, destination, groupId, message, attachments);
            this.timestamp = timestamp;
            this.source = source;
            this.destination = destination;
            this.groupId = groupId;
            this.message = message;
            this.attachments = attachments;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSource() {
            return source;
        }

        public String getDestination() {
            return destination;
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

    class SyncMessageReceivedV2 extends DBusSignal {

        private final long timestamp;
        private final String source;
        private final String destination;
        private final byte[] groupId;
        private final String message;
        private final Map<String, Variant<?>> extras;

        public SyncMessageReceivedV2(
                String objectpath,
                long timestamp,
                String source,
                String destination,
                byte[] groupId,
                String message,
                final Map<String, Variant<?>> extras
        ) throws DBusException {
            super(objectpath, timestamp, source, destination, groupId, message, extras);
            this.timestamp = timestamp;
            this.source = source;
            this.destination = destination;
            this.groupId = groupId;
            this.message = message;
            this.extras = extras;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSource() {
            return source;
        }

        public String getDestination() {
            return destination;
        }

        public byte[] getGroupId() {
            return groupId;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, Variant<?>> getExtras() {
            return extras;
        }
    }

    class StructDevice extends Struct {

        @Position(0)
        final DBusPath objectPath;

        @Position(1)
        final Long id;

        @Position(2)
        final String name;

        public StructDevice(final DBusPath objectPath, final Long id, final String name) {
            this.objectPath = objectPath;
            this.id = id;
            this.name = name;
        }

        public DBusPath getObjectPath() {
            return objectPath;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @DBusProperty(name = "Id", type = Integer.class, access = DBusProperty.Access.READ)
    @DBusProperty(name = "Name", type = String.class)
    @DBusProperty(name = "Created", type = String.class, access = DBusProperty.Access.READ)
    @DBusProperty(name = "LastSeen", type = String.class, access = DBusProperty.Access.READ)
    interface Device extends DBusInterface, Properties {

        void removeDevice() throws FailureException;
    }

    @DBusProperty(name = "ReadReceipts", type = Boolean.class)
    @DBusProperty(name = "UnidentifiedDeliveryIndicators", type = Boolean.class)
    @DBusProperty(name = "TypingIndicators", type = Boolean.class)
    @DBusProperty(name = "LinkPreviews", type = Boolean.class)
    interface Configuration extends DBusInterface, Properties {}

    class StructGroup extends Struct {

        @Position(0)
        final DBusPath objectPath;

        @Position(1)
        final byte[] id;

        @Position(2)
        final String name;

        public StructGroup(final DBusPath objectPath, final byte[] id, final String name) {
            this.objectPath = objectPath;
            this.id = id;
            this.name = name;
        }

        public DBusPath getObjectPath() {
            return objectPath;
        }

        public byte[] getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

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
    interface Group extends DBusInterface, Properties {

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

    class StructIdentity extends Struct {

        @Position(0)
        final DBusPath objectPath;

        @Position(1)
        final String uuid;

        @Position(2)
        final String number;

        public StructIdentity(final DBusPath objectPath, final String uuid, final String number) {
            this.objectPath = objectPath;
            this.uuid = uuid;
            this.number = number;
        }

        public DBusPath getObjectPath() {
            return objectPath;
        }

        public String getUuid() {
            return uuid;
        }

        public String getNumber() {
            return number;
        }
    }

    @DBusProperty(name = "Number", type = String.class, access = DBusProperty.Access.READ)
    @DBusProperty(name = "Uuid", type = String.class, access = DBusProperty.Access.READ)
    @DBusProperty(name = "Fingerprint", type = Byte[].class, access = DBusProperty.Access.READ)
    @DBusProperty(name = "SafetyNumber", type = String.class, access = DBusProperty.Access.READ)
    @DBusProperty(name = "TrustLevel", type = String.class, access = DBusProperty.Access.READ)
    @DBusProperty(name = "AddedDate", type = Integer.class, access = DBusProperty.Access.READ)
    @DBusProperty(name = "ScannableSafetyNumber", type = Byte[].class, access = DBusProperty.Access.READ)
    interface Identity extends DBusInterface, Properties {

        void trust() throws FailureException;

        void trustVerified(String safetyNumber) throws FailureException;
    }

}

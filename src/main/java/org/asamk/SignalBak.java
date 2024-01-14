package org.asamk;

import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * DBus interface for the org.asamk.Signal service.
 */
public interface SignalBak extends DBusInterface {

//    String getSelfNumber();

//    void subscribeReceive();
//
//    void unsubscribeReceive();

//    long sendMessage(
//            String message, List<String> attachments, String recipient
//    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, IdentityUntrustedException;
//
//    long sendMessage(
//            String message, List<String> attachments, List<String> recipients
//    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, IdentityUntrustedException;
//
//    void sendTyping(
//            String recipient, boolean stop
//    ) throws FailureException, IdentityUntrustedException;

//    void sendReadReceipt(
//            String recipient, List<Long> messageIds
//    ) throws FailureException, IdentityUntrustedException;
//
//    void sendViewedReceipt(
//            String recipient, List<Long> messageIds
//    ) throws FailureException, IdentityUntrustedException;

//    long sendRemoteDeleteMessage(
//            long targetSentTimestamp, String recipient
//    ) throws FailureException, NumberInvalidException;
//
//    long sendRemoteDeleteMessage(
//            long targetSentTimestamp, List<String> recipients
//    ) throws FailureException, NumberInvalidException;
//
//    long sendMessageReaction(
//            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, String recipient
//    ) throws NumberInvalidException, FailureException;
//
//    long sendMessageReaction(
//            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, List<String> recipients
//    ) throws NumberInvalidException, FailureException;

//    long sendPaymentNotification(byte[] receipt, String note, String recipient) throws FailureException;

//    void sendContacts() throws FailureException;

//    void sendSyncRequest() throws FailureException;

//    long sendNoteToSelfMessage(
//            String message, List<String> attachments
//    ) throws InvalidAttachmentException, FailureException;

//    void sendEndSessionMessage(List<String> recipients) throws FailureException, NumberInvalidException, IdentityUntrustedException;

//    void deleteRecipient(final String recipient) throws FailureException;

//    void deleteContact(final String recipient) throws FailureException;

//    long sendGroupMessage(
//            String message, List<String> attachments, byte[] groupId
//    ) throws GroupException, FailureException, InvalidAttachmentException, InvalidGroupIdException;
//
//    void sendGroupTyping(
//            final byte[] groupId, final boolean stop
//    ) throws FailureException, GroupException, IdentityUntrustedException;
//
//    long sendGroupRemoteDeleteMessage(
//            long targetSentTimestamp, byte[] groupId
//    ) throws FailureException, GroupException, InvalidGroupIdException;
//
//    long sendGroupMessageReaction(
//            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, byte[] groupId
//    ) throws GroupException, FailureException, NumberInvalidException, InvalidGroupIdException;

//    String getContactName(String number) throws NumberInvalidException;

//    void setContactName(String number, String name) throws NumberInvalidException;

//    void setExpirationTimer(final String number, final int expiration) throws FailureException;

//    void setContactBlocked(String number, boolean blocked) throws NumberInvalidException;

//    @Deprecated
//    void setGroupBlocked(byte[] groupId, boolean blocked) throws GroupException, InvalidGroupIdException;
//
//    @Deprecated
//    List<byte[]> getGroupIds();
//
//    DBusPath getGroup(byte[] groupId);
//
//    List<DbusStructGroup> listGroups();
//
//    @Deprecated
//    String getGroupName(byte[] groupId) throws InvalidGroupIdException;
//
//    @Deprecated
//    List<String> getGroupMembers(byte[] groupId) throws InvalidGroupIdException;
//
//    byte[] createGroup(
//            String name, List<String> members, String avatar
//    ) throws InvalidAttachmentException, FailureException, NumberInvalidException;
//
//    @Deprecated
//    byte[] updateGroup(
//            byte[] groupId, String name, List<String> members, String avatar
//    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, GroupException, InvalidGroupIdException;

//    @Deprecated
//    boolean isRegistered() throws FailureException, NumberInvalidException;
//
//    boolean isRegistered(String number) throws FailureException, NumberInvalidException;
//
//    List<Boolean> isRegistered(List<String> numbers) throws FailureException, NumberInvalidException;

//    void addDevice(String uri) throws InvalidUriException;
//
//    DBusPath getDevice(long deviceId);
//
//    DBusPath getIdentity(String number);

//    List<DbusStructIdentity> listIdentities();

//    List<DbusStructDevice> listDevices() throws FailureException;

//    DBusPath getThisDevice();

//    void updateProfile(
//            String givenName,
//            String familyName,
//            String about,
//            String aboutEmoji,
//            String avatarPath,
//            boolean removeAvatar
//    ) throws FailureException;

//    void updateProfile(
//            String name, String about, String aboutEmoji, String avatarPath, boolean removeAvatar
//    ) throws FailureException;

//    void removePin();
//
//    void setPin(String registrationLockPin);

//    String version();

//    List<String> listNumbers();

//    List<String> getContactNumber(final String name) throws FailureException;

//    @Deprecated
//    void quitGroup(final byte[] groupId) throws GroupException, FailureException, InvalidGroupIdException;

//    boolean isContactBlocked(final String number) throws NumberInvalidException;

//    @Deprecated
//    boolean isGroupBlocked(final byte[] groupId) throws InvalidGroupIdException;

//    @Deprecated
//    boolean isMember(final byte[] groupId) throws InvalidGroupIdException;

//    byte[] joinGroup(final String groupLink) throws FailureException;

//    String uploadStickerPack(String stickerPackPath) throws FailureException;

//    void submitRateLimitChallenge(String challenge, String captchaString) throws FailureException;

//    void unregister() throws FailureException;

//    void deleteAccount() throws FailureException;

}

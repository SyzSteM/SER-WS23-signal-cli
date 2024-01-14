package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.DbusReceiveMessageHandler;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.GroupException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.InvalidAttachmentException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.dbus.service.MessageService;
import org.asamk.signal.dbus.util.RecipientUtils;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.AttachmentInvalidException;
import org.asamk.signal.manager.api.GroupNotFoundException;
import org.asamk.signal.manager.api.GroupSendingNotAllowedException;
import org.asamk.signal.manager.api.InvalidStickerException;
import org.asamk.signal.manager.api.Message;
import org.asamk.signal.manager.api.NotAGroupMemberException;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.asamk.signal.manager.api.TypingAction;
import org.asamk.signal.manager.api.UnregisteredRecipientException;
import org.asamk.signal.util.SendMessageResultUtils;
import org.freedesktop.dbus.connections.impl.DBusConnection;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageServiceImpl implements MessageService {

    private final Manager manager;
    private final DBusConnection connection;
    private final String objectPath;

    private DbusReceiveMessageHandler dbusMessageHandler;
    private int subscriberCount;

    public MessageServiceImpl(Manager manager, DBusConnection connection, String objectPath) {
        this.manager = manager;
        this.connection = connection;
        this.objectPath = objectPath;
    }

    @Override
    public long sendMessage(
            String message, List<String> attachments, String recipient
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, IdentityUntrustedException {
        return sendMessage(message, attachments, List.of(recipient));
    }

    @Override
    public long sendMessage(
            String message, List<String> attachments, List<String> recipients
    ) throws InvalidAttachmentException, FailureException, NumberInvalidException, IdentityUntrustedException {
        try {
            var results = manager.sendMessage(new Message(message,
                            attachments,
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            List.of(),
                            Optional.empty(),
                            List.of()),
                    RecipientUtils.getSingleRecipientIdentifiers(recipients, manager.getSelfNumber())
                            .stream()
                            .map(RecipientIdentifier.class::cast)
                            .collect(Collectors.toSet()));

            SendMessageResultUtils.checkSendMessageResults(results);
            return results.timestamp();
        } catch (AttachmentInvalidException e) {
            throw new InvalidAttachmentException(e.getMessage());
        } catch (IOException | InvalidStickerException e) {
            throw new FailureException(e);
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Override
    public void sendTyping(
            String recipient, boolean stop
    ) throws FailureException, IdentityUntrustedException {
        try {
            var results = manager.sendTypingMessage(stop ? TypingAction.STOP : TypingAction.START,
                    RecipientUtils.getSingleRecipientIdentifiers(List.of(recipient), manager.getSelfNumber())
                            .stream()
                            .map(RecipientIdentifier.class::cast)
                            .collect(Collectors.toSet()));
            SendMessageResultUtils.checkSendMessageResults(results);
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        }
    }

    @Override
    public void sendReadReceipt(
            String recipient, List<Long> messageIds
    ) throws FailureException, IdentityUntrustedException {
        var results = manager.sendReadReceipt(RecipientUtils.getSingleRecipientIdentifier(recipient,
                manager.getSelfNumber()), messageIds);
        SendMessageResultUtils.checkSendMessageResults(results);
    }

    @Override
    public void sendViewedReceipt(
            String recipient, List<Long> messageIds
    ) throws FailureException, IdentityUntrustedException {
        var results = manager.sendViewedReceipt(RecipientUtils.getSingleRecipientIdentifier(recipient,
                manager.getSelfNumber()), messageIds);
        SendMessageResultUtils.checkSendMessageResults(results);

    }

    @Override
    public long sendRemoteDeleteMessage(
            long targetSentTimestamp, String recipient
    ) throws FailureException, NumberInvalidException {
        return sendRemoteDeleteMessage(targetSentTimestamp, List.of(recipient));
    }

    @Override
    public long sendRemoteDeleteMessage(
            long targetSentTimestamp, List<String> recipients
    ) throws FailureException, NumberInvalidException {
        try {
            var results = manager.sendRemoteDeleteMessage(targetSentTimestamp,
                    RecipientUtils.getSingleRecipientIdentifiers(recipients, manager.getSelfNumber())
                            .stream()
                            .map(RecipientIdentifier.class::cast)
                            .collect(Collectors.toSet()));
            SendMessageResultUtils.checkSendMessageResults(results);
            return results.timestamp();
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        }

    }

    @Override
    public long sendMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, String recipient
    ) throws NumberInvalidException, FailureException {
        return sendMessageReaction(emoji, remove, targetAuthor, targetSentTimestamp, List.of(recipient));
    }

    @Override
    public long sendMessageReaction(
            String emoji, boolean remove, String targetAuthor, long targetSentTimestamp, List<String> recipients
    ) throws NumberInvalidException, FailureException {
        try {
            var results = manager.sendMessageReaction(emoji,
                    remove,
                    RecipientUtils.getSingleRecipientIdentifier(targetAuthor, manager.getSelfNumber()),
                    targetSentTimestamp,
                    RecipientUtils.getSingleRecipientIdentifiers(recipients, manager.getSelfNumber())
                            .stream()
                            .map(RecipientIdentifier.class::cast)
                            .collect(Collectors.toSet()),
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

    @Override
    public long sendNoteToSelfMessage(
            String message, List<String> attachments
    ) throws InvalidAttachmentException, FailureException {
        try {
            var results = manager.sendMessage(new Message(message,
                    attachments,
                    List.of(),
                    Optional.empty(),
                    Optional.empty(),
                    List.of(),
                    Optional.empty(),
                    List.of()), Set.of(RecipientIdentifier.NoteToSelf.INSTANCE));
            SendMessageResultUtils.checkSendMessageResults(results);
            return results.timestamp();
        } catch (AttachmentInvalidException e) {
            throw new InvalidAttachmentException(e.getMessage());
        } catch (IOException | InvalidStickerException e) {
            throw new FailureException(e.getMessage());
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new GroupException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Override
    public void sendEndSessionMessage(List<String> recipients) throws FailureException, NumberInvalidException, IdentityUntrustedException {
        try {
            var results = manager.sendEndSessionMessage(RecipientUtils.getSingleRecipientIdentifiers(recipients,
                    manager.getSelfNumber()));
            SendMessageResultUtils.checkSendMessageResults(results);
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

    @Override
    public void deleteRecipient(String recipient) throws FailureException {
        manager.deleteRecipient(RecipientUtils.getSingleRecipientIdentifier(recipient, manager.getSelfNumber()));
    }

    @Override
    public long sendPaymentNotification(
            byte[] receipt, String note, String recipient
    ) throws FailureException {
        try {
            var results = manager.sendPaymentNotificationMessage(receipt,
                    note,
                    RecipientUtils.getSingleRecipientIdentifier(recipient, manager.getSelfNumber()));
            SendMessageResultUtils.checkSendMessageResults(results);
            return results.timestamp();
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

    @Override
    public void subscribeReceive() {
        if (dbusMessageHandler == null) {
            dbusMessageHandler = new DbusReceiveMessageHandler(connection, objectPath);
            manager.addReceiveHandler(dbusMessageHandler);
        }
        subscriberCount++;
    }

    @Override
    public void unsubscribeReceive() {
        subscriberCount = Math.max(0, subscriberCount - 1);
        if (subscriberCount == 0 && dbusMessageHandler != null) {
            manager.removeReceiveHandler(dbusMessageHandler);
            dbusMessageHandler = null;
        }
    }

    @Override
    public void sendSyncRequest() throws FailureException {
        try {
            manager.requestAllSyncData();
        } catch (IOException e) {
            throw new FailureException("Request sync data error: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (dbusMessageHandler != null) {
            manager.removeReceiveHandler(dbusMessageHandler);
            dbusMessageHandler = null;
        }
    }

}

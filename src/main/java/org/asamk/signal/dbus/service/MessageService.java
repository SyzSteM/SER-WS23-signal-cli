package org.asamk.signal.dbus.service;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.InvalidAttachmentException;
import org.asamk.signal.dbus.errors.NumberInvalidException;

import java.util.List;

public interface MessageService {

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

    long sendNoteToSelfMessage(
            String message, List<String> attachments
    ) throws InvalidAttachmentException, FailureException;

    void sendEndSessionMessage(
            List<String> recipients
    ) throws FailureException, NumberInvalidException, IdentityUntrustedException;

    void deleteRecipient(String recipient) throws FailureException;

    long sendPaymentNotification(byte[] receipt, String note, String recipient) throws FailureException;

    void subscribeReceive();

    void unsubscribeReceive();

    void sendSyncRequest() throws FailureException;

    void close();
}

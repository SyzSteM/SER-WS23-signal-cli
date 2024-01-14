package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.DbusReceiveMessageHandler;
import org.asamk.signal.dbus.service.MessageService;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.Message;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.asamk.signal.manager.api.SendMessageResults;
import org.asamk.signal.manager.api.TypingAction;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private Manager manager;

    @Mock
    private DBusConnection connection;

    private MessageService messageService;
    private SendMessageResults sendMessageResults;

    @BeforeEach
    void setUp() {
        messageService = new MessageServiceImpl(manager, connection, "objectPath");
        sendMessageResults = new SendMessageResults(0, Map.of());
    }

    @Test
    void sendMessage() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendMessage(any(Message.class), anySet())).willReturn(sendMessageResults);

        final long l = messageService.sendMessage("", List.of(), "0");

        then(manager).should().getSelfNumber();
        then(manager).should().sendMessage(any(Message.class), anySet());

        assertThat(l).isZero();
    }

    @Test
    void testSendMessage() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendMessage(any(Message.class), anySet())).willReturn(sendMessageResults);

        final long l = messageService.sendMessage("", List.of(), List.of());

        then(manager).should().getSelfNumber();
        then(manager).should().sendMessage(any(Message.class), anySet());

        assertThat(l).isZero();
    }

    @Test
    void sendTyping() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendTypingMessage(any(TypingAction.class), anySet())).willReturn(sendMessageResults);

        messageService.sendTyping("0", true);

        then(manager).should().getSelfNumber();
        then(manager).should().sendTypingMessage(any(TypingAction.class), anySet());
    }

    @Test
    void sendReadReceipt() {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendReadReceipt(any(RecipientIdentifier.Single.class), anyList())).willReturn(sendMessageResults);

        messageService.sendReadReceipt("0", List.of());

        then(manager).should().getSelfNumber();
        then(manager).should().sendReadReceipt(any(RecipientIdentifier.Single.class), anyList());
    }

    @Test
    void sendViewedReceipt() {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendViewedReceipt(any(RecipientIdentifier.Single.class),
                anyList())).willReturn(sendMessageResults);

        messageService.sendViewedReceipt("0", List.of());

        then(manager).should().getSelfNumber();
        then(manager).should().sendViewedReceipt(any(RecipientIdentifier.Single.class), anyList());
    }

    @Test
    void sendRemoteDeleteMessage() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendRemoteDeleteMessage(anyLong(), anySet())).willReturn(sendMessageResults);

        final long l = messageService.sendRemoteDeleteMessage(0, "0");

        then(manager).should().getSelfNumber();
        then(manager).should().sendRemoteDeleteMessage(anyLong(), anySet());

        assertThat(l).isZero();
    }

    @Test
    void testSendRemoteDeleteMessage() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendRemoteDeleteMessage(anyLong(), anySet())).willReturn(sendMessageResults);

        final long l = messageService.sendRemoteDeleteMessage(0, List.of());

        then(manager).should().getSelfNumber();
        then(manager).should().sendRemoteDeleteMessage(anyLong(), anySet());

        assertThat(l).isZero();
    }

    @Test
    void sendMessageReaction() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendMessageReaction(anyString(),
                anyBoolean(),
                any(RecipientIdentifier.Single.class),
                anyLong(),
                anySet(),
                anyBoolean())).willReturn(sendMessageResults);

        final long l = messageService.sendMessageReaction("", true, "0", 0, "0");

        then(manager).should(times(2)).getSelfNumber();
        then(manager).should()
                .sendMessageReaction(anyString(),
                        anyBoolean(),
                        any(RecipientIdentifier.Single.class),
                        anyLong(),
                        anySet(),
                        anyBoolean());

        assertThat(l).isZero();
    }

    @Test
    void testSendMessageReaction() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendMessageReaction(anyString(),
                anyBoolean(),
                any(RecipientIdentifier.Single.class),
                anyLong(),
                anySet(),
                anyBoolean())).willReturn(sendMessageResults);

        final long l = messageService.sendMessageReaction("", true, "0", 0, List.of());

        then(manager).should(times(2)).getSelfNumber();
        then(manager).should()
                .sendMessageReaction(anyString(),
                        anyBoolean(),
                        any(RecipientIdentifier.Single.class),
                        anyLong(),
                        anySet(),
                        anyBoolean());

        assertThat(l).isZero();
    }

    @Test
    void sendNoteToSelfMessage() throws Exception {
        given(manager.sendMessage(any(Message.class), anySet())).willReturn(sendMessageResults);

        final long l = messageService.sendNoteToSelfMessage("", List.of());

        then(manager).should().sendMessage(any(Message.class), anySet());

        assertThat(l).isZero();
    }

    @Test
    void sendEndSessionMessage() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendEndSessionMessage(anySet())).willReturn(sendMessageResults);

        messageService.sendEndSessionMessage(List.of());

        then(manager).should().getSelfNumber();
        then(manager).should().sendEndSessionMessage(anySet());
    }

    @Test
    void deleteRecipient() {
        given(manager.getSelfNumber()).willReturn("0");

        messageService.deleteRecipient("0");

        then(manager).should().getSelfNumber();
        then(manager).should().deleteRecipient(any(RecipientIdentifier.Single.class));
    }

    @Test
    void sendPaymentNotification() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendPaymentNotificationMessage(any(byte[].class),
                anyString(),
                any(RecipientIdentifier.Single.class))).willReturn(sendMessageResults);

        messageService.sendPaymentNotification(new byte[32], "", "0");

        then(manager).should().getSelfNumber();
        then(manager).should()
                .sendPaymentNotificationMessage(any(byte[].class), anyString(), any(RecipientIdentifier.Single.class));
    }

    @Test
    void subscribeReceive() {
        messageService.subscribeReceive();

        then(manager).should().addReceiveHandler(any(DbusReceiveMessageHandler.class));
    }

    @Test
    void unsubscribeReceive() {
        messageService.unsubscribeReceive();
    }

    @Test
    void sendSyncRequest() throws Exception {
        messageService.sendSyncRequest();

        then(manager).should().requestAllSyncData();
    }

    @Test
    void close() {
        messageService.close();
    }

}
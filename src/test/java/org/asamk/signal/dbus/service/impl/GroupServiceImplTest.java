package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.GroupException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.InvalidAttachmentException;
import org.asamk.signal.dbus.errors.InvalidGroupIdException;
import org.asamk.signal.dbus.service.GroupService;
import org.asamk.signal.dbus.structs.DbusStructGroup;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.AttachmentInvalidException;
import org.asamk.signal.manager.api.GroupId;
import org.asamk.signal.manager.api.GroupIdV2;
import org.asamk.signal.manager.api.GroupNotFoundException;
import org.asamk.signal.manager.api.Message;
import org.asamk.signal.manager.api.Pair;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.asamk.signal.manager.api.SendGroupMessageResults;
import org.asamk.signal.manager.api.SendMessageResults;
import org.asamk.signal.manager.api.TypingAction;
import org.asamk.signal.manager.api.UnregisteredRecipientException;
import org.asamk.signal.manager.api.UpdateGroup;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private Manager manager;

    @Mock
    private DBusConnection connection;

    private GroupService groupService;
    private SendMessageResults sendMessageResults;

    @BeforeEach
    void setUp() {
        groupService = new GroupServiceImpl(manager, connection, "objectPath");
        sendMessageResults = new SendMessageResults(0, Map.of());
    }

    @Test
    void sendGroupMessage() throws Exception {
        given(manager.sendMessage(any(Message.class), anySet())).willReturn(sendMessageResults);

        final long l = groupService.sendGroupMessage("", List.of(), new byte[32]);

        then(manager).should().sendMessage(any(Message.class), anySet());

        assertThat(l).isZero();
    }

    @Test
    void sendGroupMessageIOException() throws Exception {
        doThrow(IOException.class).when(manager).sendMessage(any(Message.class), anySet());

        assertThatThrownBy(() -> groupService.sendGroupMessage("", List.of(), new byte[32])).isInstanceOf(
                FailureException.class);

        then(manager).should().sendMessage(any(Message.class), anySet());
    }

    @Test
    void sendGroupMessageGroupNotFoundException() throws Exception {
        doThrow(GroupNotFoundException.class).when(manager).sendMessage(any(Message.class), anySet());

        assertThatThrownBy(() -> groupService.sendGroupMessage("",
                List.of(),
                new byte[32])).isInstanceOf(GroupException.class);

        then(manager).should().sendMessage(any(Message.class), anySet());
    }

    @Test
    void sendGroupMessageAttachmentInvalidException() throws Exception {
        doThrow(AttachmentInvalidException.class).when(manager).sendMessage(any(Message.class), anySet());

        assertThatThrownBy(() -> groupService.sendGroupMessage("", List.of(), new byte[32])).isInstanceOf(
                InvalidAttachmentException.class);

        then(manager).should().sendMessage(any(Message.class), anySet());
    }

    @Test
    void sendGroupTyping() throws Exception {
        given(manager.sendTypingMessage(any(TypingAction.class), anySet())).willReturn(sendMessageResults);

        groupService.sendGroupTyping(new byte[32], true);

        then(manager).should().sendTypingMessage(any(TypingAction.class), anySet());
    }

    @Test
    void sendGroupRemoteDeleteMessage() throws Exception {
        given(manager.sendRemoteDeleteMessage(anyLong(), anySet())).willReturn(sendMessageResults);

        final long l = groupService.sendGroupRemoteDeleteMessage(0, new byte[32]);

        then(manager).should().sendRemoteDeleteMessage(anyLong(), anySet());

        assertThat(l).isZero();
    }

    @Test
    void sendGroupMessageReaction() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.sendMessageReaction(anyString(),
                anyBoolean(),
                any(RecipientIdentifier.Single.class),
                anyLong(),
                anySet(),
                anyBoolean())).willReturn(sendMessageResults);

        final long l = groupService.sendGroupMessageReaction("", true, "0", 0, new byte[32]);

        then(manager).should().getSelfNumber();
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
    void setGroupBlocked() throws Exception {
        groupService.setGroupBlocked(new byte[32], true);

        then(manager).should().setGroupsBlocked(anyList(), anyBoolean());
    }

    @Test
    void getGroupIds() {
        given(manager.getGroups()).willReturn(new ArrayList<>());

        var groupIds = groupService.getGroupIds();

        then(manager).should().getGroups();

        assertThat(groupIds).isNotNull();
    }

    @Test
    void getGroup() {
        given(manager.getGroups()).willReturn(new ArrayList<>());

        assertThatThrownBy(() -> groupService.getGroup(new byte[32])).isInstanceOf(GroupException.class)
                .hasMessageContaining("DbusPropertyGroup not found");

        then(manager).should().getGroups();
    }

    @Test
    void listGroups() {
        given(manager.getGroups()).willReturn(new ArrayList<>());

        final List<DbusStructGroup> groups = groupService.listGroups();

        then(manager).should().getGroups();

        assertThat(groups).isNotNull();
    }

    @Test
    void getGroupName() {
        final String groupName = groupService.getGroupName(new byte[32]);

        then(manager).should().getGroup(any());

        assertThat(groupName).isNotNull();
    }

    @Test
    void getGroupMembers() {
        final List<String> groupMembers = groupService.getGroupMembers(new byte[32]);

        then(manager).should().getGroup(any());

        assertThat(groupMembers).isNotNull();
    }

    @Test
    void createGroup() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        given(manager.createGroup(any(), anySet(), any())).willReturn(new Pair<>(new GroupIdV2(new byte[32]),
                new SendGroupMessageResults(0, List.of())));

        final byte[] groupId = groupService.updateGroup(new byte[0], "", List.of(), "");

        then(manager).should().getSelfNumber();
        then(manager).should().createGroup(any(), anySet(), any());
        then(manager).should().getGroups();
        assertThat(groupId).isNotNull();

    }

    @Test
    void updateGroup() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");

        final byte[] groupId = groupService.updateGroup(new byte[32], "", List.of(), "");

        then(manager).should().updateGroup(any(GroupId.class), any(UpdateGroup.class));
        assertThat(groupId).isNotNull();
    }

    @Test
    void quitGroup() throws Exception {
        groupService.quitGroup(new byte[32]);
        then(manager).should().quitGroup(any(), anySet());
    }

    @Test
    void quitGroupInvalidGroupIdException() {
        assertThatThrownBy(() -> groupService.quitGroup(new byte[0])).isInstanceOf(InvalidGroupIdException.class)
                .hasMessageContaining("Invalid group id");
        then(manager).shouldHaveNoInteractions();
    }

    @Test
    void isGroupBlocked() {
        assertThat(groupService.isGroupBlocked(new byte[32])).isFalse();
        then(manager).should().getGroup(any());
    }

    @Test
    void isGroupBlockedInvalidGroupIdException() {
        assertThatThrownBy(() -> groupService.isGroupBlocked(new byte[0])).isInstanceOf(InvalidGroupIdException.class)
                .hasMessageContaining("Invalid group id");
        then(manager).shouldHaveNoInteractions();
    }

    @Test
    void isMember() {
        assertThat(groupService.isMember(new byte[32])).isFalse();
        then(manager).should().getGroup(any());
    }

    @Test
    void isMemberInvalidGroupIdException() {
        assertThatThrownBy(() -> groupService.isMember(new byte[0])).isInstanceOf(InvalidGroupIdException.class)
                .hasMessageContaining("Invalid group id");
        then(manager).shouldHaveNoInteractions();
    }

    @Test
    void joinGroup() {
        assertThatThrownBy(() -> groupService.joinGroup("0")).isInstanceOf(FailureException.class)
                .hasMessageContaining("DbusPropertyGroup link is invalid");
    }

    @Test
    void updateGroups() {
        given(manager.getGroups()).willReturn(new ArrayList<>());

        groupService.updateGroups();

        then(manager).should().getGroups();
    }

}
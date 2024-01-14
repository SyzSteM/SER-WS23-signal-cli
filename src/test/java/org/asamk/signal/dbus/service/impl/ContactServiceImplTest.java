package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.service.ContactService;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private Manager manager;

    private ContactService contactService;

    @BeforeEach
    void setUp() {
        contactService = new ContactServiceImpl(manager);
    }

    @Test
    void sendContacts() throws IOException {
        contactService.sendContacts();

        then(manager).should().sendContacts();
    }

    @Test
    void sendContactsIOException() throws IOException {
        doThrow(IOException.class).when(manager).sendContacts();

        assertThatThrownBy(() -> contactService.sendContacts()).isInstanceOf(FailureException.class)
                .hasMessageContaining("SendContacts error");

        then(manager).should().sendContacts();
    }

    @Test
    void deleteContact() {
        given(manager.getSelfNumber()).willReturn("0");

        contactService.deleteContact("0");

        then(manager).should().deleteContact(any(RecipientIdentifier.Single.class));
    }

    @Test
    void getContactName() {
        given(manager.getContactOrProfileName(any(RecipientIdentifier.Single.class))).willReturn("0");
        given(manager.getSelfNumber()).willReturn("0");

        final String contactName = contactService.getContactName("0");

        then(manager).should().getContactOrProfileName(any(RecipientIdentifier.Single.class));
        assertThat(contactName).isEqualTo("0");
    }

    @Test
    void setContactName() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");

        contactService.setContactName("0", "0");

        then(manager).should().setContactName(any(RecipientIdentifier.Single.class), anyString(), anyString());
    }

    @Test
    void setContactBlocked() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");

        contactService.setContactBlocked("0", true);

        then(manager).should().setContactsBlocked(anyList(), anyBoolean());
    }

    @Test
    void getContactNumber() {
        given(manager.getRecipients(anyBoolean(), any(), anyCollection(), any())).willReturn(new ArrayList<>());

        contactService.getContactNumber("0");

        then(manager).should().getRecipients(anyBoolean(), any(), anyCollection(), any());
    }

    @Test
    void isContactBlocked() {
        given(manager.isContactBlocked(any(RecipientIdentifier.Single.class))).willReturn(true);
        given(manager.getSelfNumber()).willReturn("0");

        final boolean contactBlocked = contactService.isContactBlocked("0");

        then(manager).should().isContactBlocked(any(RecipientIdentifier.Single.class));
        assertThat(contactBlocked).isTrue();
    }

}
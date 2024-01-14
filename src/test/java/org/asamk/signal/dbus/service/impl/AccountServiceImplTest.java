package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.service.AccountService;
import org.asamk.signal.dbus.structs.DbusStructIdentity;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.NotPrimaryDeviceException;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.asamk.signal.manager.api.UpdateProfile;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private Manager manager;
    @Mock
    private DBusConnection connection;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(manager, connection, "objectPath");
    }

    @Test
    void getIdentity() {
        assertThatThrownBy(() -> accountService.getIdentity("0")).isInstanceOf(FailureException.class)
                .hasMessageContaining("DbusPropertyIdentity for 0 unknown");
    }

    @Test
    void getSelfNumber() {
        given(manager.getSelfNumber()).willReturn("0");

        final String selfNumber = accountService.getSelfNumber();

        then(manager).should().getSelfNumber();
        assertThat(selfNumber).isNotBlank();
    }

    @Test
    void listIdentities() {
        given(manager.getIdentities()).willReturn(new ArrayList<>());

        final List<DbusStructIdentity> dbusStructIdentities = accountService.listIdentities();

        then(manager).should().getIdentities();
        assertThat(dbusStructIdentities).isNotNull();

    }

    @Test
    void listNumbers() {
        given(manager.getRecipients(false, Optional.empty(), Set.of(), Optional.empty())).willReturn(new ArrayList<>());

        final List<String> numbers = accountService.listNumbers();

        then(manager).should().getRecipients(false, Optional.empty(), Set.of(), Optional.empty());
        assertThat(numbers).isNotNull();
    }

    @Test
    void updateProfile() throws IOException {
        accountService.updateProfile("", "", "", "", "", true);

        then(manager).should().updateProfile(any(UpdateProfile.class));
    }

    @Test
    void updateProfileIOException() throws IOException {
        doThrow(IOException.class).when(manager).updateProfile(any(UpdateProfile.class));

        assertThatThrownBy(() -> accountService.updateProfile("",
                "",
                "",
                "",
                "",
                true)).isInstanceOf(FailureException.class);

        then(manager).should().updateProfile(any(UpdateProfile.class));
    }

    @Test
    void testUpdateProfile() throws IOException {
        accountService.updateProfile("", "", "", "", "", true);

        then(manager).should().updateProfile(any(UpdateProfile.class));
    }

    @Test
    void setExpirationTimer() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");

        accountService.setExpirationTimer("0", 0);

        then(manager).should().setExpirationTimer(any(RecipientIdentifier.Single.class), anyInt());
    }

    @Test
    void setExpirationTimerIOException() throws Exception {
        given(manager.getSelfNumber()).willReturn("0");
        doThrow(IOException.class).when(manager).setExpirationTimer(any(RecipientIdentifier.Single.class), anyInt());

        assertThatThrownBy(() -> accountService.setExpirationTimer("0", 0)).isInstanceOf(FailureException.class);

        then(manager).should().setExpirationTimer(any(RecipientIdentifier.Single.class), anyInt());
    }

    @Test
    void setPin() throws Exception {
        accountService.setPin("");

        then(manager).should().setRegistrationLockPin(Optional.of(""));
    }

    @Test
    void setPinIOException() throws Exception {
        doThrow(IOException.class).when(manager).setRegistrationLockPin(Optional.of(""));

        assertThatThrownBy(() -> accountService.setPin("")).isInstanceOf(FailureException.class)
                .hasMessageContaining("Set pin error");

        then(manager).should().setRegistrationLockPin(Optional.of(""));
    }

    @Test
    void setPinNotPrimaryDeviceException() throws Exception {
        doThrow(NotPrimaryDeviceException.class).when(manager).setRegistrationLockPin(Optional.of(""));

        assertThatThrownBy(() -> accountService.setPin("")).isInstanceOf(FailureException.class)
                .hasMessageContaining("This command doesn't work on linked devices.");

        then(manager).should().setRegistrationLockPin(Optional.of(""));
    }

    @Test
    void submitRateLimitChallenge() throws IOException {
        accountService.submitRateLimitChallenge("", "");

        then(manager).should().submitRateLimitRecaptchaChallenge(anyString(), anyString());
    }

    @Test
    void submitRateLimitChallengeException() throws IOException {
        doThrow(IOException.class).when(manager).submitRateLimitRecaptchaChallenge(anyString(), anyString());

        assertThatThrownBy(() -> accountService.submitRateLimitChallenge("", "")).isInstanceOf(FailureException.class)
                .hasMessageContaining("Submit challenge error");

        then(manager).should().submitRateLimitRecaptchaChallenge(anyString(), anyString());
    }

    @Test
    void removePin() throws Exception {
        accountService.removePin();

        then(manager).should().setRegistrationLockPin(Optional.empty());
    }

    @Test
    void removePinIOException() throws Exception {
        doThrow(IOException.class).when(manager).setRegistrationLockPin(Optional.empty());

        assertThatThrownBy(() -> accountService.removePin()).isInstanceOf(FailureException.class)
                .hasMessageContaining("Remove pin error");

        then(manager).should().setRegistrationLockPin(Optional.empty());
    }

    @Test
    void removePinNotPrimaryDeviceException() throws Exception {
        doThrow(NotPrimaryDeviceException.class).when(manager).setRegistrationLockPin(Optional.empty());

        assertThatThrownBy(() -> accountService.removePin()).isInstanceOf(FailureException.class)
                .hasMessageContaining("This command doesn't work on linked devices.");

        then(manager).should().setRegistrationLockPin(Optional.empty());
    }

    @Test
    void deleteAccount() throws IOException {
        accountService.deleteAccount();

        then(manager).should().deleteAccount();
    }

    @Test
    void deleteAccountException() throws IOException {
        doThrow(IOException.class).when(manager).deleteAccount();

        assertThatThrownBy(() -> accountService.deleteAccount()).isInstanceOf(FailureException.class)
                .hasMessageContaining("Failed to delete account");

        then(manager).should().deleteAccount();
    }

    @Test
    void updateIdentities() {
        given(manager.getIdentities()).willReturn(new ArrayList<>());

        accountService.updateIdentities();

        then(manager).should().getIdentities();
    }

    @Test
    void unExportIdentities() {
        accountService.unExportIdentities();
    }
}
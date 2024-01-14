package org.asamk.signal.dbus.service;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.structs.DbusStructIdentity;
import org.freedesktop.dbus.DBusPath;

import java.util.List;

public interface AccountService {

    DBusPath getIdentity(String number);

    String getSelfNumber();

    List<DbusStructIdentity> listIdentities();

    List<String> listNumbers();

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

    void setExpirationTimer(String number, int expiration) throws FailureException;

    void setPin(String registrationLockPin);

    void submitRateLimitChallenge(String challenge, String captchaString) throws FailureException;

    String uploadStickerPack(String stickerPackPath) throws FailureException;

    void removePin();

    void deleteAccount() throws FailureException;

    void updateIdentities();

    void unExportIdentities();
}

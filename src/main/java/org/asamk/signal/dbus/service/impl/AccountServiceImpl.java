package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.properties.impl.DbusPropertyIdentityImpl;
import org.asamk.signal.dbus.service.AccountService;
import org.asamk.signal.dbus.structs.DbusStructIdentity;
import org.asamk.signal.dbus.util.RecipientUtils;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.Identity;
import org.asamk.signal.manager.api.NotPrimaryDeviceException;
import org.asamk.signal.manager.api.StickerPackInvalidException;
import org.asamk.signal.manager.api.UnregisteredRecipientException;
import org.asamk.signal.manager.api.UpdateProfile;
import org.asamk.signal.util.Util;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final Manager manager;
    private final DBusConnection connection;
    private final String objectPath;

    private final List<DbusStructIdentity> identities = new ArrayList<>();

    public AccountServiceImpl(Manager manager, DBusConnection connection, String objectPath) {
        this.manager = manager;
        this.connection = connection;
        this.objectPath = objectPath;
    }

    @Override
    public DBusPath getIdentity(String number) {
        var found = identities.stream()
                .filter(identity -> identity.getNumber().equals(number) || identity.getUuid().equals(number))
                .findFirst();

        if (found.isEmpty()) {
            throw new FailureException("DbusPropertyIdentity for " + number + " unknown");
        }
        return found.get().getObjectPath();
    }

    @Override
    public String getSelfNumber() {
        return manager.getSelfNumber();
    }

    @Override
    public List<DbusStructIdentity> listIdentities() {
        updateIdentities();

        return Collections.unmodifiableList(identities);
    }

    /**
     * Create a unique list of Numbers from Identities and Contacts to really get
     * all numbers the system knows
     *
     * @return list of all numbers the system knows
     */
    @Override
    public List<String> listNumbers() {
        return manager.getRecipients(false, Optional.empty(), Set.of(), Optional.empty())
                .stream()
                .map(r -> r.getAddress().number().orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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
        try {
            givenName = Util.nullIfEmpty(givenName);
            familyName = Util.nullIfEmpty(familyName);
            about = Util.nullIfEmpty(about);
            aboutEmoji = Util.nullIfEmpty(aboutEmoji);
            avatarPath = Util.nullIfEmpty(avatarPath);
            var avatarFile = removeAvatar || avatarPath == null ? null : avatarPath;
            manager.updateProfile(UpdateProfile.newBuilder()
                    .withGivenName(givenName)
                    .withFamilyName(familyName)
                    .withAbout(about)
                    .withAboutEmoji(aboutEmoji)
                    .withAvatar(avatarFile)
                    .withDeleteAvatar(removeAvatar)
                    .build());
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        }
    }

    @Override
    public void updateProfile(
            String name, String about, String aboutEmoji, String avatarPath, boolean removeAvatar
    ) throws FailureException {
        updateProfile(name, "", about, aboutEmoji, avatarPath, removeAvatar);
    }

    @Override
    public void setExpirationTimer(String number, int expiration) throws FailureException {
        try {
            manager.setExpirationTimer(RecipientUtils.getSingleRecipientIdentifier(number, manager.getSelfNumber()),
                    expiration);
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Override
    public void setPin(String registrationLockPin) {
        try {
            manager.setRegistrationLockPin(Optional.of(registrationLockPin));
        } catch (IOException e) {
            throw new FailureException("Set pin error: " + e.getMessage());
        } catch (NotPrimaryDeviceException e) {
            throw new FailureException("This command doesn't work on linked devices.");
        }
    }

    @Override
    public void submitRateLimitChallenge(String challenge, String captchaString) throws FailureException {
        try {
            manager.submitRateLimitRecaptchaChallenge(challenge, captchaString);
        } catch (IOException e) {
            throw new FailureException("Submit challenge error: " + e.getMessage());
        }
    }

    @Override
    public String uploadStickerPack(String stickerPackPath) throws FailureException {
        File path = new File(stickerPackPath);
        try {
            return manager.uploadStickerPack(path).toString();
        } catch (IOException e) {
            throw new FailureException("Upload error (maybe image size is too large):" + e.getMessage());
        } catch (StickerPackInvalidException e) {
            throw new FailureException("Invalid sticker pack: " + e.getMessage());
        }
    }

    @Override
    public void removePin() {
        try {
            manager.setRegistrationLockPin(Optional.empty());
        } catch (IOException e) {
            throw new FailureException("Remove pin error: " + e.getMessage());
        } catch (NotPrimaryDeviceException e) {
            throw new FailureException("This command doesn't work on linked devices.");
        }
    }

    @Override
    public void deleteAccount() throws FailureException {
        try {
            manager.deleteAccount();
        } catch (IOException e) {
            throw new FailureException("Failed to delete account: " + e.getMessage());
        }
    }

    @Override
    public void updateIdentities() {
        List<Identity> identities;
        identities = manager.getIdentities();

        unExportIdentities();

        identities.forEach(i -> {
            var object = new DbusPropertyIdentityImpl(manager, objectPath, i);
            exportObject(object);
            this.identities.add(new DbusStructIdentity(new DBusPath(object.getObjectPath()),
                    i.recipient().uuid().map(UUID::toString).orElse(""),
                    i.recipient().number().orElse("")));
        });
    }

    @Override
    public void unExportIdentities() {
        identities.stream()
                .map(DbusStructIdentity::getObjectPath)
                .map(DBusPath::getPath)
                .forEach(connection::unExportObject);
        identities.clear();
    }

    private void exportObject(DBusInterface object) {
        try {
            connection.exportObject(object);
            logger.debug("Exported dbus object: {}", object.getObjectPath());
        } catch (DBusException e) {
            logger.warn("Failed to export dbus object ({}): {}", object.getObjectPath(), e.getMessage());
        }
    }

}

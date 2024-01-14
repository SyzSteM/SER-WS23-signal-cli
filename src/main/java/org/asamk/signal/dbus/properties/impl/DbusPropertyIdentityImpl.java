package org.asamk.signal.dbus.properties.impl;

import org.asamk.signal.dbus.DbusInterfacePropertiesHandler;
import org.asamk.signal.dbus.DbusProperties;
import org.asamk.signal.dbus.DbusProperty;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.properties.DbusPropertyIdentity;
import org.asamk.signal.dbus.util.DbusUtils;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.Identity;
import org.asamk.signal.manager.api.IdentityVerificationCode;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.asamk.signal.manager.api.UnregisteredRecipientException;

import java.util.List;
import java.util.UUID;

public final class DbusPropertyIdentityImpl extends DbusProperties implements DbusPropertyIdentity {

    private final Manager manager;
    private final String objectPath;
    private final Identity identity;

    public DbusPropertyIdentityImpl(Manager manager, String objectPath, Identity identity) {
        this.manager = manager;
        this.objectPath = objectPath;
        this.identity = identity;

        addPropertiesHandler(new DbusInterfacePropertiesHandler("org.asamk.signal.dbus.properties.DbusPropertyIdentity",
                List.of(new DbusProperty<>("Number", () -> identity.recipient().number().orElse("")),
                        new DbusProperty<>("Uuid", () -> identity.recipient().uuid().map(UUID::toString).orElse("")),
                        new DbusProperty<>("Fingerprint", identity::getFingerprint),
                        new DbusProperty<>("SafetyNumber", identity::safetyNumber),
                        new DbusProperty<>("ScannableSafetyNumber", identity::scannableSafetyNumber),
                        new DbusProperty<>("TrustLevel", identity::trustLevel),
                        new DbusProperty<>("AddedDate", identity::dateAddedTimestamp))));
    }

    private static String getIdentityObjectPath(String basePath, String id) {
        return basePath + "/Identities/" + DbusUtils.makeValidObjectPathElement(id);
    }

    @Override
    public String getObjectPath() {
        return getIdentityObjectPath(objectPath,
                identity.recipient().getLegacyIdentifier() + "_" + identity.recipient().getIdentifier());
    }

    @Override
    public void trust() throws FailureException {
        var recipient = RecipientIdentifier.Single.fromAddress(identity.recipient());

        try {
            manager.trustIdentityAllKeys(recipient);
        } catch (UnregisteredRecipientException e) {
            throw new FailureException("The user " + e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Override
    public void trustVerified(String safetyNumber) throws FailureException {
        var recipient = RecipientIdentifier.Single.fromAddress(identity.recipient());

        if (safetyNumber == null) {
            throw new FailureException("You need to specify a fingerprint/safety number");
        }
        IdentityVerificationCode verificationCode;
        try {
            verificationCode = IdentityVerificationCode.parse(safetyNumber);
        } catch (Exception e) {
            throw new FailureException(
                    "Safety number has invalid format, either specify the old hex fingerprint or the new safety number");
        }

        try {
            var res = manager.trustIdentityVerified(recipient, verificationCode);
            if (!res) {
                throw new FailureException(
                        "Failed to set the trust for this number, make sure the number and the fingerprint/safety number are correct.");
            }
        } catch (UnregisteredRecipientException e) {
            throw new FailureException("The user " + e.getSender().getIdentifier() + " is not registered.");
        }
    }
}

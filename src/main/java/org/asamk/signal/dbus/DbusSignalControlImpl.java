package org.asamk.signal.dbus;

import org.asamk.SignalControl;
import org.asamk.signal.BaseConfig;
import org.asamk.signal.DbusConfig;
import org.asamk.signal.dbus.errors.ControlFailureException;
import org.asamk.signal.dbus.errors.ControlNumberInvalidException;
import org.asamk.signal.dbus.errors.ControlRequiresCaptchaException;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.MultiAccountManager;
import org.asamk.signal.manager.ProvisioningManager;
import org.asamk.signal.manager.RegistrationManager;
import org.asamk.signal.manager.api.CaptchaRequiredException;
import org.asamk.signal.manager.api.IncorrectPinException;
import org.asamk.signal.manager.api.NonNormalizedPhoneNumberException;
import org.asamk.signal.manager.api.PinLockedException;
import org.asamk.signal.manager.api.RateLimitException;
import org.asamk.signal.manager.api.UserAlreadyExistsException;
import org.freedesktop.dbus.DBusPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.OverlappingFileLockException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class DbusSignalControlImpl implements org.asamk.SignalControl {

    private static final Logger logger = LoggerFactory.getLogger(DbusSignalControlImpl.class);
    private final MultiAccountManager c;

    private final String objectPath;

    public DbusSignalControlImpl(final MultiAccountManager c, final String objectPath) {
        this.c = c;
        this.objectPath = objectPath;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String getObjectPath() {
        return objectPath;
    }

    @Override
    public void register(
            final String number, final boolean voiceVerification
    ) throws FailureException, NumberInvalidException {
        registerWithCaptcha(number, voiceVerification, null);
    }

    @Override
    public void registerWithCaptcha(
            final String number, final boolean voiceVerification, final String captcha
    ) throws FailureException, NumberInvalidException {
        if (!Manager.isValidNumber(number, null)) {
            throw new ControlNumberInvalidException(
                    "Invalid account (phone number), make sure you include the country code.");
        }
        try (final RegistrationManager registrationManager = c.getNewRegistrationManager(number)) {
            registrationManager.register(voiceVerification, captcha);
        } catch (RateLimitException e) {
            String message = "Rate limit reached";
            throw new ControlFailureException(message);
        } catch (CaptchaRequiredException e) {
            String message = captcha == null ? "Captcha required for verification." : "Invalid captcha given.";
            throw new ControlRequiresCaptchaException(message);
        } catch (NonNormalizedPhoneNumberException e) {
            throw new NumberInvalidException(e.getMessage());
        } catch (OverlappingFileLockException e) {
            throw new ControlFailureException("Account is already in use");
        } catch (IOException e) {
            throw new ControlFailureException(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    @Override
    public void verify(final String number, final String verificationCode) throws FailureException, NumberInvalidException {
        verifyWithPin(number, verificationCode, null);
    }

    @Override
    public void verifyWithPin(
            final String number, final String verificationCode, final String pin
    ) throws FailureException, NumberInvalidException {
        try (final RegistrationManager registrationManager = c.getNewRegistrationManager(number)) {
            registrationManager.verifyAccount(verificationCode, pin);
        } catch (OverlappingFileLockException e) {
            throw new ControlFailureException("Account is already in use");
        } catch (IOException e) {
            throw new ControlFailureException(e.getClass().getSimpleName() + " " + e.getMessage());
        } catch (PinLockedException e) {
            throw new FailureException(
                    "Verification failed! This number is locked with a pin. Hours remaining until reset: "
                            + (e.getTimeRemaining() / 1000 / 60 / 60));
        } catch (IncorrectPinException e) {
            throw new FailureException("Verification failed! Invalid pin, tries remaining: " + e.getTriesRemaining());
        }
    }

    @Override
    public String link(final String newDeviceName) throws FailureException {
        final URI deviceLinkUri;
        try {
            deviceLinkUri = c.getNewProvisioningDeviceLinkUri();
        } catch (TimeoutException | IOException e) {
            throw new ControlFailureException(e.getClass().getSimpleName() + " " + e.getMessage());
        }
        Thread.ofPlatform().name("dbus-link").start(() -> {
            final ProvisioningManager provisioningManager = c.getProvisioningManagerFor(deviceLinkUri);
            try {
                provisioningManager.finishDeviceLink(newDeviceName);
            } catch (IOException | TimeoutException | UserAlreadyExistsException e) {
                logger.warn("Failed to finish linking", e);
            }
        });
        return deviceLinkUri.toString();
    }

    @Override
    public String startLink() throws FailureException {
        try {
            final URI deviceLinkUri = c.getNewProvisioningDeviceLinkUri();
            return deviceLinkUri.toString();
        } catch (TimeoutException | IOException e) {
            throw new ControlFailureException(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    @Override
    public String finishLink(String deviceLinkUri, final String newDeviceName) throws FailureException {
        try {
            final var provisioningManager = c.getProvisioningManagerFor(new URI(deviceLinkUri));
            return provisioningManager.finishDeviceLink(newDeviceName);
        } catch (TimeoutException | IOException | UserAlreadyExistsException | URISyntaxException e) {
            throw new ControlFailureException(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    @Override
    public String version() {
        return BaseConfig.PROJECT_VERSION;
    }

    @Override
    public List<DBusPath> listAccounts() {
        return c.getAccountNumbers().stream().map(u -> new DBusPath(DbusConfig.getObjectPath(u))).toList();
    }

    @Override
    public DBusPath getAccount(final String number) {
        return new DBusPath(DbusConfig.getObjectPath(number));
    }
}

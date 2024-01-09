package org.asamk;

import org.asamk.signal.dbus.errors.ControlRequiresCaptchaException;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.DBusInterface;

import java.util.List;

/**
 * DBus interface for the org.asamk.SignalControl interface.
 * Including emitted Signals and returned Errors.
 */
public interface SignalControl extends DBusInterface {

    void register(
            String number, boolean voiceVerification
    ) throws FailureException, NumberInvalidException, ControlRequiresCaptchaException;

    void registerWithCaptcha(
            String number, boolean voiceVerification, String captcha
    ) throws FailureException, NumberInvalidException, ControlRequiresCaptchaException;

    void verify(String number, String verificationCode) throws FailureException, NumberInvalidException;

    void verifyWithPin(String number, String verificationCode, String pin) throws FailureException, NumberInvalidException;

    String link(String newDeviceName) throws FailureException;

    String startLink() throws FailureException;

    String finishLink(String deviceLinkUri, String newDeviceName) throws FailureException;

    String version();

    List<DBusPath> listAccounts();

    DBusPath getAccount(String number);

}

package org.asamk.signal.dbus.service.impl;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.dbus.errors.NumberInvalidException;
import org.asamk.signal.dbus.service.ContactService;
import org.asamk.signal.dbus.util.RecipientUtils;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.NotPrimaryDeviceException;
import org.asamk.signal.manager.api.UnregisteredRecipientException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ContactServiceImpl implements ContactService {

    private final Manager manager;

    public ContactServiceImpl(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void sendContacts() throws FailureException {
        try {
            manager.sendContacts();
        } catch (IOException e) {
            throw new FailureException("SendContacts error: " + e.getMessage());
        }
    }

    @Override
    public void deleteContact(String recipient) throws FailureException {
        manager.deleteContact(RecipientUtils.getSingleRecipientIdentifier(recipient, manager.getSelfNumber()));
    }

    /**
     * Since contact names might be empty if not defined, also potentially return the profile name
     *
     * @param number
     * @return
     * @throws NumberInvalidException
     */
    @Override
    public String getContactName(String number) throws NumberInvalidException {
        var name = manager.getContactOrProfileName(RecipientUtils.getSingleRecipientIdentifier(number,
                manager.getSelfNumber()));
        return name == null ? "" : name;
    }

    @Override
    public void setContactName(String number, String name) throws NumberInvalidException {
        try {
            manager.setContactName(RecipientUtils.getSingleRecipientIdentifier(number, manager.getSelfNumber()),
                    name,
                    "");
        } catch (NotPrimaryDeviceException e) {
            throw new FailureException("This command doesn't work on linked devices.");
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Override
    public void setContactBlocked(String number, boolean blocked) throws NumberInvalidException {
        try {
            manager.setContactsBlocked(List.of(RecipientUtils.getSingleRecipientIdentifier(number,
                    manager.getSelfNumber())), blocked);
        } catch (NotPrimaryDeviceException e) {
            throw new FailureException("This command doesn't work on linked devices.");
        } catch (IOException e) {
            throw new FailureException(e.getMessage());
        } catch (UnregisteredRecipientException e) {
            throw new IdentityUntrustedException(e.getSender().getIdentifier() + " is not registered.");
        }
    }

    @Override
    public List<String> getContactNumber(String name) throws FailureException {
        return manager.getRecipients(false, Optional.empty(), Set.of(), Optional.of(name))
                .stream()
                .map(r -> r.getAddress().getLegacyIdentifier())
                .toList();
    }

    @Override
    public boolean isContactBlocked(String number) throws NumberInvalidException {
        return manager.isContactBlocked(RecipientUtils.getSingleRecipientIdentifier(number, manager.getSelfNumber()));
    }

}

package org.asamk.signal.dbus.service;

import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.NumberInvalidException;

import java.util.List;

public interface ContactService {

    void sendContacts() throws FailureException;

    void deleteContact(String recipient) throws FailureException;

    String getContactName(String number) throws NumberInvalidException;

    void setContactName(String number, String name) throws NumberInvalidException;

    void setContactBlocked(String number, boolean blocked) throws NumberInvalidException;

    List<String> getContactNumber(String name) throws FailureException;

    boolean isContactBlocked(String number) throws NumberInvalidException;

}

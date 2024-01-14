package org.asamk.signal.dbus.properties.impl;

import org.asamk.signal.dbus.DbusInterfacePropertiesHandler;
import org.asamk.signal.dbus.DbusProperties;
import org.asamk.signal.dbus.DbusProperty;
import org.asamk.signal.dbus.DbusSignalImpl;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.properties.DbusPropertyConfiguration;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.NotPrimaryDeviceException;

import java.util.List;
import java.util.Optional;

public final class DbusPropertyConfigurationImpl extends DbusProperties implements DbusPropertyConfiguration {

    private final Manager manager;
    private final String objectPath;

    public DbusPropertyConfigurationImpl(Manager manager, String objectPath) {
        this.manager = manager;
        this.objectPath = objectPath;

        addPropertiesHandler(new DbusInterfacePropertiesHandler(
                "org.asamk.signal.dbus.properties.DbusPropertyConfiguration",
                List.of(new DbusProperty<>("ReadReceipts", this::getReadReceipts, this::setReadReceipts),
                        new DbusProperty<>("UnidentifiedDeliveryIndicators",
                                this::getUnidentifiedDeliveryIndicators,
                                this::setUnidentifiedDeliveryIndicators),
                        new DbusProperty<>("TypingIndicators", this::getTypingIndicators, this::setTypingIndicators),
                        new DbusProperty<>("LinkPreviews", this::getLinkPreviews, this::setLinkPreviews))));
    }

    private static String getConfigurationObjectPath(String basePath) {
        return basePath + "/DbusPropertyConfiguration";
    }

    @Override
    public String getObjectPath() {
        return getConfigurationObjectPath(objectPath);
    }

    private void setConfiguration(
            Boolean readReceipts, Boolean unidentifiedDeliveryIndicators, Boolean typingIndicators, Boolean linkPreviews
    ) {
        try {
            manager.updateConfiguration(new org.asamk.signal.manager.api.Configuration(Optional.ofNullable(readReceipts),
                    Optional.ofNullable(unidentifiedDeliveryIndicators),
                    Optional.ofNullable(typingIndicators),
                    Optional.ofNullable(linkPreviews)));
        } catch (NotPrimaryDeviceException e) {
            throw new FailureException("This command doesn't work on linked devices.");
        }
    }

    private boolean getReadReceipts() {
        return manager.getConfiguration().readReceipts().orElse(false);
    }

    public void setReadReceipts(Boolean readReceipts) {
        setConfiguration(readReceipts, null, null, null);
    }

    private boolean getUnidentifiedDeliveryIndicators() {
        return manager.getConfiguration().unidentifiedDeliveryIndicators().orElse(false);
    }

    public void setUnidentifiedDeliveryIndicators(Boolean unidentifiedDeliveryIndicators) {
        setConfiguration(null, unidentifiedDeliveryIndicators, null, null);
    }

    private boolean getTypingIndicators() {
        return manager.getConfiguration().typingIndicators().orElse(false);
    }

    public void setTypingIndicators(Boolean typingIndicators) {
        setConfiguration(null, null, typingIndicators, null);
    }

    private boolean getLinkPreviews() {
        return manager.getConfiguration().linkPreviews().orElse(false);
    }

    public void setLinkPreviews(Boolean linkPreviews) {
        setConfiguration(null, null, null, linkPreviews);
    }

}

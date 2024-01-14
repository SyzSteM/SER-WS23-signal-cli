package org.asamk.signal.dbus.util;

public final class DbusUtils {

    private DbusUtils() {

    }

    public static String makeValidObjectPathElement(String pathElement) {
        return pathElement.replaceAll("[^A-Za-z0-9_]", "_");
    }
}

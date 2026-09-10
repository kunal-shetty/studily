package com.studily.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central logging helper so SQL/API failures show up in Tomcat's logs
 * (catalina.out / localhost.log) instead of disappearing silently.
 */
public final class Log {

    private static final Logger LOG = Logger.getLogger("studily");

    private Log() {
    }

    public static void severe(String message, Throwable t) {
        LOG.log(Level.SEVERE, message, t);
    }

    public static void warning(String message) {
        LOG.warning(message);
    }

    public static void info(String message) {
        LOG.info(message);
    }
}

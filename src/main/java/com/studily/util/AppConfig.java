package com.studily.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration loader. Reads application.properties from the
 * classpath once and caches the values for the lifetime of the app.
 */
public final class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("application.properties not found on classpath");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load application.properties: " + e.getMessage());
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        String value = PROPS.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config key: " + key);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        String value = PROPS.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

package com.studily.util;

import java.util.regex.Pattern;

/**
 * Server-side input validation. Never trust client-side validation.
 */
public final class Validators {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern SAFE_TITLE =
            Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N} \\-_.:()']{1,199}$");

    private Validators() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches() && email.length() <= 150;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.trim().length() >= 2 && name.trim().length() <= 100;
    }

    /** Minimum 8 chars, at least one letter and one digit. */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 100) {
            return false;
        }
        return password.chars().anyMatch(Character::isLetter)
                && password.chars().anyMatch(Character::isDigit);
    }

    public static boolean isValidTitle(String title) {
        return title != null && SAFE_TITLE.matcher(title.trim()).matches();
    }

    public static Integer parseIntOrNull(String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /** Strip control chars and collapse excessive whitespace from pasted text. */
    public static String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", " ")
                   .replaceAll("[ \\t]{3,}", "  ")
                   .trim();
    }
}

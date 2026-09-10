package com.studily.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * One-shot flash messages stored in the session, rendered and cleared by
 * FlashFilter on every request.
 */
public final class Flash {

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String INFO = "info";

    private static final String SESSION_KEY = "FLASH_MESSAGES";

    private Flash() {
    }

    @SuppressWarnings("unchecked")
    public static void add(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        java.util.List<String[]> messages =
                (java.util.List<String[]>) session.getAttribute(SESSION_KEY);
        if (messages == null) {
            messages = new java.util.ArrayList<>();
            session.setAttribute(SESSION_KEY, messages);
        }
        messages.add(new String[] { type, message });
    }

    public static void success(HttpServletRequest request, String message) {
        add(request, SUCCESS, message);
    }

    public static void error(HttpServletRequest request, String message) {
        add(request, ERROR, message);
    }

    public static void info(HttpServletRequest request, String message) {
        add(request, INFO, message);
    }

    @SuppressWarnings("unchecked")
    public static java.util.List<String[]> drain(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return java.util.List.of();
        }
        java.util.List<String[]> messages =
                (java.util.List<String[]>) session.getAttribute(SESSION_KEY);
        if (messages != null) {
            session.removeAttribute(SESSION_KEY);
        }
        return messages == null ? java.util.List.of() : messages;
    }
}

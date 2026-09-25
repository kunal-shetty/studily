package com.studily.model;

/**
 * Per-user preferences (theme, accent, AI model, notifications).
 */
public class UserSettings {

    public static final String[] ACCENTS = { "gold", "blue", "violet", "emerald", "rose" };
    public static final String[] AI_MODELS = {
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b"
    };

    private int userId;
    private String theme = "dark";
    private String accent = "gold";
    private String aiModel = "llama-3.3-70b-versatile";
    private boolean notifications = true;

    public static String safeAccent(String value) {
        for (String a : ACCENTS) if (a.equals(value)) return a;
        return "gold";
    }

    public static String safeModel(String value) {
        for (String m : AI_MODELS) if (m.equals(value)) return m;
        return "llama-3.3-70b-versatile";
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = "light".equals(theme) ? "light" : "dark"; }

    public String getAccent() { return accent; }
    public void setAccent(String accent) { this.accent = safeAccent(accent); }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = UserSettings.safeModel(aiModel); }

    public boolean isNotifications() { return notifications; }
    public void setNotifications(boolean notifications) { this.notifications = notifications; }
}

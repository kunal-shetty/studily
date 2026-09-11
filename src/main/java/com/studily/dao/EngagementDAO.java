package com.studily.dao;

import com.studily.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gamification + engagement persistence: XP events, weekly leaderboard,
 * badges, and per-day activity drill-down. Every statement is a
 * PreparedStatement.
 */
public class EngagementDAO {

    public void awardXp(int userId, int xp, String reason) throws SQLException {
        String sql = "INSERT INTO xp_events (user_id, xp, reason) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, xp);
            ps.setString(3, reason);
            ps.executeUpdate();
        }
    }

    /** Weekly leaderboard: XP earned Monday-onwards this week. */
    public List<Map<String, Object>> weeklyLeaderboard(int limit) throws SQLException {
        String sql = "SELECT u.user_id, u.name, COALESCE(SUM(x.xp), 0) total_xp, "
                   + "COUNT(DISTINCT CAST(x.created_at AS date)) active_days "
                   + "FROM users u "
                   + "LEFT JOIN xp_events x ON x.user_id = u.user_id "
                   + "  AND x.created_at >= DATE_TRUNC('week', NOW()) "
                   + "GROUP BY u.user_id, u.name "
                   + "ORDER BY total_xp DESC, u.name ASC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                int rank = 0;
                while (rs.next()) {
                    rank++;
                    Map<String, Object> row = new HashMap<>();
                    row.put("rank", rank);
                    row.put("userId", rs.getInt("user_id"));
                    row.put("name", rs.getString("name"));
                    row.put("xp", rs.getLong("total_xp"));
                    row.put("days", rs.getInt("active_days"));
                    out.add(row);
                }
                return out;
            }
        }
    }

    /** Activity level (0-3) for each day of a month in one round-trip. */
    public Map<java.time.LocalDate, Integer> monthActivity(int userId, int year, int month) throws SQLException {
        Map<java.time.LocalDate, Integer> out = new java.util.HashMap<>();
        String monthWindow = "  AND CAST(?::date AS date) >= DATE_TRUNC('month', ?::date) "
                           + "  AND CAST(?::date AS date) < DATE_TRUNC('month', ?::date) + INTERVAL '1 month'";
        String sql = "SELECT d, LEAST(3, COUNT(*)) lvl FROM ("
                   + "  SELECT CAST(attempt_date AS date) d FROM quiz_results WHERE user_id = ?" + monthWindow
                   + "  UNION ALL "
                   + "  SELECT CAST(last_reviewed_at AS date) FROM flashcard_reviews WHERE user_id = ? "
                   + "    AND last_reviewed_at IS NOT NULL" + monthWindow
                   + "  UNION ALL "
                   + "  SELECT CAST(created_at AS date) FROM notes WHERE user_id = ?" + monthWindow
                   + "  UNION ALL "
                   + "  SELECT CAST(created_at AS date) FROM chat_messages WHERE user_id = ? AND role = 'user'" + monthWindow
                   + ") t GROUP BY d";
        // Param order: user, first, first, first, then repeat per subquery.
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            java.time.LocalDate first = java.time.LocalDate.of(year, month, 1);
            for (int block = 0; block < 4; block++) {
                int base = block * 4;
                ps.setInt(base + 1, userId);
                ps.setObject(base + 2, first);
                ps.setObject(base + 3, first);
                ps.setObject(base + 4, first);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("d");
                    if (d != null) out.put(d.toLocalDate(), rs.getInt("lvl"));
                }
            }
        }
        return out;
    }

    /** All activity for one calendar day (quiz attempts + reviews + uploads + chats). */
    public List<Map<String, Object>> dayActivity(int userId, java.time.LocalDate day) throws SQLException {
        List<Map<String, Object>> out = new ArrayList<>();
        String sql;
        try (Connection con = DBConnection.getConnection()) {

            sql = "SELECT 'quiz' kind, n.title detail, qr.score * 100 / NULLIF(qr.total_questions,0) pct "
                + "FROM quiz_results qr JOIN notes n ON n.note_id = qr.note_id "
                + "WHERE qr.user_id = ? AND CAST(qr.attempt_date AS date) = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setObject(2, day);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) out.add(row("📝", "Quiz on " + rs.getString("detail"),
                            rs.getObject("pct") == null ? "" : rs.getInt("pct") + "% accuracy"));
                }
            }

            sql = "SELECT 'review' kind, COUNT(*) c FROM flashcard_reviews "
                + "WHERE user_id = ? AND CAST(last_reviewed_at AS date) = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setObject(2, day);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("c") > 0)
                        out.add(row("🧠", "Reviewed flashcards", rs.getInt("c") + " cards"));
                }
            }

            sql = "SELECT 'upload' kind, title FROM notes WHERE user_id = ? AND CAST(created_at AS date) = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setObject(2, day);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) out.add(row("📤", "Uploaded " + rs.getString("title"), ""));
                }
            }

            sql = "SELECT 'chat' kind, COUNT(*) c FROM chat_messages "
                + "WHERE user_id = ? AND role = 'user' AND CAST(created_at AS date) = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setObject(2, day);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("c") > 0)
                        out.add(row("💬", "AI chat questions", rs.getInt("c") + " asked"));
                }
            }
        }
        return out;
    }

    /** Badges already unlocked. */
    public java.util.Set<String> badgesFor(int userId) throws SQLException {
        String sql = "SELECT badge_key FROM user_badges WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                java.util.Set<String> out = new java.util.HashSet<>();
                while (rs.next()) out.add(rs.getString("badge_key"));
                return out;
            }
        }
    }

    public void grantBadge(int userId, String key) throws SQLException {
        String sql = "INSERT INTO user_badges (user_id, badge_key) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, key);
            ps.executeUpdate();
        }
    }

    /** Syncs badge unlocks from current stats. Returns newly earned keys. */
    public java.util.Set<String> syncBadges(int userId, int notes, int flashcards, double accuracy, int streak,
                                            boolean quizCompleted)
            throws SQLException {
        Map<String, Boolean> earned = new HashMap<>();
        earned.put("streak_7", streak >= 7);
        earned.put("flashcards_100", flashcards >= 100);
        earned.put("accuracy_90", accuracy >= 90.0);
        earned.put("notes_10", notes >= 10);
        earned.put("first_quiz", quizCompleted);

        java.util.Set<String> existing = badgesFor(userId);
        java.util.Set<String> fresh = new java.util.HashSet<>();
        for (Map.Entry<String, Boolean> e : earned.entrySet()) {
            if (e.getValue() && !existing.contains(e.getKey())) {
                grantBadge(userId, e.getKey());
                fresh.add(e.getKey());
            }
        }
        return fresh;
    }

    private Map<String, Object> row(String icon, String detail, String extra) {
        Map<String, Object> m = new HashMap<>();
        m.put("icon", icon);
        m.put("detail", detail);
        m.put("extra", extra);
        return m;
    }
}

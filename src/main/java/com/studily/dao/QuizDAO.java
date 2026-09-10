package com.studily.dao;

import com.studily.model.QuizResult;
import com.studily.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Quiz attempt persistence. Every statement is a PreparedStatement.
 */
public class QuizDAO {

    public int insert(int userId, int noteId, int score, int totalQuestions) throws SQLException {
        String sql = "INSERT INTO quiz_results (user_id, note_id, score, total_questions) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] { "id" })) {
            ps.setInt(1, userId);
            ps.setInt(2, noteId);
            ps.setInt(3, score);
            ps.setInt(4, totalQuestions);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public List<QuizResult> findRecentByUser(int userId, int limit) throws SQLException {
        String sql = "SELECT qr.id, qr.user_id, qr.note_id, qr.score, qr.total_questions, qr.attempt_date, n.title "
                   + "FROM quiz_results qr JOIN notes n ON qr.note_id = n.note_id "
                   + "WHERE qr.user_id = ? ORDER BY qr.attempt_date DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<QuizResult> results = new ArrayList<>();
                while (rs.next()) results.add(mapRow(rs));
                return results;
            }
        }
    }

    public int countByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM quiz_results WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Average score as a percentage across all attempts. */
    public double averageScorePercent(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(score) * 100.0 / NULLIF(SUM(total_questions), 0), 0) "
                   + "FROM quiz_results WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    /**
     * Study streak: consecutive days (ending today or yesterday) with at
     * least one quiz attempt or note upload. Uses DISTINCT calendar dates.
     * (Postgres: CAST AS date works on TIMESTAMPTZ.)
     */
    public int studyStreak(int userId) throws SQLException {
        String sql = "SELECT DISTINCT CAST(attempt_date AS date) FROM quiz_results WHERE user_id = ? "
                   + "UNION "
                   + "SELECT DISTINCT CAST(created_at AS date) FROM notes WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                java.util.Set<java.time.LocalDate> days = new java.util.HashSet<>();
                while (rs.next()) {
                    java.sql.Date d = rs.getDate(1);
                    if (d != null) days.add(d.toLocalDate());
                }
                return computeStreak(days);
            }
        }
    }

    static int computeStreak(java.util.Set<java.time.LocalDate> days) {
        if (days.isEmpty()) return 0;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate cursor = days.contains(today) ? today
                : days.contains(today.minusDays(1)) ? today.minusDays(1) : null;
        if (cursor == null) return 0;
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
    }
        return streak;
    }

    private QuizResult mapRow(ResultSet rs) throws SQLException {
        QuizResult r = new QuizResult();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setNoteId(rs.getInt("note_id"));
        r.setScore(rs.getInt("score"));
        r.setTotalQuestions(rs.getInt("total_questions"));
        Timestamp ts = rs.getTimestamp("attempt_date");
        r.setAttemptDate(ts == null ? null : ts.toLocalDateTime());
        r.setNoteTitle(rs.getString("title"));
        return r;
    }
}

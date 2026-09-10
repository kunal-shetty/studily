package com.studily.dao;

import com.studily.model.Flashcard;
import com.studily.model.FlashcardReview;
import com.studily.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spaced-repetition state persistence (SM-2 style). Every statement is a
 * PreparedStatement.
 */
public class ReviewDAO {

    /** Get (or lazily create) scheduling state for a card. */
    public FlashcardReview getOrCreate(int userId, int cardId) throws SQLException {
        String sql = "SELECT id, card_id, user_id, ease_factor, interval_days, repetitions, "
                   + "due_date, last_rating, last_reviewed_at "
                   + "FROM flashcard_reviews WHERE user_id = ? AND card_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        // Create default state (due immediately).
        FlashcardReview r = new FlashcardReview();
        r.setCardId(cardId);
        r.setUserId(userId);
        r.setEaseFactor(2.5);
        r.setIntervalDays(0);
        r.setRepetitions(0);
        r.setDueDate(java.time.LocalDateTime.now());
        String ins = "INSERT INTO flashcard_reviews (card_id, user_id, ease_factor, interval_days, repetitions, due_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(ins)) {
            ps.setInt(1, cardId);
            ps.setInt(2, userId);
            ps.setDouble(3, r.getEaseFactor());
            ps.setInt(4, 0);
            ps.setInt(5, 0);
            ps.setObject(6, r.getDueDate());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) r.setId(rs.getLong(1));
            }
        }
        return r;
    }

    public void update(FlashcardReview r) throws SQLException {
        String sql = "UPDATE flashcard_reviews SET ease_factor = ?, interval_days = ?, repetitions = ?, "
                   + "due_date = ?, last_rating = ?, last_reviewed_at = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, r.getEaseFactor());
            ps.setInt(2, r.getIntervalDays());
            ps.setInt(3, r.getRepetitions());
            ps.setObject(4, r.getDueDate());
            if (r.getLastRating() == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, r.getLastRating());
            ps.setObject(6, r.getLastReviewedAt());
            ps.setLong(7, r.getId());
            ps.executeUpdate();
        }
    }

    /** Cards due now: never-reviewed cards + cards whose due date passed. */
    public List<FlashcardReview> findDue(int userId, int limit) throws SQLException {
        String sql = "SELECT r.*, f.question, f.answer, f.note_id, n.title AS note_title "
                   + "FROM flashcards f "
                   + "LEFT JOIN flashcard_reviews r ON r.card_id = f.id AND r.user_id = ? "
                   + "JOIN notes n ON n.note_id = f.note_id AND n.user_id = ? "
                   + "WHERE r.id IS NULL OR r.due_date <= NOW() "
                   + "ORDER BY COALESCE(r.due_date, NOW()) ASC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<FlashcardReview> list = new ArrayList<>();
                while (rs.next()) list.add(mapJoinedRow(rs));
                return list;
            }
        }
    }

    public int countDue(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM flashcards f "
                   + "LEFT JOIN flashcard_reviews r ON r.card_id = f.id AND r.user_id = ? "
                   + "JOIN notes n ON n.note_id = f.note_id AND n.user_id = ? "
                   + "WHERE r.id IS NULL OR r.due_date <= NOW()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countTotalReviews(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM flashcard_reviews WHERE user_id = ? AND last_reviewed_at IS NOT NULL";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Daily review counts for the heatmap (last N days). */
    public Map<String, Integer> reviewActivity(int userId, int days) throws SQLException {
        String sql = "SELECT CAST(last_reviewed_at AS date) d, COUNT(*) c FROM flashcard_reviews "
                   + "WHERE user_id = ? AND last_reviewed_at >= NOW() - CAST(? AS INT) * INTERVAL '1 day' "
                   + "GROUP BY 1";
        Map<String, Integer> out = new java.util.LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate(1);
                    if (d != null) out.put(d.toLocalDate().toString(), rs.getInt(2));
                }
            }
        }
        return out;
    }

    private FlashcardReview mapRow(ResultSet rs) throws SQLException {
        FlashcardReview r = new FlashcardReview();
        r.setId(rs.getLong("id"));
        r.setCardId(rs.getInt("card_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setEaseFactor(rs.getDouble("ease_factor"));
        r.setIntervalDays(rs.getInt("interval_days"));
        r.setRepetitions(rs.getInt("repetitions"));
        Timestamp due = rs.getTimestamp("due_date");
        r.setDueDate(due == null ? null : due.toLocalDateTime());
        int lr = rs.getInt("last_rating");
        r.setLastRating(rs.wasNull() ? null : lr);
        Timestamp lra = rs.getTimestamp("last_reviewed_at");
        r.setLastReviewedAt(lra == null ? null : lra.toLocalDateTime());
        return r;
    }

    private FlashcardReview mapJoinedRow(ResultSet rs) throws SQLException {
        FlashcardReview r = mapRow(rs);
        Flashcard f = new Flashcard();
        f.setId(rs.getInt("card_id"));
        f.setQuestion(rs.getString("question"));
        f.setAnswer(rs.getString("answer"));
        f.setNoteId(rs.getInt("note_id"));
        r.setQuestion(f.getQuestion());
        r.setAnswer(f.getAnswer());
        r.setNoteId(f.getNoteId());
        r.setNoteTitle(rs.getString("note_title"));
        return r;
    }
}

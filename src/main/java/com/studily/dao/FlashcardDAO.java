package com.studily.dao;

import com.studily.model.Flashcard;
import com.studily.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Flashcard persistence. Every statement is a PreparedStatement.
 */
public class FlashcardDAO {

    /** Bulk insert flashcards for a note (single transaction). */
    public void insertBatch(int noteId, List<Flashcard> cards) throws SQLException {
        String sql = "INSERT INTO flashcards (note_id, question, answer) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Flashcard c : cards) {
                    ps.setInt(1, noteId);
                    ps.setString(2, c.getQuestion());
                    ps.setString(3, c.getAnswer());
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Flashcard> findByNote(int noteId) throws SQLException {
        String sql = "SELECT id, note_id, question, answer FROM flashcards WHERE note_id = ? ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Flashcard> cards = new ArrayList<>();
                while (rs.next()) {
                    Flashcard c = new Flashcard();
                    c.setId(rs.getInt("id"));
                    c.setNoteId(rs.getInt("note_id"));
                    c.setQuestion(rs.getString("question"));
                    c.setAnswer(rs.getString("answer"));
                    cards.add(c);
                }
                return cards;
            }
        }
    }

    /** Edit a card's question/answer — ownership-scoped through the note. */
    public boolean updateCard(int userId, int cardId, String question, String answer) throws SQLException {
        String sql = "UPDATE flashcards SET question = ?, answer = ? "
                   + "FROM notes WHERE flashcards.note_id = notes.note_id "
                   + "AND flashcards.id = ? AND notes.user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, question);
            ps.setString(2, answer);
            ps.setInt(3, cardId);
            ps.setInt(4, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public int countByNote(int noteId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM flashcards WHERE note_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM flashcards f "
                   + "JOIN notes n ON f.note_id = n.note_id WHERE n.user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}

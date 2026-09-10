package com.studily.dao;

import com.studily.model.Note;
import com.studily.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Notes CRUD. Every statement is a PreparedStatement.
 */
public class NotesDAO {

    public int insert(int userId, String title, String pdfPath, String extractedText) throws SQLException {
        String sql = "INSERT INTO notes (user_id, title, pdf_path, extracted_text) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] { "note_id" })) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, pdfPath);
            ps.setString(4, extractedText);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Store the AI summary JSON after generation. */
    public void updateSummary(int noteId, String summaryJson) throws SQLException {
        String sql = "UPDATE notes SET summary = ? WHERE note_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, summaryJson);
            ps.setInt(2, noteId);
            ps.executeUpdate();
        }
    }

    /** Fetch a note only if it belongs to the given user. */
    public Note findByIdAndUser(int noteId, int userId) throws SQLException {
        String sql = "SELECT note_id, user_id, title, pdf_path, extracted_text, summary, created_at "
                   + "FROM notes WHERE note_id = ? AND user_id = ? LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Note> findRecentByUser(int userId, int limit) throws SQLException {
        String sql = "SELECT note_id, user_id, title, pdf_path, extracted_text, summary, created_at "
                   + "FROM notes WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Note> notes = new ArrayList<>();
                while (rs.next()) notes.add(mapRow(rs));
                return notes;
            }
        }
    }

    public List<Note> findAllByUser(int userId) throws SQLException {
        return findRecentByUser(userId, 500);
    }

    public int countByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notes WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean deleteByIdAndUser(int noteId, int userId) throws SQLException {
        String sql = "DELETE FROM notes WHERE note_id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    private Note mapRow(ResultSet rs) throws SQLException {
        Note n = new Note();
        n.setNoteId(rs.getInt("note_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setPdfPath(rs.getString("pdf_path"));
        n.setExtractedText(rs.getString("extracted_text"));
        n.setSummaryJson(rs.getString("summary"));
        Timestamp ts = rs.getTimestamp("created_at");
        n.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
        return n;
    }
}

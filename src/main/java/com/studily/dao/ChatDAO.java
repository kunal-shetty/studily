package com.studily.dao;

import com.studily.model.ChatMessage;
import com.studily.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat history persistence. Every statement is a PreparedStatement.
 */
public class ChatDAO {

    public void insert(int userId, int noteId, String role, String content) throws SQLException {
        String sql = "INSERT INTO chat_messages (user_id, note_id, role, content) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, noteId);
            ps.setString(3, role);
            ps.setString(4, content);
            ps.executeUpdate();
        }
    }

    public List<ChatMessage> findByNote(int userId, int noteId, int limit) throws SQLException {
        // Newest first, then reverse in Java for chronological display.
        String sql = "SELECT id, user_id, note_id, role, content, created_at "
                   + "FROM chat_messages WHERE user_id = ? AND note_id = ? ORDER BY id DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, noteId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChatMessage> list = new ArrayList<>();
                while (rs.next()) {
                    ChatMessage m = new ChatMessage();
                    m.setId(rs.getLong("id"));
                    m.setUserId(rs.getInt("user_id"));
                    m.setNoteId(rs.getInt("note_id"));
                    m.setRole(rs.getString("role"));
                    m.setContent(rs.getString("content"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    m.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
                    list.add(m);
                }
                java.util.Collections.reverse(list);
                return list;
            }
        }
    }

    public void clearForNote(int userId, int noteId) throws SQLException {
        String sql = "DELETE FROM chat_messages WHERE user_id = ? AND note_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, noteId);
            ps.executeUpdate();
        }
    }
}

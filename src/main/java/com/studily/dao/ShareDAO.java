package com.studily.dao;

import com.studily.util.DBConnection;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Note share links and subject folders. Every statement is a
 * PreparedStatement.
 */
public class ShareDAO {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Create (or return existing) view-only share token for a note. */
    public String createShare(int noteId, int userId) throws SQLException {
        String existing = "SELECT ns.token FROM note_shares ns "
                        + "JOIN notes n ON n.note_id = ns.note_id "
                        + "WHERE ns.note_id = ? AND n.user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(existing)) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String ins = "INSERT INTO note_shares (note_id, token) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(ins)) {
            ps.setInt(1, noteId);
            ps.setString(2, token);
            ps.executeUpdate();
        }
        return token;
    }

    /** Public lookup for the share view — no user scoping (token IS the credential). */
    public Map<String, Object> findSharedNote(String token) throws SQLException {
        String sql = "SELECT n.note_id, n.title, n.summary, n.created_at, u.name AS owner "
                   + "FROM note_shares ns "
                   + "JOIN notes n ON n.note_id = ns.note_id "
                   + "JOIN users u ON u.user_id = n.user_id "
                   + "WHERE ns.token = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Map<String, Object> out = new HashMap<>();
                out.put("noteId", rs.getInt("note_id"));
                out.put("title", rs.getString("title"));
                out.put("summaryJson", rs.getString("summary"));
                out.put("createdAt", rs.getTimestamp("created_at"));
                out.put("owner", rs.getString("owner"));
                return out;
            }
        }
    }

    // ---------- Subjects ----------

    public int createSubject(int userId, String name) throws SQLException {
        String sql = "INSERT INTO subjects (user_id, name) VALUES (?, ?) RETURNING id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /** subject_id -> name for a user's folders. */
    public Map<Integer, String> subjectsFor(int userId) throws SQLException {
        String sql = "SELECT id, name FROM subjects WHERE user_id = ? ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Integer, String> out = new HashMap<>();
                while (rs.next()) out.put(rs.getInt("id"), rs.getString("name"));
                return out;
            }
        }
    }
}

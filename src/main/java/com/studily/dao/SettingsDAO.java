package com.studily.dao;

import com.studily.model.UserSettings;
import com.studily.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * User preferences persistence. Every statement is a PreparedStatement.
 */
public class SettingsDAO {

    public UserSettings getOrCreate(int userId) throws SQLException {
        String sql = "SELECT user_id, theme, accent, ai_model, notifications FROM user_settings WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserSettings s = new UserSettings();
                    s.setUserId(rs.getInt("user_id"));
                    s.setTheme(rs.getString("theme"));
                    s.setAccent(rs.getString("accent"));
                    s.setAiModel(rs.getString("ai_model"));
                    s.setNotifications(rs.getBoolean("notifications"));
                    return s;
                }
            }
        }
        UserSettings s = new UserSettings();
        s.setUserId(userId);
        String ins = "INSERT INTO user_settings (user_id, theme, accent, ai_model, notifications, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(ins)) {
            ps.setInt(1, userId);
            ps.setString(2, s.getTheme());
            ps.setString(3, s.getAccent());
            ps.setString(4, s.getAiModel());
            ps.setBoolean(5, s.isNotifications());
            ps.setObject(6, LocalDateTime.now());
            ps.executeUpdate();
        }
        return s;
    }

    public void update(UserSettings s) throws SQLException {
        String sql = "UPDATE user_settings SET theme = ?, accent = ?, ai_model = ?, notifications = ?, updated_at = ? "
                   + "WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getTheme());
            ps.setString(2, s.getAccent());
            ps.setString(3, s.getAiModel());
            ps.setBoolean(4, s.isNotifications());
            ps.setObject(5, LocalDateTime.now());
            ps.setInt(6, s.getUserId());
            ps.executeUpdate();
        }
    }
}

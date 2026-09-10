package com.studily.dao;

import com.studily.model.MCQ;
import com.studily.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MCQ persistence. Every statement is a PreparedStatement.
 */
public class MCQDAO {

    public void insertBatch(int noteId, List<MCQ> mcqs) throws SQLException {
        String sql = "INSERT INTO mcqs (note_id, question, option_a, option_b, option_c, option_d, "
                   + "correct_answer, explanation, difficulty) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (MCQ m : mcqs) {
                    ps.setInt(1, noteId);
                    ps.setString(2, m.getQuestion());
                    ps.setString(3, m.getOptionA());
                    ps.setString(4, m.getOptionB());
                    ps.setString(5, m.getOptionC());
                    ps.setString(6, m.getOptionD());
                    ps.setString(7, m.getCorrectAnswer());
                    ps.setString(8, m.getExplanation());
                    ps.setString(9, m.getDifficulty());
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

    public List<MCQ> findByNote(int noteId, String difficulty) throws SQLException {
        String sql = "SELECT id, note_id, question, option_a, option_b, option_c, option_d, "
                   + "correct_answer, explanation, difficulty FROM mcqs WHERE note_id = ?";
        if (difficulty != null && !difficulty.isBlank()) {
            sql += " AND difficulty = ?";
        }
        sql += " ORDER BY id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            if (difficulty != null && !difficulty.isBlank()) {
                ps.setString(2, difficulty);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<MCQ> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    public int countByNote(int noteId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mcqs WHERE note_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mcqs m JOIN notes n ON m.note_id = n.note_id WHERE n.user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public MCQ findById(int id) throws SQLException {
        String sql = "SELECT id, note_id, question, option_a, option_b, option_c, option_d, "
                   + "correct_answer, explanation, difficulty FROM mcqs WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    private MCQ mapRow(ResultSet rs) throws SQLException {
        MCQ m = new MCQ();
        m.setId(rs.getInt("id"));
        m.setNoteId(rs.getInt("note_id"));
        m.setQuestion(rs.getString("question"));
        m.setOptionA(rs.getString("option_a"));
        m.setOptionB(rs.getString("option_b"));
        m.setOptionC(rs.getString("option_c"));
        m.setOptionD(rs.getString("option_d"));
        m.setCorrectAnswer(rs.getString("correct_answer"));
        m.setExplanation(rs.getString("explanation"));
        m.setDifficulty(rs.getString("difficulty"));
        return m;
    }
}

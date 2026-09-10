package com.studily.dao;

import com.studily.model.QuizAnswer;
import com.studily.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Per-question quiz answer persistence. Every statement is a
 * PreparedStatement.
 */
public class QuizAnswerDAO {

    public void insertBatch(int resultId, int userId, List<QuizAnswer> answers) throws SQLException {
        String sql = "INSERT INTO quiz_answers (result_id, mcq_id, user_id, chosen_answer, is_correct, note_question) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (QuizAnswer a : answers) {
                    ps.setInt(1, resultId);
                    ps.setInt(2, a.getMcqId());
                    ps.setInt(3, userId);
                    if (a.getChosenAnswer() == null) ps.setNull(4, Types.VARCHAR);
                    else ps.setString(4, a.getChosenAnswer());
                    ps.setBoolean(5, a.isCorrect());
                    ps.setString(6, a.getNoteQuestion());
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

    /** Wrongly-answered questions grouped across attempts (weak-topic input). */
    public List<QuizAnswer> findRecentWrong(int userId, int limit) throws SQLException {
        String sql = "SELECT id, result_id, mcq_id, user_id, chosen_answer, is_correct, note_question "
                   + "FROM quiz_answers WHERE user_id = ? AND is_correct = FALSE "
                   + "ORDER BY id DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<QuizAnswer> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    public double accuracyByUser(int userId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(CASE WHEN is_correct THEN 100.0 ELSE 0 END), 0) "
                   + "FROM quiz_answers WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    private QuizAnswer mapRow(ResultSet rs) throws SQLException {
        QuizAnswer a = new QuizAnswer();
        a.setId(rs.getLong("id"));
        a.setResultId(rs.getInt("result_id"));
        a.setMcqId(rs.getInt("mcq_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setChosenAnswer(rs.getString("chosen_answer"));
        a.setCorrect(rs.getBoolean("is_correct"));
        a.setNoteQuestion(rs.getString("note_question"));
        return a;
    }
}

package com.studily.model;

import java.time.LocalDateTime;

/**
 * One completed quiz attempt.
 */
public class QuizResult {

    private int id;
    private int userId;
    private int noteId;
    private int score;
    private int totalQuestions;
    private LocalDateTime attemptDate;
    private String noteTitle; // joined for display

    public double getPercentage() {
        return totalQuestions == 0 ? 0.0 : (score * 100.0) / totalQuestions;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getNoteId() { return noteId; }
    public void setNoteId(int noteId) { this.noteId = noteId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getAttemptDate() { return attemptDate; }
    public void setAttemptDate(LocalDateTime attemptDate) { this.attemptDate = attemptDate; }

    public String getNoteTitle() { return noteTitle; }
    public void setNoteTitle(String noteTitle) { this.noteTitle = noteTitle; }
}

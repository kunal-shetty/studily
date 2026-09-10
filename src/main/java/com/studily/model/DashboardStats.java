package com.studily.model;

/**
 * Dashboard metrics — every value computed from MySQL via DAOs.
 */
public class DashboardStats {

    private int totalNotes;
    private int totalFlashcards;
    private int quizzesCompleted;
    private double averageScore;
    private int studyStreak;
    private java.util.List<QuizResult> recentQuizzes;
    private java.util.List<Note> recentNotes;

    public int getTotalNotes() { return totalNotes; }
    public void setTotalNotes(int totalNotes) { this.totalNotes = totalNotes; }

    public int getTotalFlashcards() { return totalFlashcards; }
    public void setTotalFlashcards(int totalFlashcards) { this.totalFlashcards = totalFlashcards; }

    public int getQuizzesCompleted() { return quizzesCompleted; }
    public void setQuizzesCompleted(int quizzesCompleted) { this.quizzesCompleted = quizzesCompleted; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public int getStudyStreak() { return studyStreak; }
    public void setStudyStreak(int studyStreak) { this.studyStreak = studyStreak; }

    public java.util.List<QuizResult> getRecentQuizzes() { return recentQuizzes; }
    public void setRecentQuizzes(java.util.List<QuizResult> recentQuizzes) { this.recentQuizzes = recentQuizzes; }

    public java.util.List<Note> getRecentNotes() { return recentNotes; }
    public void setRecentNotes(java.util.List<Note> recentNotes) { this.recentNotes = recentNotes; }
}

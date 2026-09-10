package com.studily.service;

import com.studily.dao.FlashcardDAO;
import com.studily.dao.NotesDAO;
import com.studily.dao.QuizDAO;
import com.studily.model.DashboardStats;
import com.studily.model.Note;
import com.studily.model.QuizResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Aggregates DAO queries into dashboard statistics.
 */
public class DashboardService {

    private final NotesDAO notesDAO = new NotesDAO();
    private final FlashcardDAO flashcardDAO = new FlashcardDAO();
    private final QuizDAO quizDAO = new QuizDAO();

    public DashboardStats buildStats(int userId) throws SQLException {
        DashboardStats stats = new DashboardStats();

        stats.setTotalNotes(notesDAO.countByUser(userId));
        stats.setTotalFlashcards(flashcardDAO.countByUser(userId));
        stats.setQuizzesCompleted(quizDAO.countByUser(userId));
        stats.setAverageScore(quizDAO.averageScorePercent(userId));
        stats.setStudyStreak(quizDAO.studyStreak(userId));

        List<Note> recentNotes = notesDAO.findRecentByUser(userId, 5);
        List<QuizResult> recentQuizzes = quizDAO.findRecentByUser(userId, 5);
        stats.setRecentNotes(recentNotes);
        stats.setRecentQuizzes(recentQuizzes);

        return stats;
    }
}

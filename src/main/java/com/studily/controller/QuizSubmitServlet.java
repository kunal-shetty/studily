package com.studily.controller;

import com.studily.dao.EngagementDAO;
import com.studily.dao.MCQDAO;
import com.studily.dao.NotesDAO;
import com.studily.dao.QuizAnswerDAO;
import com.studily.dao.QuizDAO;
import com.studily.model.QuizAnswer;
import com.studily.model.MCQ;
import com.studily.model.Note;
import com.studily.model.User;
import com.studily.util.Flash;
import com.studily.util.Log;
import com.studily.util.Validators;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Grades the session quiz, stores the attempt, and shows results with
 * instant feedback and AI explanations.
 */
@WebServlet(name = "quizSubmitServlet", urlPatterns = {"/quiz-submit"})
public class QuizSubmitServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final MCQDAO mcqDAO = new MCQDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final QuizAnswerDAO quizAnswerDAO = new QuizAnswerDAO();
    private final EngagementDAO engagementDAO = new EngagementDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        HttpSession session = request.getSession(false);

        Long startedAt = (session == null) ? null : (Long) session.getAttribute("quiz.startedAt");
        Integer noteId = (session == null) ? null : (Integer) session.getAttribute("quiz.noteId");
        @SuppressWarnings("unchecked")
        List<Integer> ids = (session == null) ? null : (List<Integer>) session.getAttribute("quiz.ids");
        String difficulty = (session == null) ? null : (String) session.getAttribute("quiz.difficulty");

        if (noteId == null || ids == null || ids.isEmpty()) {
            Flash.error(request, "No active quiz found. Start a quiz from a note's summary page.");
            redirect(request, response, "/dashboard");
            return;
        }

        Note note;
        List<MCQ> mcqs;
        try {
            note = notesDAO.findByIdAndUser(noteId, user.getUserId());
            mcqs = mcqDAO.findByNote(noteId, "all".equals(difficulty) ? null : difficulty);
        } catch (SQLException e) {
            Flash.error(request, "Database error while grading your quiz.");
            redirect(request, response, "/dashboard");
            return;
        }
        if (note == null) {
            Flash.error(request, "Note not found.");
            redirect(request, response, "/dashboard");
            return;
        }

        // Keep only the questions that were actually part of this attempt.
        mcqs.removeIf(m -> !ids.contains(m.getId()));
        if (mcqs.isEmpty()) {
            Flash.error(request, "Quiz state expired. Please start the quiz again.");
            redirect(request, response, "/summary?noteId=" + noteId);
            return;
        }

        // --- Grade ---
        Map<Integer, String> chosen = new java.util.HashMap<>();
        for (MCQ m : mcqs) {
            String value = request.getParameter("q" + m.getId());
            if (value != null && !value.isBlank()) {
                chosen.put(m.getId(), value.trim().toUpperCase().substring(0, 1));
            }
        }

        int score = 0;
        for (MCQ m : mcqs) {
            if (m.isCorrect(chosen.get(m.getId()))) score++;
        }

        // --- Persist attempt + per-question answers ---
        try {
            int resultId = quizDAO.insert(user.getUserId(), noteId, score, mcqs.size());
            List<QuizAnswer> answers = new ArrayList<>();
            for (MCQ m : mcqs) {
                QuizAnswer a = new QuizAnswer();
                a.setResultId(resultId);
                a.setMcqId(m.getId());
                a.setUserId(user.getUserId());
                a.setChosenAnswer(chosen.get(m.getId()));
                a.setCorrect(m.isCorrect(chosen.get(m.getId())));
                a.setNoteQuestion(m.getQuestion());
                answers.add(a);
            }
            quizAnswerDAO.insertBatch(resultId, user.getUserId(), answers);
        } catch (SQLException e) {
            Log.severe("Quiz result save failed (user " + user.getUserId() + "): " + e.getMessage(), e);
            Flash.error(request, "Could not save your quiz result.");
        }

        // --- Gamification: XP + badges (never blocks the result page) ---
        try {
            int xp = 10 + score * 4;
            if (Boolean.TRUE.equals(session.getAttribute("studySession"))) xp += 20;
            engagementDAO.awardXp(user.getUserId(), xp, "quiz");
            double pct = mcqs.isEmpty() ? 0 : (score * 100.0 / mcqs.size());
            int notes = notesDAO.countByUser(user.getUserId());
            int cards = new com.studily.dao.FlashcardDAO().countByUser(user.getUserId());
            double accuracy = quizAnswerDAO.accuracyByUser(user.getUserId());
            int streak = 0;
            try {
                streak = new com.studily.service.DashboardService().buildStats(user.getUserId()).getStudyStreak();
            } catch (Exception ignored) {
            }
            var fresh = engagementDAO.syncBadges(user.getUserId(), notes, cards, accuracy, streak, true);
            if (!fresh.isEmpty()) {
                Flash.success(request, "🏅 New badge unlocked!");
            }
        } catch (Exception e) {
            Log.severe("XP/badge award failed (user " + user.getUserId() + "): " + e.getMessage(), e);
        }
        session.removeAttribute("studySession");

        // --- Clear quiz session state ---
        session.removeAttribute("quiz.noteId");
        session.removeAttribute("quiz.ids");
        session.removeAttribute("quiz.difficulty");
        session.removeAttribute("quiz.deadline");
        session.removeAttribute("quiz.startedAt");

        request.setAttribute("note", note);
        request.setAttribute("mcqs", mcqs);
        request.setAttribute("chosen", chosen);
        request.setAttribute("score", score);
        request.setAttribute("total", mcqs.size());
        request.setAttribute("elapsedSeconds",
                startedAt == null ? 0L : (System.currentTimeMillis() - startedAt) / 1000);
        render(request, response, "quiz-result.jsp");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        redirect(request, response, "/dashboard");
    }
}

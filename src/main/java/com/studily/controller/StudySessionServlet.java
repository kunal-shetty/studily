package com.studily.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.studily.dao.EngagementDAO;
import com.studily.dao.FlashcardDAO;
import com.studily.dao.MCQDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.Flashcard;
import com.studily.model.MCQ;
import com.studily.model.Note;
import com.studily.model.User;
import com.studily.service.AIService;
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
import java.util.List;

/**
 * The AI Study Session: choose a note, Groq builds a personalized 20-minute
 * plan, then the session view funnels the student through focus areas,
 * flashcards, and a 10-question quiz. Completing the session awards XP.
 */
@WebServlet(name = "studySessionServlet", urlPatterns = {"/session"})
public class StudySessionServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final FlashcardDAO flashcardDAO = new FlashcardDAO();
    private final MCQDAO mcqDAO = new MCQDAO();
    private final EngagementDAO engagementDAO = new EngagementDAO();
    private final Gson gson = new Gson();

    /** Step 1: choose a subject/note. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        try {
            request.setAttribute("notes", notesDAO.findAllByUser(user.getUserId()));
        } catch (SQLException e) {
            Log.severe("Session note list failed (user " + user.getUserId() + "): " + e.getMessage(), e);
            request.setAttribute("notes", List.of());
        }
        request.setAttribute("navActive", "session");
        render(request, response, "session-start.jsp");
    }

    /** Step 2: build the AI plan and land on the guided session view. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));
        if (noteId == null) {
            Flash.error(request, "Pick a note to study.");
            redirect(request, response, "/session");
            return;
        }

        Note note;
        List<Flashcard> cards;
        List<MCQ> mcqs;
        try {
            note = notesDAO.findByIdAndUser(noteId, user.getUserId());
            if (note == null) {
                Flash.error(request, "Note not found.");
                redirect(request, response, "/session");
                return;
            }
            cards = flashcardDAO.findByNote(noteId);
            mcqs = mcqDAO.findByNote(noteId, null);
        } catch (SQLException e) {
            Log.severe("Session load failed (note " + noteId + "): " + e.getMessage(), e);
            Flash.error(request, "Database error while preparing your session.");
            redirect(request, response, "/session");
            return;
        }

        if (note.getSummaryJson() == null || note.getSummaryJson().isBlank() || mcqs.isEmpty()) {
            Flash.info(request, "Generate the study material for this note first, then start a session.");
            redirect(request, response, "/summary?noteId=" + noteId);
            return;
        }

        // AI plan (cached per session attempt; one API call).
        String planJson;
        try {
            planJson = AIService.buildSessionPlan(note.getTitle(), note.getExtractedText());
        } catch (AIService.AIServiceException e) {
            Log.warning("Session plan fallback (note " + noteId + "): " + e.getMessage());
            planJson = "{\"intro\":\"A focused 20-minute pass through this material.\","
                     + "\"focus\":[\"Read the summary\",\"Review every flashcard\",\"Take the quiz\"],"
                     + "\"tip\":\"Say each answer out loud before flipping the card.\"}";
        }

        // Trim the quiz to 10 questions for the session window.
        List<MCQ> sessionMcqs = mcqs.size() > 10 ? mcqs.subList(0, 10) : mcqs;

        // Stash the quiz state exactly like QuizServlet does, so the built-in
        // quiz form posts to /quiz-submit and gets graded normally.
        HttpSession session = request.getSession();
        session.setAttribute("quiz.noteId", noteId);
        session.setAttribute("quiz.ids", sessionMcqs.stream().map(MCQ::getId).toList());
        session.setAttribute("quiz.difficulty", "all");
        session.setAttribute("quiz.startedAt", System.currentTimeMillis());
        session.setAttribute("quiz.deadline", System.currentTimeMillis() + 20 * 60 * 1000L);
        session.setAttribute("studySession", Boolean.TRUE);

        request.setAttribute("note", note);
        request.setAttribute("plan", parsePlan(planJson));
        request.setAttribute("cards", cards);
        request.setAttribute("mcqs", sessionMcqs);
        render(request, response, "session.jsp");
    }

    private JsonObject parsePlan(String planJson) {
        try {
            String s = planJson.trim();
            int brace = s.indexOf('{');
            int lastBrace = s.lastIndexOf('}');
            if (brace >= 0 && lastBrace > brace) {
                return JsonParser.parseString(s.substring(brace, lastBrace + 1)).getAsJsonObject();
            }
        } catch (Exception ignored) {
        }
        JsonObject fallback = new JsonObject();
        fallback.addProperty("intro", "A focused 20-minute pass through this material.");
        fallback.addProperty("tip", "Say each answer out loud before flipping the card.");
        com.google.gson.JsonArray focus = new com.google.gson.JsonArray();
        focus.add("Read the summary");
        focus.add("Review every flashcard");
        focus.add("Take the quiz");
        fallback.add("focus", focus);
        return fallback;
    }

    /** XP award endpoint hit when the session quiz completes. */
    static void awardSessionXp(User user, int score, int total) {
        EngagementDAO engagementDAO = new EngagementDAO();
        try {
            int xp = 20 + score * 5;
            engagementDAO.awardXp(user.getUserId(), xp, "study_session");
        } catch (Exception e) {
            Log.severe("Session XP award failed (user " + user.getUserId() + "): " + e.getMessage(), e);
        }
    }
}

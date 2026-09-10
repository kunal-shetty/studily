package com.studily.controller;

import com.studily.dao.MCQDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.MCQ;
import com.studily.model.Note;
import com.studily.model.User;
import com.studily.util.Flash;
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

/**
 * Quiz mode. Stores quiz state in the session: the question ids in play,
 * the user's chosen answers, and the deadline for the timer.
 */
@WebServlet(name = "quizServlet", urlPatterns = {"/quiz"})
public class QuizServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final MCQDAO mcqDAO = new MCQDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));
        if (noteId == null) {
            Flash.error(request, "Invalid note reference.");
            redirect(request, response, "/dashboard");
            return;
        }

        String difficulty = request.getParameter("difficulty");
        if (difficulty != null && List.of("easy", "medium", "hard").contains(difficulty)) {
            // keep
        } else {
            difficulty = null;
        }

        Note note;
        List<MCQ> mcqs;
        try {
            note = notesDAO.findByIdAndUser(noteId, user.getUserId());
            if (note == null) {
                Flash.error(request, "Note not found.");
                redirect(request, response, "/dashboard");
                return;
            }
            mcqs = mcqDAO.findByNote(noteId, difficulty);
        } catch (SQLException e) {
            Flash.error(request, "Database error while loading the quiz.");
            redirect(request, response, "/dashboard");
            return;
        }

        if (mcqs.isEmpty()) {
            Flash.info(request, difficulty == null
                    ? "No MCQs available for this note — generate study material first."
                    : "No " + difficulty + " MCQs for this note. Try another difficulty.");
            redirect(request, response, "/summary?noteId=" + noteId);
            return;
        }

        // --- Initialize session quiz state ---
        HttpSession session = request.getSession();
        List<Integer> ids = new ArrayList<>();
        for (MCQ m : mcqs) ids.add(m.getId());
        session.setAttribute("quiz.noteId", noteId);
        session.setAttribute("quiz.ids", ids);
        session.setAttribute("quiz.difficulty", difficulty == null ? "all" : difficulty);
        session.setAttribute("quiz.answers", new java.util.HashMap<Integer, String>());
        session.setAttribute("quiz.deadline", System.currentTimeMillis() + mcqs.size() * 60_000L);
        session.setAttribute("quiz.startedAt", System.currentTimeMillis());

        request.setAttribute("note", note);
        request.setAttribute("mcqs", mcqs);
        request.setAttribute("difficulty", difficulty == null ? "all" : difficulty);
        render(request, response, "quiz.jsp");
    }
}

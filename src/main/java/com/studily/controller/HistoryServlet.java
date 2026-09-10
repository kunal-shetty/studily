package com.studily.controller;

import com.studily.dao.NotesDAO;
import com.studily.dao.QuizDAO;
import com.studily.model.Note;
import com.studily.model.QuizResult;
import com.studily.model.User;
import com.studily.util.Flash;
import com.studily.util.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * History page: all notes and all quiz attempts for the logged-in user.
 */
@WebServlet(name = "historyServlet", urlPatterns = {"/history"})
public class HistoryServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final QuizDAO quizDAO = new QuizDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        try {
            List<Note> notes = notesDAO.findAllByUser(user.getUserId());
            List<QuizResult> attempts = quizDAO.findRecentByUser(user.getUserId(), 200);
            request.setAttribute("notes", notes);
            request.setAttribute("attempts", attempts);
        } catch (SQLException e) {
            Log.severe("History load failed for user " + user.getUserId() + ": " + e.getMessage(), e);
            Flash.error(request, "Could not load your history.");
            List<Note> notes = List.of();
            List<QuizResult> attempts = List.of();
            request.setAttribute("notes", notes);
            request.setAttribute("attempts", attempts);
        }
        render(request, response, "history.jsp");
    }
}

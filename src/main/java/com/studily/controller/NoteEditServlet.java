package com.studily.controller;

import com.studily.dao.FlashcardDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.User;
import com.studily.util.Flash;
import com.studily.util.Log;
import com.studily.util.Validators;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Note actions: toggle bookmark, and edit a custom flashcard's
 * question/answer (AI cards become the student's own).
 */
@WebServlet(name = "noteEditServlet", urlPatterns = {"/note-edit"})
public class NoteEditServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final FlashcardDAO flashcardDAO = new FlashcardDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String action = request.getParameter("action");
        Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));
        String back = "/summary?noteId=" + (noteId == null ? "" : noteId);

        if (noteId == null) {
            Flash.error(request, "Invalid note reference.");
            redirect(request, response, "/dashboard");
            return;
        }

        try {
            switch (action == null ? "" : action) {
                case "bookmark" -> {
                    boolean current = Boolean.parseBoolean(request.getParameter("value"));
                    notesDAO.setBookmarked(noteId, user.getUserId(), !current);
                    Flash.success(request, !current ? "⭐ Bookmarked for quick revision." : "Bookmark removed.");
                    back = "/history";
                }
                case "edit-card" -> {
                    Integer cardId = Validators.parseIntOrNull(request.getParameter("cardId"));
                    String question = Validators.sanitizeText(request.getParameter("question"));
                    String answer = Validators.sanitizeText(request.getParameter("answer"));
                    if (cardId == null || question == null || question.isBlank()
                            || answer == null || answer.isBlank()) {
                        Flash.error(request, "Both sides of the card are required.");
                    } else {
                        flashcardDAO.updateCard(user.getUserId(), cardId,
                                question.trim(), answer.trim());
                        Flash.success(request, "Card updated.");
                    }
                    back = "/flashcards?noteId=" + noteId;
                }
                case "delete" -> {
                    boolean deleted = notesDAO.deleteByIdAndUser(noteId, user.getUserId());
                    Flash.success(request, deleted ? "Note deleted." : "Note not found.");
                    back = "/history";
                }
                default -> Flash.error(request, "Unknown action.");
            }
        } catch (SQLException e) {
            Log.severe("Note edit failed (user " + user.getUserId() + "): " + e.getMessage(), e);
            Flash.error(request, "Database error. Please retry.");
        }
        redirect(request, response, back);
    }
}

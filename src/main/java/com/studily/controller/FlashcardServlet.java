package com.studily.controller;

import com.studily.dao.FlashcardDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.Flashcard;
import com.studily.model.Note;
import com.studily.model.User;
import com.studily.util.Flash;
import com.studily.util.Validators;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Flashcard study view for one note.
 */
@WebServlet(name = "flashcardServlet", urlPatterns = {"/flashcards"})
public class FlashcardServlet extends BaseAppServlet {

    private final FlashcardDAO flashcardDAO = new FlashcardDAO();
    private final NotesDAO notesDAO = new NotesDAO();

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

        Note note;
        List<Flashcard> cards;
        try {
            note = notesDAO.findByIdAndUser(noteId, user.getUserId());
            if (note == null) {
                Flash.error(request, "Note not found.");
                redirect(request, response, "/dashboard");
                return;
            }
            cards = flashcardDAO.findByNote(noteId);
        } catch (SQLException e) {
            Flash.error(request, "Database error while loading flashcards.");
            redirect(request, response, "/dashboard");
            return;
        }

        if (cards.isEmpty()) {
            Flash.info(request, "No flashcards for this note yet — generate study material first.");
            redirect(request, response, "/summary?noteId=" + noteId);
            return;
        }

        request.setAttribute("note", note);
        request.setAttribute("cards", cards);
        render(request, response, "flashcards.jsp");
    }
}

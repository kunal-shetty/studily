package com.studily.controller;

import com.google.gson.Gson;
import com.studily.dao.FlashcardDAO;
import com.studily.dao.MCQDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.Note;
import com.studily.model.StudyKit;
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
 * Summary page for a note: parsed from stored JSON plus counts.
 */
@WebServlet(name = "summaryServlet", urlPatterns = {"/summary"})
public class SummaryServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final FlashcardDAO flashcardDAO = new FlashcardDAO();
    private final MCQDAO mcqDAO = new MCQDAO();
    private final Gson gson = new Gson();

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
        try {
            note = notesDAO.findByIdAndUser(noteId, user.getUserId());
        } catch (SQLException e) {
            Log.severe("Summary load failed (note " + noteId + "): " + e.getMessage(), e);
            Flash.error(request, "Database error while loading the note.");
            redirect(request, response, "/dashboard");
            return;
        }
        if (note == null) {
            Flash.error(request, "Note not found.");
            redirect(request, response, "/dashboard");
            return;
        }

        StudyKit.SummaryBundle bundle = null;
        boolean needsGeneration = note.getSummaryJson() == null || note.getSummaryJson().isBlank();
        if (!needsGeneration) {
            try {
                bundle = gson.fromJson(note.getSummaryJson(), StudyKit.SummaryBundle.class);
            } catch (Exception e) {
                needsGeneration = true;
            }
        }

        int flashcardCount = 0;
        int mcqCount = 0;
        try {
            flashcardCount = flashcardDAO.countByNote(noteId);
            mcqCount = mcqDAO.countByNote(noteId);
        } catch (SQLException ignored) {
        }

        if (!needsGeneration && (flashcardCount == 0 || mcqCount == 0)) {
            needsGeneration = true;
        }

        request.setAttribute("note", note);
        request.setAttribute("bundle", bundle);
        request.setAttribute("flashcardCount", flashcardCount);
        request.setAttribute("mcqCount", mcqCount);
        request.setAttribute("needsGeneration", needsGeneration);
        render(request, response, "summary.jsp");
    }
}

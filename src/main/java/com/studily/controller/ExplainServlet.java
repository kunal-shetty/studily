package com.studily.controller;

import com.studily.dao.MCQDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.MCQ;
import com.studily.model.Note;
import com.studily.model.User;
import com.studily.service.AIService;
import com.studily.util.Flash;
import com.studily.util.Validators;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * "Explain my mistake" — AI analyzes why THIS student fell for the option
 * they chose. POST-only (costs an API call).
 */
@WebServlet(name = "explainServlet", urlPatterns = {"/explain"})
public class ExplainServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final MCQDAO mcqDAO = new MCQDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));
        Integer mcqId = Validators.parseIntOrNull(request.getParameter("mcqId"));
        String chosen = request.getParameter("chosen");

        if (noteId == null || mcqId == null) {
            Flash.error(request, "Invalid explanation request.");
            redirect(request, response, "/dashboard");
            return;
        }

        try {
            Note note = notesDAO.findByIdAndUser(noteId, user.getUserId());
            MCQ mcq = mcqDAO.findById(mcqId);
            if (note == null || mcq == null || mcq.getNoteId() != noteId) {
                Flash.error(request, "Question not found.");
                redirect(request, response, "/summary?noteId=" + noteId);
                return;
            }
            String chosenText = mcq.getOptionByLetter(chosen);
            String explanation = AIService.explainMistake(
                    note.getTitle(),
                    note.getExtractedText() == null ? "" : note.getExtractedText(),
                    mcq.getQuestion(),
                    chosen == null ? "?" : chosen.toUpperCase(),
                    chosenText == null ? "(no answer)" : chosenText,
                    mcq.getCorrectAnswer(),
                    mcq.getOptionByLetter(mcq.getCorrectAnswer()),
                    mcq.getExplanation());
            request.setAttribute("personalExplanation", explanation);
            request.setAttribute("explainedMcq", mcq);
            request.setAttribute("chosenLetter", chosen == null ? "?" : chosen.toUpperCase());
            request.setAttribute("note", note);
            render(request, response, "explain.jsp");
        } catch (AIService.AIServiceException e) {
            Flash.error(request, e.getMessage());
            redirect(request, response, "/quiz-result-inline?noteId=" + noteId);
        } catch (SQLException e) {
            Flash.error(request, "Could not load the question.");
            redirect(request, response, "/summary?noteId=" + noteId);
        }
    }
}

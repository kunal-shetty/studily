package com.studily.controller;

import com.studily.dao.NotesDAO;
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

import java.io.IOException;
import java.sql.SQLException;

/**
 * AI mind map: generates once per note, caches in notes.mindmap_json.
 */
@WebServlet(name = "mindMapServlet", urlPatterns = {"/mindmap"})
public class MindMapServlet extends BaseAppServlet {

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

        try {
            Note note = notesDAO.findByIdAndUser(noteId, user.getUserId());
            if (note == null) {
                Flash.error(request, "Note not found.");
                redirect(request, response, "/dashboard");
                return;
            }
            if (note.getSummaryJson() == null || note.getSummaryJson().isBlank()) {
                Flash.info(request, "Generate the study material first, then the mind map.");
                redirect(request, response, "/summary?noteId=" + noteId);
                return;
            }
            if (note.getMindmapJson() == null || note.getMindmapJson().isBlank()) {
                try {
                    String raw = AIService.generateMindMap(note.getExtractedText() == null ? "" : note.getExtractedText());
                    String json = raw.trim();
                    int start = json.indexOf('{');
                    int end = json.lastIndexOf('}');
                    if (start >= 0 && end > start) json = json.substring(start, end + 1);
                    notesDAO.updateMindmap(noteId, json);
                    note.setMindmapJson(json);
                } catch (AIService.AIServiceException e) {
                    Flash.error(request, e.getMessage());
                    redirect(request, response, "/summary?noteId=" + noteId);
                    return;
                }
            }
            request.setAttribute("note", note);
            request.setAttribute("mindmapJson", note.getMindmapJson());
            render(request, response, "mindmap.jsp");
        } catch (SQLException e) {
            Log.severe("Mindmap failed (note " + noteId + "): " + e.getMessage(), e);
            Flash.error(request, "Could not load the mind map.");
            redirect(request, response, "/dashboard");
        }
    }
}

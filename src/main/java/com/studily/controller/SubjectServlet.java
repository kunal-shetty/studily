package com.studily.controller;

import com.studily.dao.NotesDAO;
import com.studily.dao.ShareDAO;
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
 * Subject folders: create a folder and assign notes to it. Kept minimal —
 * creation + assignment happen from the history page.
 */
@WebServlet(name = "subjectServlet", urlPatterns = {"/subjects"})
public class SubjectServlet extends BaseAppServlet {

    private final ShareDAO shareDAO = new ShareDAO();
    private final NotesDAO notesDAO = new NotesDAO();

    /** Create a subject folder. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String action = request.getParameter("action");

        try {
            if ("create".equals(action)) {
                String name = Validators.sanitizeText(request.getParameter("name"));
                if (name == null || name.length() < 2 || name.length() > 80) {
                    Flash.error(request, "Folder name must be 2-80 characters.");
                } else {
                    shareDAO.createSubject(user.getUserId(), name.trim());
                    Flash.success(request, "Folder \"" + name.trim() + "\" created.");
                }
            } else if ("assign".equals(action)) {
                Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));
                Integer subjectId = Validators.parseIntOrNull(request.getParameter("subjectId"));
                if (noteId == null) {
                    Flash.error(request, "Invalid note reference.");
                } else {
                    notesDAO.setSubject(noteId, user.getUserId(), subjectId); // null clears
                    Flash.success(request, subjectId == null ? "Removed from folder." : "Moved to folder.");
                }
            } else {
                Flash.error(request, "Unknown action.");
            }
        } catch (SQLException e) {
            Log.severe("Subject action failed (user " + user.getUserId() + "): " + e.getMessage(), e);
            Flash.error(request, "Database error. Please retry.");
        }
        redirect(request, response, "/history");
    }
}

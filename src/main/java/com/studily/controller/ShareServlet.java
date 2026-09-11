package com.studily.controller;

import com.google.gson.Gson;
import com.studily.dao.ShareDAO;
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
import java.util.Map;

/**
 * Share notes: owner POSTs to mint a view-only link; anyone with the link
 * gets a read-only summary page (token is the credential).
 */
@WebServlet(name = "shareServlet", urlPatterns = {"/share", "/s/*"})
public class ShareServlet extends BaseAppServlet {

    private final ShareDAO shareDAO = new ShareDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));
        if (noteId == null) {
            Flash.error(request, "Invalid note reference.");
            redirect(request, response, "/dashboard");
            return;
        }
        try {
            String token = shareDAO.createShare(noteId, user.getUserId());
            String link = request.getRequestURL().toString().replace("/share", "/s/") + token;
            Flash.success(request, "Share link ready: " + link);
            redirect(request, response, "/summary?noteId=" + noteId);
        } catch (Exception e) {
            Log.severe("Share creation failed: " + e.getMessage(), e);
            Flash.error(request, "Could not create the share link.");
            redirect(request, response, "/dashboard");
        }
    }

    /** Public read-only view: /s/<token>. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getPathInfo() == null ? "" : request.getPathInfo().replaceFirst("^/", "");
        if (token.isBlank()) {
            // GET /share with no token: authed users go back to their notes.
            if (request.getSession(false) != null
                    && request.getSession().getAttribute(com.studily.filter.AuthFilter.SESSION_USER) != null) {
                response.sendRedirect(request.getContextPath() + "/history");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }
        try {
            Map<String, Object> shared = shareDAO.findSharedNote(token);
            if (shared == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String summaryJson = (String) shared.get("summaryJson");
            StudyKit.SummaryBundle bundle = null;
            if (summaryJson != null && !summaryJson.isBlank()) {
                try {
                    bundle = gson.fromJson(summaryJson, StudyKit.SummaryBundle.class);
                } catch (Exception ignored) {
                }
            }
            request.setAttribute("sharedTitle", shared.get("title"));
            request.setAttribute("sharedOwner", shared.get("owner"));
            request.setAttribute("sharedBundle", bundle);
            request.getRequestDispatcher("/WEB-INF/views/share-view.jsp").forward(request, response);
        } catch (Exception e) {
            Log.severe("Share view failed: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

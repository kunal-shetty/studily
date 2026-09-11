package com.studily.controller;

import com.studily.dao.SettingsDAO;
import com.studily.model.User;
import com.studily.model.UserSettings;
import com.studily.util.Flash;
import com.studily.util.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Settings page: appearance (dark/light, accent), AI model, notifications.
 */
@WebServlet(name = "settingsServlet", urlPatterns = {"/settings"})
public class SettingsServlet extends BaseAppServlet {

    private final SettingsDAO settingsDAO = new SettingsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        try {
            request.setAttribute("settings", settingsDAO.getOrCreate(user.getUserId()));
        } catch (Exception e) {
            Log.severe("Settings load failed: " + e.getMessage(), e);
            request.setAttribute("settings", new UserSettings());
        }
        request.setAttribute("navActive", "settings");
        render(request, response, "settings.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        try {
            UserSettings s = settingsDAO.getOrCreate(user.getUserId());
            s.setTheme(request.getParameter("theme"));
            s.setAccent(request.getParameter("accent"));
            s.setAiModel(request.getParameter("aiModel"));
            s.setNotifications(request.getParameter("notifications") != null);
            settingsDAO.update(s);
            Flash.success(request, "Settings saved.");
        } catch (Exception e) {
            Log.severe("Settings save failed: " + e.getMessage(), e);
            Flash.error(request, "Could not save settings.");
        }
        redirect(request, response, "/settings");
    }
}

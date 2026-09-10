package com.studily.controller;

import com.studily.model.DashboardStats;
import com.studily.model.User;
import com.studily.service.DashboardService;
import com.studily.util.Flash;
import com.studily.util.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Personalized dashboard: all metrics come from MySQL via DAOs.
 */
@WebServlet(name = "dashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends BaseAppServlet {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        try {
            DashboardStats stats = dashboardService.buildStats(user.getUserId());
            request.setAttribute("stats", stats);
            render(request, response, "dashboard.jsp");
        } catch (SQLException e) {
            Log.severe("Dashboard load failed for user " + user.getUserId() + ": " + e.getMessage(), e);
            Flash.error(request, "Could not load your dashboard data.");
            render(request, response, "error.jsp");
        }
    }
}

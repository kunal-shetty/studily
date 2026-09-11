package com.studily.controller;

import com.studily.dao.EngagementDAO;
import com.studily.model.User;
import com.studily.util.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Weekly XP leaderboard — gamified consistency, reset every Monday.
 */
@WebServlet(name = "leaderboardServlet", urlPatterns = {"/leaderboard"})
public class LeaderboardServlet extends BaseAppServlet {

    private final EngagementDAO engagementDAO = new EngagementDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        List<Map<String, Object>> board;
        try {
            board = engagementDAO.weeklyLeaderboard(25);
        } catch (Exception e) {
            Log.severe("Leaderboard load failed: " + e.getMessage(), e);
            board = List.of();
        }
        request.setAttribute("board", board);
        request.setAttribute("navActive", "leaderboard");
        render(request, response, "leaderboard.jsp");
    }
}

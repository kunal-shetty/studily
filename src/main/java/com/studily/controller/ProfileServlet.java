package com.studily.controller;

import com.studily.dao.EngagementDAO;
import com.studily.dao.QuizAnswerDAO;
import com.studily.service.DashboardService;
import com.studily.model.DashboardStats;
import com.studily.model.ProfileData;
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
 * GitHub-style profile: stats, badges, weekly leaderboard position.
 */
@WebServlet(name = "profileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends BaseAppServlet {

    private final DashboardService dashboardService = new DashboardService();
    private final EngagementDAO engagementDAO = new EngagementDAO();
    private final QuizAnswerDAO quizAnswerDAO = new QuizAnswerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        ProfileData p = new ProfileData();
        try {
            p.setName(user.getName());
            p.setEmail(user.getEmail());
            p.setMemberSince(user.getCreatedAt());

            DashboardStats stats = dashboardService.buildStats(user.getUserId());
            p.setStats(stats);
            p.setBadges(engagementDAO.badgesFor(user.getUserId()));

            double accuracy = quizAnswerDAO.accuracyByUser(user.getUserId());
            if (accuracy <= 0) accuracy = stats.getAverageScore();
            engagementDAO.syncBadges(user.getUserId(), stats.getTotalNotes(),
                    stats.getTotalFlashcards(), accuracy, stats.getStudyStreak(), false);
            p.setBadges(engagementDAO.badgesFor(user.getUserId()));

            List<Map<String, Object>> board = engagementDAO.weeklyLeaderboard(10);
            p.setLeaderboard(board);
            for (Map<String, Object> row : board) {
                if (user.getUserId() == (Integer) row.get("userId")) {
                    p.setWeeklyRank((Integer) row.get("rank"));
                    p.setWeeklyXp(((Number) row.get("xp")).intValue());
                }
            }
        } catch (Exception e) {
            Log.severe("Profile load failed for user " + user.getUserId() + ": " + e.getMessage(), e);
        }
        request.setAttribute("profile", p);
        request.setAttribute("navActive", "profile");
        render(request, response, "profile.jsp");
    }
}

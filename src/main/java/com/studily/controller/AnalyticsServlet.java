package com.studily.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.studily.dao.QuizAnswerDAO;
import com.studily.dao.QuizDAO;
import com.studily.dao.ReviewDAO;
import com.studily.model.AnalyticsData;
import com.studily.model.QuizAnswer;
import com.studily.model.User;
import com.studily.service.AIService;
import com.studily.util.Flash;
import com.studily.util.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Analytics: study heatmap, quiz accuracy trend, and AI weak-topic detection.
 */
@WebServlet(name = "analyticsServlet", urlPatterns = {"/analytics"})
public class AnalyticsServlet extends BaseAppServlet {

    private final QuizDAO quizDAO = new QuizDAO();
    private final QuizAnswerDAO quizAnswerDAO = new QuizAnswerDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        AnalyticsData data = new AnalyticsData();

        try {
            Map<String, Integer> heat = new LinkedHashMap<>();
            for (int i = 119; i >= 0; i--) heat.put(LocalDate.now().minusDays(i).toString(), 0);
            quizDAO.attemptActivity(user.getUserId(), 120)
                    .forEach((k, v) -> heat.merge(k, v, Integer::sum));
            reviewDAO.reviewActivity(user.getUserId(), 120)
                    .forEach((k, v) -> heat.merge(k, v, Integer::sum));
            data.setHeatmap(heat);

            data.setQuizTrend(quizDAO.findTrendByUser(user.getUserId(), 50));
            data.setTotalReviews(reviewDAO.countTotalReviews(user.getUserId()));
            data.setDueCount(reviewDAO.countDue(user.getUserId()));
        } catch (SQLException e) {
            Log.severe("Analytics load failed for user " + user.getUserId() + ": " + e.getMessage(), e);
            Flash.error(request, "Could not load some analytics data.");
        }

        // --- AI weak-topic detection (fail-soft: analytics page still renders) ---
        try {
            List<QuizAnswer> wrong = quizAnswerDAO.findRecentWrong(user.getUserId(), 25);
                if (wrong.isEmpty()) {
                    data.setWeakTopics(List.of());
                } else {
                    StringBuilder digest = new StringBuilder();
                    for (QuizAnswer a : wrong) {
                        digest.append(clip(a.getNoteQuestion(), 200)).append(" | chose ")
                              .append(a.getChosenAnswer() == null ? "nothing" : a.getChosenAnswer())
                              .append("\n");
                    }
                    String raw = AIService.detectWeakTopics(digest.toString());
                    data.setWeakTopics(parseWeakTopics(raw));
                }
            } catch (AIService.AIServiceException | SQLException e) {
                Log.warning("Weak-topic detection unavailable: " + e.getMessage());
                data.setWeakTopics(List.of());
            }

        request.setAttribute("analytics", data);
        request.setAttribute("navActive", "analytics");
        render(request, response, "analytics.jsp");
    }

    private List<AnalyticsData.WeakTopic> parseWeakTopics(String raw) {
        List<AnalyticsData.WeakTopic> out = new ArrayList<>();
        try {
            String json = raw.trim();
            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            if (start >= 0 && end > start) json = json.substring(start, end + 1);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                AnalyticsData.WeakTopic t = new AnalyticsData.WeakTopic();
                t.setTopic(o.has("topic") ? o.get("topic").getAsString() : "Unknown topic");
                t.setReason(o.has("reason") ? o.get("reason").getAsString() : "");
                t.setNoteId(o.has("noteId") && o.get("noteId").isJsonPrimitive()
                        ? o.get("noteId").getAsInt() : 0);
                out.add(t);
                if (out.size() >= 4) break;
            }
        } catch (Exception e) {
            Log.warning("Weak-topic parse failed: " + e.getMessage());
        }
        return out;
    }

    private String clip(String s, int max) {
        return s == null ? "" : (s.length() <= max ? s : s.substring(0, max));
    }
}

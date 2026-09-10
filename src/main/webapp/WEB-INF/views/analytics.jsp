<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.AnalyticsData" %>
<%@ page import="com.studily.model.QuizResult" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    AnalyticsData analytics = (AnalyticsData) request.getAttribute("analytics");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
%>
<div class="container">
    <div class="page-head rise">
        <h1>📊 Analytics</h1>
        <p>Your consistency, accuracy, and the topics that need work.</p>
    </div>

    <div class="stats-grid">
        <div class="stat-card rise rise-1">
            <span class="stat-icon">✅</span>
            <div class="stat-value"><%= analytics.getTotalReviews() %></div>
            <div class="stat-label">Flashcards reviewed</div>
        </div>
        <div class="stat-card rise rise-2">
            <span class="stat-icon">📥</span>
            <div class="stat-value"><%= analytics.getDueCount() %></div>
            <div class="stat-label">Cards due now</div>
        </div>
        <div class="stat-card rise rise-3">
            <span class="stat-icon">🎯</span>
            <div class="stat-value"><%= analytics.getQuizTrend().isEmpty() ? "—" :
                    String.format("%.0f", analytics.getQuizTrend().get(analytics.getQuizTrend().size() - 1).getPercentage()) %>%</div>
            <div class="stat-label">Latest quiz accuracy</div>
        </div>
    </div>

    <div class="card rise rise-2 mb-3">
        <div class="card-title"><span class="icon">🔥</span><h3>Study Heatmap <span class="text-dim" style="font-size:0.8rem; font-weight:400">(last 120 days)</span></h3></div>
        <div class="heatmap" id="heatmap"></div>
    </div>

    <div class="grid-2">
        <div class="card rise rise-3">
            <div class="card-title"><span class="icon">📈</span><h3>Quiz Accuracy Trend</h3></div>
            <% if (analytics.getQuizTrend().isEmpty()) { %>
            <div class="empty-state" style="padding:26px">
                <div class="empty-icon">🧠</div>
                <h3>No quizzes yet</h3>
                <p>Take a quiz to start tracking accuracy.</p>
            </div>
            <% } else { %>
            <canvas id="trendChart" height="180"></canvas>
            <% } %>
        </div>

        <div class="card rise rise-4">
            <div class="card-title"><span class="icon">⚠️</span><h3>Weak Topics <span class="text-dim" style="font-size:0.8rem; font-weight:400">AI-detected</span></h3></div>
            <% if (analytics.getWeakTopics().isEmpty()) { %>
            <div class="empty-state" style="padding:26px">
                <div class="empty-icon">💪</div>
                <h3>No weak spots detected</h3>
                <p>Take more quizzes — the AI flags topics from wrong answers.</p>
            </div>
            <% } else { %>
                <% for (AnalyticsData.WeakTopic t : analytics.getWeakTopics()) { %>
            <div class="weak-topic">
                <span style="font-size:1.1rem">⚠️</span>
                <div>
                    <div class="wt-topic"><%= t.getTopic() %></div>
                    <div class="wt-reason"><%= t.getReason() %></div>
                </div>
            </div>
                <% } %>
            <% } %>
        </div>
    </div>
</div>

<script type="application/json" id="heatmap-data">{
<%
    StringBuilder heatJson = new StringBuilder();
    int i = 0;
    for (java.util.Map.Entry<String, Integer> e : analytics.getHeatmap().entrySet()) {
        heatJson.append("\"").append(e.getKey()).append("\":").append(e.getValue());
        if (++i < analytics.getHeatmap().size()) heatJson.append(",");
    }
    out.print(heatJson.toString());
%>
}
</script>
<% if (!analytics.getQuizTrend().isEmpty()) { %>
<script type="application/json" id="trend-data">[
<%
    StringBuilder trendJson = new StringBuilder();
    int j = 0;
    for (QuizResult q : analytics.getQuizTrend()) {
        trendJson.append("{\"date\":\"").append(q.getAttemptDate() == null ? "" : q.getAttemptDate().format(fmt))
                 .append("\",\"pct\":").append(String.format("%.1f", q.getPercentage())).append("}");
        if (++j < analytics.getQuizTrend().size()) trendJson.append(",");
    }
    out.print(trendJson.toString());
%>
]
</script>
<% } %>
<%@ include file="partials/footer.jsp" %>

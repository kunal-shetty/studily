<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.ProfileData" %>
<%@ page import="com.studily.model.DashboardStats" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Map" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    ProfileData profile = (ProfileData) request.getAttribute("profile");
    DashboardStats stats = profile.getStats();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM yyyy");
    String profInitials = profile.getName() == null || profile.getName().isBlank() ? "?"
            : profile.getName().trim().substring(0, 1).toUpperCase();
%>
<div class="container">
    <div class="profile-hero card rise">
        <span class="profile-avatar"><%= profInitials %></span>
        <div class="profile-id">
            <h1><%= profile.getName() %></h1>
            <p class="text-dim"><%= profile.getEmail() %> · member since
                <%= profile.getMemberSince() == null ? "—" : profile.getMemberSince().format(fmt) %></p>
        </div>
        <div class="profile-streak">
            <div class="stat-value">🔥 <%= stats.getStudyStreak() %></div>
            <div class="stat-label">day streak</div>
        </div>
    </div>

    <div class="stats-grid mt-3">
        <div class="stat-card rise rise-1">
            <span class="stat-icon">📚</span>
            <div class="stat-value"><%= stats.getTotalNotes() %></div>
            <div class="stat-label">Notes uploaded</div>
        </div>
        <div class="stat-card rise rise-2">
            <span class="stat-icon">🃏</span>
            <div class="stat-value"><%= stats.getTotalFlashcards() %></div>
            <div class="stat-label">Flashcards</div>
        </div>
        <div class="stat-card rise rise-3">
            <span class="stat-icon">🎯</span>
            <div class="stat-value"><%= String.format("%.0f", stats.getAverageScore()) %>%</div>
            <div class="stat-label">Avg accuracy</div>
        </div>
        <div class="stat-card rise rise-4">
            <span class="stat-icon">⚡</span>
            <div class="stat-value"><%= profile.getWeeklyXp() %></div>
            <div class="stat-label">XP this week <%= profile.getWeeklyRank() > 0 ? "(#" + profile.getWeeklyRank() + ")" : "" %></div>
        </div>
    </div>

    <div class="card mt-3 rise rise-2">
        <div class="card-title"><span class="icon">🏅</span><h3>Badges</h3></div>
        <div class="badge-grid">
            <%
                boolean anyBadge = false;
                for (Map.Entry<String, String[]> b : ProfileData.BADGE_META.entrySet()) {
                    boolean earned = profile.getBadges() != null && profile.getBadges().contains(b.getKey());
                    if (earned) anyBadge = true;
            %>
            <div class="badge-tile <%= earned ? "earned" : "locked" %>">
                <span class="badge-emoji"><%= b.getValue()[0] %></span>
                <span class="badge-name"><%= b.getValue()[1] %></span>
                <span class="badge-desc"><%= earned ? b.getValue()[2] : "Locked" %></span>
            </div>
            <% } %>
        </div>
        <% if (!anyBadge) { %>
        <p class="text-dim mt-2">Earn your first badge — complete a quiz or hit a study streak!</p>
        <% } %>
    </div>

    <div class="card mt-3 rise rise-3">
        <div class="card-title">
            <span class="icon">🏆</span>
            <h3>Weekly Leaderboard</h3>
            <a class="btn btn-ghost btn-sm" style="margin-left:auto" href="<%= ctx %>/leaderboard">Full board →</a>
        </div>
        <%
            java.util.List<Map<String, Object>> board = profile.getLeaderboard();
            if (board == null || board.isEmpty()) {
        %>
        <p class="text-dim">No XP earned this week yet. Take a quiz or finish a study session!</p>
        <% } else { %>
            <% for (Map<String, Object> row : board) { %>
        <div class="list-row <%= ((Integer) row.get("userId")) == navUser.getUserId() ? "highlight" : "" %>">
            <div class="list-main">
                <div class="list-title">
                    <%= row.get("rank").equals(1) ? "🥇" : row.get("rank").equals(2) ? "🥈" : row.get("rank").equals(3) ? "🥉" : "#" + row.get("rank") %>
                    <%= row.get("name") %>
                </div>
            </div>
            <div class="list-actions">
                <span class="badge"><%= row.get("xp") %> XP · <%= row.get("days") %>d active</span>
            </div>
        </div>
            <% } %>
        <% } %>
    </div>
</div>
<%@ include file="partials/footer.jsp" %>

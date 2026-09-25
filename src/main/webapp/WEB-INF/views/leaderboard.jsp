<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> board = (List<Map<String, Object>>) request.getAttribute("board");
%>
<div class="container-narrow">
    <div class="page-head rise">
        <h1>Weekly Leaderboard</h1>
        <p>XP earned since Monday — quizzes, reviews, and study sessions all count. Resets every week.</p>
    </div>

    <div class="card rise rise-1">
        <% if (board.isEmpty()) { %>
        <div class="empty-state" style="padding:30px">
            <div class="empty-icon"><svg class="i"><use href="#i-zap"/></svg></div>
            <h3>No XP earned yet this week</h3>
            <p>Finish a quiz or an AI Study Session to get on the board.</p>
            <a class="btn btn-primary mt-2" href="<%= ctx %>/session">Start a Study Session</a>
        </div>
        <% } else { %>
            <% for (Map<String, Object> row : board) {
                   int rank = (Integer) row.get("rank");
            %>
        <div class="list-row <%= ((Integer) row.get("userId")) == navUser.getUserId() ? "highlight" : "" %>">
            <div class="list-main">
                <div class="list-title">
                    <span class="rank-medal <%= rank <= 3 ? "rank-" + rank : "" %>"><% if (rank <= 3) { %><svg class="i"><use href="#i-award"/></svg><% } else { %>#<%= rank %><% } %></span>
                    <%= row.get("name") %>
                </div>
            </div>
            <div class="list-actions">
                <span class="badge"><%= row.get("xp") %> XP</span>
                <span class="badge <%= (Integer) row.get("days") > 0 ? "" : "medium" %>"><%= row.get("days") %>d active</span>
            </div>
        </div>
            <% } %>
        <% } %>
    </div>
</div>
<%@ include file="partials/footer.jsp" %>

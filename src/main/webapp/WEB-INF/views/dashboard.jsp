<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.DashboardStats" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.QuizResult" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    DashboardStats stats = (DashboardStats) request.getAttribute("stats");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
    DateTimeFormatter fmtTime = DateTimeFormatter.ofPattern("MMM d, HH:mm");
%>
<div class="container">
    <div class="page-head rise">
        <h1>Welcome back, <%= navUser.getName() %> 👋</h1>
        <p>Here's your study progress — every number below is live from your account.</p>
    </div>

    <div class="stats-grid">
        <div class="stat-card rise rise-1">
            <span class="stat-icon">📚</span>
            <div class="stat-value"><%= stats.getTotalNotes() %></div>
            <div class="stat-label">Notes Uploaded</div>
        </div>
        <div class="stat-card rise rise-2">
            <span class="stat-icon">🃏</span>
            <div class="stat-value"><%= stats.getTotalFlashcards() %></div>
            <div class="stat-label">Flashcards Created</div>
        </div>
        <div class="stat-card rise rise-3">
            <span class="stat-icon">🎯</span>
            <div class="stat-value"><%= String.format("%.0f", stats.getAverageScore()) %>%</div>
            <div class="stat-label">Average Quiz Score</div>
        </div>
        <div class="stat-card rise rise-4">
            <span class="stat-icon">🔥</span>
            <div class="stat-value"><%= stats.getStudyStreak() %> <span style="font-size:1rem">day<%= stats.getStudyStreak() == 1 ? "" : "s" %></span></div>
            <div class="stat-label">Study Streak · <%= stats.getQuizzesCompleted() %> quiz<%= stats.getQuizzesCompleted() == 1 ? "" : "zes" %> done</div>
        </div>
    </div>

    <div class="grid-2">
        <div class="card rise rise-2">
            <div class="card-title">
                <span class="icon">📄</span>
                <h3>Recent Uploads</h3>
            </div>
            <%
                List<Note> notes = stats.getRecentNotes();
                if (notes == null || notes.isEmpty()) {
            %>
            <div class="empty-state" style="padding: 26px">
                <div class="empty-icon">📥</div>
                <h3>No notes yet</h3>
                <p>Upload your first PDF to generate study material.</p>
                <a class="btn btn-primary mt-2" href="<%= ctx %>/upload">Upload Notes</a>
            </div>
            <% } else { %>
                <% for (Note n : notes) { %>
            <div class="list-row">
                <div class="list-main">
                    <div class="list-title"><%= n.getTitle() %></div>
                    <div class="list-sub"><%= n.getCreatedAt() == null ? "" : n.getCreatedAt().format(fmt) %></div>
                </div>
                <div class="list-actions">
                    <a class="btn btn-secondary btn-sm" href="<%= ctx %>/summary?noteId=<%= n.getNoteId() %>">Open</a>
                </div>
            </div>
            <% } %>
            <a class="btn btn-ghost btn-sm mt-2" href="<%= ctx %>/history">View all →</a>
            <% } %>
        </div>

        <div class="card rise rise-3">
            <div class="card-title">
                <span class="icon">🏆</span>
                <h3>Recent Quizzes</h3>
            </div>
            <%
                List<QuizResult> quizzes = stats.getRecentQuizzes();
                if (quizzes == null || quizzes.isEmpty()) {
            %>
            <div class="empty-state" style="padding: 26px">
                <div class="empty-icon">🧠</div>
                <h3>No quizzes yet</h3>
                <p>Test yourself with AI-generated MCQs from your notes.</p>
            </div>
            <% } else { %>
                <% for (QuizResult q : quizzes) { %>
            <div class="list-row">
                <div class="list-main">
                    <div class="list-title"><%= q.getNoteTitle() %></div>
                    <div class="list-sub"><%= q.getAttemptDate() == null ? "" : q.getAttemptDate().format(fmtTime) %></div>
                </div>
                <div class="list-actions">
                    <span class="badge <%= q.getPercentage() >= 70 ? "" : "medium" %>"><%= q.getScore() %>/<%= q.getTotalQuestions() %></span>
                </div>
            </div>
            <% } %>
            <a class="btn btn-ghost btn-sm mt-2" href="<%= ctx %>/history">View all →</a>
            <% } %>
        </div>
    </div>

    <div class="card mt-3 rise rise-4">
        <div class="card-title">
            <span class="icon">⚡</span>
            <h3>Quick Actions</h3>
        </div>
        <div style="display:flex; gap:12px; flex-wrap:wrap">
            <a class="btn btn-primary" href="<%= ctx %>/upload">📎 Upload New Notes</a>
            <a class="btn btn-secondary" href="<%= ctx %>/history">🕘 Study History</a>
        </div>
    </div>
</div>
<%@ include file="partials/footer.jsp" %>

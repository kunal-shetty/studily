<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.QuizResult" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    request.setAttribute("navActive", "history");
    @SuppressWarnings("unchecked")
    List<Note> notes = (List<Note>) request.getAttribute("notes");
    @SuppressWarnings("unchecked")
    List<QuizResult> attempts = (List<QuizResult>) request.getAttribute("attempts");
    Map<Integer, String> subjects = (Map<Integer, String>) request.getAttribute("subjects");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm");
%>
<div class="container">
    <div class="page-head rise">
        <h1>Study History</h1>
        <p>Every note you've uploaded and every quiz you've taken.</p>
    </div>

    <div class="card rise rise-1 mb-3">
        <div class="card-title">
            <span class="icon">📚</span>
            <h3>Your Notes (<%= notes.size() %>)</h3>
            <form method="post" action="<%= ctx %>/subjects" style="margin-left:auto; display:flex; gap:8px">
                <input type="hidden" name="action" value="create">
                <input class="input input-sm" type="text" name="name" placeholder="New folder name…" maxlength="80" required>
                <button type="submit" class="btn btn-secondary btn-sm">+ Folder</button>
            </form>
        </div>
        <% if (notes.isEmpty()) { %>
        <div class="empty-state" style="padding:30px">
            <div class="empty-icon">📥</div>
            <h3>Nothing here yet</h3>
            <p>Upload your first PDF to get started.</p>
            <a class="btn btn-primary mt-2" href="<%= ctx %>/upload">Upload Notes</a>
        </div>
        <% } else { %>
        <% for (Note n : notes) { %>
        <div class="list-row">
            <div class="list-main">
                <div class="list-title">
                    <%= n.isBookmarked() ? "⭐ " : "" %><%= n.getTitle() %>
                    <% if (n.getSubjectId() != null && subjects.containsKey(n.getSubjectId())) { %>
                    <span class="badge medium">📁 <%= subjects.get(n.getSubjectId()) %></span>
                    <% } %>
                </div>
                <div class="list-sub"><%= n.getCreatedAt() == null ? "" : n.getCreatedAt().format(fmt) %>
                    · <%= n.getPdfPath() != null ? "PDF" : "Text" %></div>
            </div>
            <div class="list-actions">
                <form method="post" action="<%= ctx %>/note-edit">
                    <input type="hidden" name="action" value="bookmark">
                    <input type="hidden" name="noteId" value="<%= n.getNoteId() %>">
                    <input type="hidden" name="value" value="<%= n.isBookmarked() %>">
                    <button type="submit" class="btn btn-ghost btn-sm" title="Bookmark"><%= n.isBookmarked() ? "⭐" : "☆" %></button>
                </form>
                <a class="btn btn-secondary btn-sm" href="<%= ctx %>/summary?noteId=<%= n.getNoteId() %>">Summary</a>
                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/chat?noteId=<%= n.getNoteId() %>">Chat</a>
                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/flashcards?noteId=<%= n.getNoteId() %>">Cards</a>
                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/quiz?noteId=<%= n.getNoteId() %>">Quiz</a>
                <form method="post" action="<%= ctx %>/note-edit" data-confirm="Delete “<%= n.getTitle() %>” and all its flashcards, MCQs, and quiz history?">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="noteId" value="<%= n.getNoteId() %>">
                    <button type="submit" class="btn btn-ghost btn-sm btn-icon-danger" title="Delete">🗑</button>
                </form>
            </div>
        </div>
        <% } %>
        <% } %>
    </div>

    <div class="card rise rise-2">
        <div class="card-title">
            <span class="icon">🏆</span>
            <h3>Quiz Attempts (<%= attempts.size() %>)</h3>
        </div>
        <% if (attempts.isEmpty()) { %>
        <div class="empty-state" style="padding:30px">
            <div class="empty-icon">🧠</div>
            <h3>No attempts yet</h3>
            <p>Take a quiz from any note's summary page.</p>
        </div>
        <% } else { %>
        <% for (QuizResult q : attempts) { %>
        <div class="list-row">
            <div class="list-main">
                <div class="list-title"><%= q.getNoteTitle() %></div>
                <div class="list-sub"><%= q.getAttemptDate() == null ? "" : q.getAttemptDate().format(fmt) %></div>
            </div>
            <div class="list-actions">
                <span class="badge <%= q.getPercentage() >= 70 ? "" : "medium" %>"><%= q.getScore() %>/<%= q.getTotalQuestions() %> · <%= String.format("%.0f", q.getPercentage()) %>%</span>
            </div>
        </div>
        <% } %>
        <% } %>
    </div>
</div>
<%@ include file="partials/footer.jsp" %>

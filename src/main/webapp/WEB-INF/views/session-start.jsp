<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<Note> notes = (List<Note>) request.getAttribute("notes");
    java.util.List<Note> ready = new java.util.ArrayList<>();
    for (Note n : notes) {
        if (n.getSummaryJson() != null && !n.getSummaryJson().isBlank()) ready.add(n);
    }
%>
<div class="container-narrow">
    <div class="page-head rise">
        <h1>⚡ AI Study Session</h1>
        <p>One click. The AI plans 20 focused minutes: summary → flashcards → quiz. Streak and XP update automatically.</p>
    </div>

    <% if (ready.isEmpty()) { %>
    <div class="card rise rise-1">
        <div class="empty-state">
            <div class="empty-icon">🚀</div>
            <h3>Nothing to study yet</h3>
            <p>Upload a note and generate its study material — then a full AI-guided session unlocks here.</p>
            <a class="btn btn-primary mt-2" href="<%= ctx %>/upload">Upload your first note</a>
        </div>
    </div>
    <% } else { %>
    <div class="card rise rise-1">
        <div class="card-title"><span class="icon">🎯</span><h3>What do you want to study today?</h3></div>
        <% for (Note n : ready) { %>
        <form method="post" action="<%= ctx %>/session" class="list-row session-pick">
            <input type="hidden" name="noteId" value="<%= n.getNoteId() %>">
            <div class="list-main">
                <div class="list-title"><%= n.getTitle() %></div>
                <div class="list-sub"><%= n.getCreatedAt() == null ? "" : n.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM d")) %>
                    · <%= n.getPdfPath() != null ? "PDF" : "Text" %></div>
            </div>
            <div class="list-actions">
                <button type="submit" class="btn btn-primary btn-sm">Start Session →</button>
            </div>
        </form>
        <% } %>
    </div>

    <div class="card rise rise-2 mt-3">
        <div class="card-title"><span class="icon">🗺</span><h3>How a session runs</h3></div>
        <div class="session-flow">
            <div class="flow-step"><span class="flow-num">1</span> AI builds your 20-minute plan</div>
            <div class="flow-step"><span class="flow-num">2</span> Skim the summary &amp; focus areas</div>
            <div class="flow-step"><span class="flow-num">3</span> Flip through every flashcard</div>
            <div class="flow-step"><span class="flow-num">4</span> Take the 10-question quiz (auto-timed)</div>
            <div class="flow-step"><span class="flow-num">5</span> Streak + XP + analytics update themselves</div>
        </div>
    </div>
    <% } %>
</div>
<%@ include file="partials/footer.jsp" %>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.StudyKit" %>
<%@ page import="com.studily.model.Flashcard" %>
<%@ page import="com.studily.model.MCQ" %>
<%@ page import="com.google.gson.JsonObject" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    Note note = (Note) request.getAttribute("note");
    JsonObject plan = (JsonObject) request.getAttribute("plan");
    @SuppressWarnings("unchecked")
    List<Flashcard> cards = (List<Flashcard>) request.getAttribute("cards");
    @SuppressWarnings("unchecked")
    List<MCQ> mcqs = (List<MCQ>) request.getAttribute("mcqs");

    StudyKit.SummaryBundle bundle = null;
    try {
        bundle = new com.google.gson.Gson().fromJson(note.getSummaryJson(), StudyKit.SummaryBundle.class);
    } catch (Exception ignored) {
    }
%>
<div class="container">
    <div class="page-head rise">
        <div class="flex-between">
            <div>
                <h1>⚡ Study Session — <%= note.getTitle() %></h1>
                <p>20 minutes. Three steps. The AI is your coach.</p>
            </div>
            <a class="btn btn-ghost btn-sm" href="<%= ctx %>/session">Switch note</a>
        </div>
    </div>

    <div class="card rise rise-1">
        <div class="card-title"><span class="icon">🤖</span><h3>Your AI plan</h3></div>
        <p style="color:var(--text)"><%= plan.has("intro") ? plan.get("intro").getAsString() : "" %></p>
        <% if (plan.has("focus") && plan.getAsJsonArray("focus").size() > 0) { %>
        <div class="chip-list mt-2">
            <% for (var el : plan.getAsJsonArray("focus")) { %><span class="chip">🎯 <%= el.getAsString() %></span><% } %>
        </div>
        <% } %>
        <% if (plan.has("tip")) { %>
        <div class="tip-item mt-2"><span>💡 <%= plan.get("tip").getAsString() %></span></div>
        <% } %>
    </div>

    <% if (bundle != null && bundle.getSummary() != null) { %>
    <div class="card mt-3 rise rise-2">
        <div class="card-title"><span class="icon">📝</span><h3>Step 1 — Skim the summary</h3></div>
        <p style="color:var(--text)"><%= bundle.getSummary() %></p>
        <a class="btn btn-ghost btn-sm mt-2" href="<%= ctx %>/summary?noteId=<%= note.getNoteId() %>">Full summary page →</a>
    </div>
    <% } %>

    <% if (!cards.isEmpty()) { %>
    <div class="card mt-3 rise rise-2">
        <div class="card-title"><span class="icon">🃏</span><h3>Step 2 — Flashcard pass <span class="text-dim" style="font-size:0.8rem; font-weight:400">(<%= cards.size() %> cards)</span></h3></div>
        <p class="text-dim">Do one quick pass — flip each card, say the answer out loud.</p>
        <a class="btn btn-secondary mt-2" href="<%= ctx %>/flashcards?noteId=<%= note.getNoteId() %>">Open flashcards →</a>
    </div>
    <% } %>

    <div class="card mt-3 rise rise-3">
        <div class="card-title">
            <span class="icon">🧠</span>
            <h3>Step 3 — The quiz <span class="text-dim" style="font-size:0.8rem; font-weight:400">(<%= mcqs.size() %> questions · 20:00 timer)</span></h3>
            <span class="timer" id="quiz-timer" data-deadline="<%= (Long) request.getSession().getAttribute("quiz.deadline") %>">⏱ --:--</span>
        </div>
        <p class="text-dim">Answer every question. This posts to the normal grader — score, explanations, XP, and your streak update automatically.</p>

        <form method="post" action="<%= ctx %>/quiz-submit" id="quiz-form">
            <% for (int i = 0; i < mcqs.size(); i++) {
                   MCQ m = mcqs.get(i);
            %>
            <div class="quiz-q">
                <div class="quiz-q-text"><strong>Q<%= i + 1 %>.</strong> <%= m.getQuestion() %></div>
                <div class="quiz-opts">
                    <% String[] letters = {"A", "B", "C", "D"};
                       String[] texts = {m.getOptionA(), m.getOptionB(), m.getOptionC(), m.getOptionD()};
                       for (int j = 0; j < 4; j++) { %>
                    <label class="quiz-opt">
                        <input type="radio" name="q<%= m.getId() %>" value="<%= letters[j] %>" required>
                        <span><strong><%= letters[j] %>.</strong> <%= texts[j] %></span>
                    </label>
                    <% } %>
                </div>
            </div>
            <% } %>
            <button type="submit" class="btn btn-primary btn-block mt-2">Finish Session — Submit Quiz</button>
        </form>
    </div>
</div>
<%@ include file="partials/footer.jsp" %>

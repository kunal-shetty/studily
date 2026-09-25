<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.MCQ" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    Note note = (Note) request.getAttribute("note");
    @SuppressWarnings("unchecked")
    List<MCQ> mcqs = (List<MCQ>) request.getAttribute("mcqs");
    @SuppressWarnings("unchecked")
    Map<Integer, String> chosen = (Map<Integer, String>) request.getAttribute("chosen");
    int score = (Integer) request.getAttribute("score");
    int total = (Integer) request.getAttribute("total");
    int pct = total == 0 ? 0 : (int) Math.round((score * 100.0) / total);
    String verdict = pct >= 80 ? "Outstanding!" : pct >= 60 ? "Good work!"
            : pct >= 40 ? "Keep practicing" : "Review the notes and retry";
%>
<div class="container-narrow">
    <div class="page-head rise">
        <div class="flex-between">
            <div>
                <h1>Quiz Result</h1>
                <p><%= note.getTitle() %></p>
            </div>
            <a class="btn btn-ghost btn-sm" href="<%= ctx %>/dashboard">Dashboard</a>
        </div>
    </div>

    <div class="card rise rise-1">
        <div class="result-hero">
            <div class="score-ring" style="--pct: <%= pct %>" id="score-ring">
                <span class="score-text" id="score-text">0/<%= total %></span>
            </div>
            <h2><%= verdict %></h2>
            <p>You scored <strong style="color:var(--text)"><%= score %> out of <%= total %></strong>
                (<%= pct %>%) · saved to your history.</p>
            <div class="mt-2" style="display:flex; gap:12px; justify-content:center; flex-wrap:wrap">
                <a class="btn btn-secondary" href="<%= ctx %>/quiz?noteId=<%= note.getNoteId() %>"><svg class="i"><use href="#i-refresh"/></svg> Retake Quiz</a>
                <a class="btn btn-primary" href="<%= ctx %>/flashcards?noteId=<%= note.getNoteId() %>"><svg class="i"><use href="#i-cards"/></svg> Study Flashcards</a>
            </div>
        </div>
    </div>

    <h3 class="mt-4 mb-2 rise rise-2">Review &amp; AI Explanations</h3>
    <%
        String[] letters = MCQ.LETTERS;
        for (int i = 0; i < mcqs.size(); i++) {
            MCQ m = mcqs.get(i);
            String pick = chosen.get(m.getId());
            boolean wasCorrect = m.isCorrect(pick);
            String[] options = { m.getOptionA(), m.getOptionB(), m.getOptionC(), m.getOptionD() };
    %>
    <div class="card rise" style="margin-bottom:14px">
        <div class="card-title" style="margin-bottom:10px">
            <span class="icon"><svg class="i"><use href="#i-<%= wasCorrect ? "check" : "x" %>"/></svg></span>
            <h3 style="font-size:1rem"><%= i + 1 %>. <%= m.getQuestion() %></h3>
            <span class="badge <%= m.getDifficulty() %>" style="margin-left:auto"><%= m.getDifficulty() %></span>
        </div>
        <%
            for (int j = 0; j < letters.length; j++) {
                String opt = options[j];
                if (opt == null) continue;
                boolean isCorrectOption = letters[j].equalsIgnoreCase(m.getCorrectAnswer());
                boolean isPicked = letters[j].equalsIgnoreCase(pick);
                String cls = "option";
                if (isCorrectOption) cls += " correct";
                else if (isPicked) cls += " wrong";
        %>
        <div class="<%= cls %>" style="cursor:default; margin-bottom:8px">
            <span class="letter"><%= letters[j] %></span>
            <span><%= opt %></span>
            <% if (isPicked) { %><span class="badge" style="margin-left:auto">your answer</span><% } %>
        </div>
        <% } %>
        <div class="explanation">
            <strong>AI Explanation:</strong> <%= m.getExplanation() == null || m.getExplanation().isBlank()
                    ? "The correct answer is " + m.getCorrectAnswer() + "." : m.getExplanation() %>
        </div>
        <% if (!wasCorrect) { %>
        <form method="post" action="<%= ctx %>/explain" class="mt-2">
            <input type="hidden" name="noteId" value="<%= note.getNoteId() %>">
            <input type="hidden" name="mcqId" value="<%= m.getId() %>">
            <input type="hidden" name="chosen" value="<%= pick == null ? "" : pick %>">
            <button type="submit" class="btn btn-secondary btn-sm"><svg class="i"><use href="#i-compass"/></svg> Explain my mistake</button>
        </form>
        <% } %>
    </div>
    <% } %>
</div>
<script>
    // Animate the score ring filling up.
    (function () {
        var ring = document.getElementById('score-ring');
        var text = document.getElementById('score-text');
        var target = <%= pct %>;
        var current = 0;
        var t = setInterval(function () {
            current += 2;
            if (current >= target) { current = target; clearInterval(t); }
            ring.style.setProperty('--pct', current);
            text.textContent = Math.round(current / 100 * <%= total %>) + '/<%= total %>';
        }, 18);
    })();
</script>
<%@ include file="partials/footer.jsp" %>

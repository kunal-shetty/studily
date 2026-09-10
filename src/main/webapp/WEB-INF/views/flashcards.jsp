<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.Flashcard" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    Note note = (Note) request.getAttribute("note");
    List<Flashcard> cards = (List<Flashcard>) request.getAttribute("cards");
%>
<div class="container-narrow">
    <div class="page-head rise">
        <div class="flex-between">
            <div>
                <h1>Flashcards</h1>
                <p><%= note.getTitle() %> · <%= cards.size() %> cards</p>
            </div>
            <a class="btn btn-ghost btn-sm" href="<%= ctx %>/summary?noteId=<%= note.getNoteId() %>">← Summary</a>
        </div>
    </div>

    <div class="mb-2 rise rise-1" style="display:flex; justify-content:center">
        <div class="progress-bar" style="width:100%; max-width:640px">
            <div class="fill" id="card-progress" style="width:0%"></div>
        </div>
    </div>

    <div class="flashcard-stage rise rise-2" id="flashcard-stage">
        <div class="flashcard" id="flashcard">
            <div class="flashcard-face front">
                <span class="face-label">Question</span>
                <span class="face-text" id="card-question"></span>
            </div>
            <div class="flashcard-face back">
                <span class="face-label">Answer</span>
                <span class="face-text" id="card-answer"></span>
            </div>
        </div>
    </div>

    <div class="flashcard-nav rise rise-3">
        <button class="btn btn-secondary" id="btn-prev">← Prev</button>
        <span class="flashcard-counter" id="card-counter"></span>
        <button class="btn btn-secondary" id="btn-next">Next →</button>
    </div>

    <div class="flashcard-nav rise rise-3" style="margin-top:12px">
        <button class="btn btn-ghost btn-sm" id="btn-shuffle">🔀 Shuffle</button>
        <button class="btn btn-secondary btn-sm" id="btn-known">✓ Mark Known</button>
        <span class="badge" id="known-count">0 marked</span>
    </div>

    <p class="hint" style="text-align:center; margin-top:16px">
        Tip: click the card or press Enter to flip · arrow keys navigate
    </p>
</div>

<script type="application/json" id="flashcard-data">
[
<%
    StringBuilder json = new StringBuilder();
    for (int i = 0; i < cards.size(); i++) {
        Flashcard c = cards.get(i);
        String q = c.getQuestion() == null ? "" : c.getQuestion();
        String a = c.getAnswer() == null ? "" : c.getAnswer();
        // Minimal JSON string escaping (quotes, backslashes, control chars are rare in AI output).
        q = q.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", "\\t");
        a = a.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", "\\t");
        json.append("{\"question\":\"").append(q).append("\",\"answer\":\"").append(a).append("\"}");
        if (i < cards.size() - 1) json.append(",");
    }
    out.print(json.toString());
%>
]
</script>
<%@ include file="partials/footer.jsp" %>

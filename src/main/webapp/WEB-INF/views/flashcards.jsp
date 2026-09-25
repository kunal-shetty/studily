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
            <a class="btn btn-ghost btn-sm" href="<%= ctx %>/summary?noteId=<%= note.getNoteId() %>"><svg class="i i-sm"><use href="#i-chevron-left"/></svg> Summary</a>
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
        <button class="btn btn-secondary" id="btn-prev"><svg class="i i-sm"><use href="#i-chevron-left"/></svg> Prev</button>
        <span class="flashcard-counter" id="card-counter"></span>
        <button class="btn btn-secondary" id="btn-next">Next <svg class="i i-sm"><use href="#i-chevron-right"/></svg></button>
    </div>

    <div class="flashcard-nav rise rise-3" style="margin-top:12px">
        <button class="btn btn-ghost btn-sm" id="btn-shuffle"><svg class="i"><use href="#i-shuffle"/></svg> Shuffle</button>
        <button class="btn btn-secondary btn-sm" id="btn-known"><svg class="i"><use href="#i-check"/></svg> Mark Known</button>
        <button class="btn btn-ghost btn-sm" id="btn-listen-card" title="Read this card aloud"><svg class="i"><use href="#i-volume"/></svg> Listen</button>
        <button class="btn btn-ghost btn-sm" id="btn-edit"><svg class="i"><use href="#i-pen"/></svg> Edit Card</button>
        <span class="badge" id="known-count">0 marked</span>
    </div>

    <div class="card mt-3 rise rise-3" id="card-editor" style="display:none">
        <div class="card-title"><span class="icon"><svg class="i"><use href="#i-pen"/></svg></span><h3>Edit this card</h3></div>
        <form method="post" action="<%= ctx %>/note-edit">
            <input type="hidden" name="action" value="edit-card">
            <input type="hidden" name="noteId" value="<%= note.getNoteId() %>">
            <input type="hidden" name="cardId" id="edit-card-id">
            <div class="form-group">
                <label for="edit-question">Question</label>
                <textarea class="textarea" id="edit-question" name="question" rows="2" required></textarea>
            </div>
            <div class="form-group">
                <label for="edit-answer">Answer</label>
                <textarea class="textarea" id="edit-answer" name="answer" rows="3" required></textarea>
            </div>
            <div style="display:flex; gap:10px">
                <button type="submit" class="btn btn-primary btn-sm">Save Card</button>
                <button type="button" class="btn btn-ghost btn-sm" id="edit-cancel">Cancel</button>
            </div>
        </form>
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
        json.append("{\"id\":").append(c.getId())
            .append(",\"question\":\"").append(q).append("\",\"answer\":\"").append(a).append("\"}");
        if (i < cards.size() - 1) json.append(",");
    }
    out.print(json.toString());
%>
]
</script>
<%@ include file="partials/footer.jsp" %>

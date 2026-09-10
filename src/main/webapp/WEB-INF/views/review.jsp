<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.FlashcardReview" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    List<FlashcardReview> dueCards = (List<FlashcardReview>) request.getAttribute("dueCards");
    int dueCount = (Integer) request.getAttribute("dueCount");
    int totalReviews = (Integer) request.getAttribute("totalReviews");
%>
<div class="container-narrow">
    <div class="page-head rise">
        <h1>🧠 Review Session</h1>
        <p>Spaced repetition: cards return right before you'd forget them.</p>
    </div>

    <div class="stats-grid">
        <div class="stat-card rise rise-1">
            <span class="stat-icon">📥</span>
            <div class="stat-value"><%= dueCount %></div>
            <div class="stat-label">Cards due now</div>
        </div>
        <div class="stat-card rise rise-2">
            <span class="stat-icon">✅</span>
            <div class="stat-value"><%= totalReviews %></div>
            <div class="stat-label">Total reviews done</div>
        </div>
    </div>

    <% if (dueCards.isEmpty()) { %>
    <div class="card rise" style="text-align:center; padding:48px 24px">
        <div class="empty-state">
            <div class="empty-icon">🎉</div>
            <h3>Nothing due — you're all caught up!</h3>
            <p>Cards reappear automatically as their schedules come due.</p>
            <a class="btn btn-primary mt-2" href="<%= ctx %>/history">Study new material</a>
        </div>
    </div>
    <% } else { %>

    <div class="mb-2 rise rise-1" style="display:flex; justify-content:center">
        <div class="progress-bar" style="width:100%; max-width:640px">
            <div class="fill" id="review-progress" style="width:0%"></div>
        </div>
    </div>

    <div class="flashcard-stage rise rise-2" id="review-stage">
        <div class="flashcard" id="review-card">
            <div class="flashcard-face front">
                <span class="face-label">Question · <span id="review-counter"></span></span>
                <span class="face-text" id="review-q"></span>
            </div>
            <div class="flashcard-face back">
                <span class="face-label">Answer</span>
                <span class="face-text" id="review-a"></span>
            </div>
        </div>
    </div>

    <p class="hint" style="text-align:center; margin-top:14px">Click the card to reveal the answer, then rate your recall:</p>

    <div class="rating-row rise rise-3">
        <button class="btn rating-btn again" data-rating="1">😖 Again</button>
        <button class="btn rating-btn hard" data-rating="2">😅 Hard</button>
        <button class="btn rating-btn good" data-rating="3">🙂 Good</button>
        <button class="btn rating-btn easy" data-rating="4">😎 Easy</button>
    </div>
    <% } %>
</div>

<script type="application/json" id="review-data">[
<%
    StringBuilder json = new StringBuilder();
    for (int i = 0; i < dueCards.size(); i++) {
        FlashcardReview r = dueCards.get(i);
        String q = r.getQuestion() == null ? "" : r.getQuestion();
        String a = r.getAnswer() == null ? "" : r.getAnswer();
        q = q.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", "\\t");
        a = a.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", "\\t");
        json.append("{\"cardId\":").append(r.getCardId()).append(",\"question\":\"").append(q)
            .append("\",\"answer\":\"").append(a).append("\"}");
        if (i < dueCards.size() - 1) json.append(",");
    }
    out.print(json.toString());
%>
]
</script>
<%@ include file="partials/footer.jsp" %>

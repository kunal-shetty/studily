<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.MCQ" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    Note note = (Note) request.getAttribute("note");
    MCQ mcq = (MCQ) request.getAttribute("explainedMcq");
    String explanation = (String) request.getAttribute("personalExplanation");
    String chosen = (String) request.getAttribute("chosenLetter");
%>
<div class="container-narrow">
    <div class="page-head rise">
        <h1>🧭 Why you missed it</h1>
        <p><%= note.getTitle() %></p>
    </div>

    <div class="card rise rise-1">
        <div class="card-title" style="margin-bottom:10px">
            <span class="icon">❓</span>
            <h3 style="font-size:1rem"><%= mcq.getQuestion() %></h3>
        </div>
        <p class="text-dim" style="font-size:0.9rem">
            Your answer: <span class="badge hard"><%= chosen %></span>
            &nbsp; Correct: <span class="badge easy"><%= mcq.getCorrectAnswer() %></span>
        </p>
        <div class="explanation mt-2" style="font-size:0.98rem; line-height:1.6">
            <strong>Coach:</strong> <%= explanation %>
        </div>
        <div class="mt-3" style="display:flex; gap:10px; flex-wrap:wrap">
            <a class="btn btn-secondary" href="<%= ctx %>/quiz?noteId=<%= note.getNoteId() %>">🔄 Retake quiz</a>
            <a class="btn btn-primary" href="<%= ctx %>/review">🧠 Review flashcards</a>
        </div>
    </div>
</div>
<%@ include file="partials/footer.jsp" %>

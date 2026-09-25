<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.MCQ" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    Note note = (Note) request.getAttribute("note");
    @SuppressWarnings("unchecked")
    List<MCQ> mcqs = (List<MCQ>) request.getAttribute("mcqs");
    String difficulty = (String) request.getAttribute("difficulty");
    Long deadline = (Long) request.getAttribute("quizDeadlineMs");
    if (deadline == null) {
        deadline = (Long) request.getSession().getAttribute("quiz.deadline");
    }
%>
<div class="container-narrow">
    <div class="page-head rise">
        <div class="flex-between">
            <div>
                <h1>Quiz: <%= note.getTitle() %></h1>
                <p><%= mcqs.size() %> questions
                    · <span class="badge <%= "easy".equals(difficulty) ? "easy" : "hard".equals(difficulty) ? "hard" : "medium" %>"><%= difficulty %></span></p>
            </div>
            <a class="btn btn-ghost btn-sm" href="<%= ctx %>/summary?noteId=<%= note.getNoteId() %>"><svg class="i i-sm"><use href="#i-chevron-left"/></svg> Exit Quiz</a>
        </div>
    </div>

    <form method="post" action="<%= ctx %>/quiz-submit" id="quiz-form">
        <div class="quiz-meta rise rise-1">
            <span class="timer" id="quiz-timer" data-deadline="<%= deadline == null ? 0L : deadline %>"><svg class="i i-sm"><use href="#i-clock"/></svg> --:--</span>
            <span class="badge badge-primary">Answer all questions — instant feedback at the end</span>
        </div>

        <%
            String[] letters = MCQ.LETTERS;
            for (int i = 0; i < mcqs.size(); i++) {
                MCQ m = mcqs.get(i);
                String[] options = { m.getOptionA(), m.getOptionB(), m.getOptionC(), m.getOptionD() };
        %>
        <div class="card rise" style="margin-bottom:16px">
            <div class="card-title" style="margin-bottom:12px">
                <span class="icon"><%= i + 1 %></span>
                <h3 style="font-size:1.02rem"><%= m.getQuestion() %></h3>
            </div>
            <%
                for (int j = 0; j < letters.length; j++) {
                    String opt = options[j];
                    if (opt == null) continue;
            %>
            <label class="option" data-for="q<%= m.getId() %>">
                <input type="radio" name="q<%= m.getId() %>" value="<%= letters[j] %>">
                <span class="letter"><%= letters[j] %></span>
                <span><%= opt %></span>
            </label>
            <% } %>
        </div>
        <% } %>

        <button type="submit" class="btn btn-primary btn-block rise">Submit Quiz</button>
    </form>
</div>
<%@ include file="partials/footer.jsp" %>

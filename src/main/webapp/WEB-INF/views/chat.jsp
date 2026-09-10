<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.ChatMessage" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    Note note = (Note) request.getAttribute("note");
    List<ChatMessage> history = (List<ChatMessage>) request.getAttribute("chatHistory");
%>
<div class="container-narrow" style="max-width:820px">
    <div class="page-head rise">
        <div class="flex-between">
            <div>
                <h1>💬 AI Chat</h1>
                <p>Ask anything about <strong style="color:var(--text)"><%= note.getTitle() %></strong> — answers come only from your notes.</p>
            </div>
            <a class="btn btn-ghost btn-sm" href="<%= ctx %>/summary?noteId=<%= note.getNoteId() %>">← Summary</a>
        </div>
    </div>

    <div class="card chat-wrap rise rise-1">
        <div class="chat-scroll" id="chat-scroll">
            <% if (history.isEmpty()) { %>
            <div class="chat-empty">
                <div style="font-size:2rem; margin-bottom:8px">🤖</div>
                <p>Ask your first question below — the AI answers strictly from this note.</p>
            </div>
            <div class="chat-suggest">
                <span class="chip">Explain the core concept simply</span>
                <span class="chip">Quiz me on this note</span>
                <span class="chip">What are the most exam-important parts?</span>
            </div>
            <% } else { %>
                <% for (ChatMessage m : history) { %>
            <div class="chat-bubble <%= m.getRole() %>"><%= m.getContent() %></div>
                <% } %>
            <% } %>
        </div>

        <form class="chat-form" method="post" action="<%= ctx %>/chat">
            <input type="hidden" name="noteId" value="<%= note.getNoteId() %>">
            <input class="input" type="text" name="question" required maxlength="1200"
                   placeholder="e.g. Explain deadlock like I'm 10…" autofocus>
            <button class="btn btn-primary" type="submit">Ask</button>
        </form>
    </div>
    <div id="chat-bottom"></div>
</div>
<script>
    // Keep the newest message in view.
    var sc = document.getElementById('chat-scroll');
    if (sc) sc.scrollTop = sc.scrollHeight;
</script>
<%@ include file="partials/footer.jsp" %>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ page import="com.studily.model.StudyKit" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    Note note = (Note) request.getAttribute("note");
    StudyKit.SummaryBundle bundle = (StudyKit.SummaryBundle) request.getAttribute("bundle");
    int flashcardCount = (Integer) request.getAttribute("flashcardCount");
    int mcqCount = (Integer) request.getAttribute("mcqCount");
    boolean needsGeneration = (Boolean) request.getAttribute("needsGeneration");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
%>
<div class="container">
    <div class="page-head rise">
        <div class="flex-between">
            <div>
                <h1><%= note.getTitle() %></h1>
                <p>Uploaded <%= note.getCreatedAt() == null ? "" : note.getCreatedAt().format(fmt) %>
                    · <%= note.getPdfPath() != null ? "PDF" : "Pasted text" %></p>
            </div>
            <a class="btn btn-ghost btn-sm" href="<%= ctx %>/dashboard">← Dashboard</a>
        </div>
    </div>

    <% if (needsGeneration) { %>
    <div class="card rise" style="text-align:center; padding:48px 24px">
        <div class="ai-loader">
            <div class="orb"></div>
            <h3>Ready to generate your study kit</h3>
            <p>Click below — the AI reads your note and builds a summary, flashcards, and MCQs in seconds.</p>
            <a class="btn btn-primary" href="<%= ctx %>/generate-notes?noteId=<%= note.getNoteId() %>">✨ Generate Study Material</a>
        </div>
    </div>
    <% } else { %>

    <div class="card rise rise-1">
        <div class="card-title">
            <span class="icon">📝</span>
            <h3>Summary</h3>
        </div>
        <p style="color: var(--text); font-size: 1.02rem"><%= bundle.getSummary() %></p>
    </div>

    <div class="grid-2 mt-3">
        <div class="card rise rise-2">
            <div class="card-title">
                <span class="icon">🔑</span>
                <h3>Key Concepts</h3>
            </div>
            <%
                List<String> concepts = bundle.getKeyConcepts();
                if (concepts == null || concepts.isEmpty()) {
            %><p class="text-dim">No key concepts extracted.</p><% } else { %>
            <div class="chip-list">
                <% for (String c : concepts) { %><span class="chip"><%= c %></span><% } %>
            </div>
            <% } %>
        </div>

        <div class="card rise rise-2">
            <div class="card-title">
                <span class="icon">📖</span>
                <h3>Important Definitions</h3>
            </div>
            <%
                List<String> defs = bundle.getDefinitions();
                if (defs == null || defs.isEmpty()) {
            %><p class="text-dim">No definitions extracted.</p><% } else { %>
                <% for (String d : defs) { %>
            <div class="def-item"><%= d %></div>
                <% } %>
            <% } %>
        </div>
    </div>

    <div class="card mt-3 rise rise-3">
        <div class="card-title">
            <span class="icon">🎓</span>
            <h3>Exam Tips</h3>
        </div>
        <%
            List<String> tips = bundle.getExamTips();
            if (tips == null || tips.isEmpty()) {
        %><p class="text-dim">No exam tips generated.</p><% } else { %>
            <% for (String t : tips) { %>
        <div class="tip-item"><span><%= t %></span></div>
            <% } %>
        <% } %>
    </div>

    <div class="grid-2 mt-3">
        <div class="card hoverable rise rise-3" style="text-align:center">
            <div class="stat-icon">🃏</div>
            <div class="stat-value"><%= flashcardCount %></div>
            <p>Flashcards ready for spaced repetition</p>
            <a class="btn btn-primary mt-2" href="<%= ctx %>/flashcards?noteId=<%= note.getNoteId() %>">Study Flashcards</a>
        </div>
        <div class="card hoverable rise rise-4" style="text-align:center">
            <div class="stat-icon">🧠</div>
            <div class="stat-value"><%= mcqCount %></div>
            <p>MCQs ready to test yourself</p>
            <div class="mt-2" style="display:flex; gap:8px; justify-content:center; flex-wrap:wrap">
                <a class="btn btn-primary btn-sm" href="<%= ctx %>/quiz?noteId=<%= note.getNoteId() %>&difficulty=easy">Easy</a>
                <a class="btn btn-primary btn-sm" href="<%= ctx %>/quiz?noteId=<%= note.getNoteId() %>&difficulty=medium">Medium</a>
                <a class="btn btn-primary btn-sm" href="<%= ctx %>/quiz?noteId=<%= note.getNoteId() %>&difficulty=hard">Hard</a>
                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/quiz?noteId=<%= note.getNoteId() %>">All</a>
            </div>
        </div>
    </div>

    <div class="grid-2 mt-3">
        <div class="card hoverable rise rise-3" style="text-align:center">
            <div class="stat-icon">💬</div>
            <h3>AI Chat</h3>
            <p class="mt-1">Ask questions about this note — answered strictly from your material.</p>
            <a class="btn btn-primary mt-2" href="<%= ctx %>/chat?noteId=<%= note.getNoteId() %>">Open Chat</a>
        </div>
        <div class="card hoverable rise rise-4" style="text-align:center">
            <div class="stat-icon">🗺</div>
            <h3>Mind Map</h3>
            <p class="mt-1">A visual hierarchy of this note's concepts, generated by AI.</p>
            <a class="btn btn-primary mt-2" href="<%= ctx %>/mindmap?noteId=<%= note.getNoteId() %>">View Mind Map</a>
        </div>
    </div>

    <% } %>
</div>
<%@ include file="partials/footer.jsp" %>

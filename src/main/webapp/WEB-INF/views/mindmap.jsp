<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.Note" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    Note note = (Note) request.getAttribute("note");
    String mindmapJson = (String) request.getAttribute("mindmapJson");
%>
<div class="container-narrow">
    <div class="page-head rise">
        <div class="flex-between">
            <div>
                <h1>Mind Map</h1>
                <p><%= note.getTitle() %></p>
            </div>
            <a class="btn btn-ghost btn-sm" href="<%= ctx %>/summary?noteId=<%= note.getNoteId() %>"><svg class="i i-sm"><use href="#i-chevron-left"/></svg> Summary</a>
        </div>
    </div>

    <div class="card rise rise-1" style="padding:32px">
        <div class="mindmap-tree" id="mindmap-tree"></div>
        <script type="application/json" id="mindmap-json"><%= mindmapJson %></script>
    </div>
    <p class="hint mt-2" style="text-align:center">Generated once per note from your AI summary and cached.</p>
</div>
<%@ include file="partials/footer.jsp" %>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.StudyKit" %>
<%@ page import="java.util.List" %>
<%
    String ctx = request.getContextPath();
    String title = (String) request.getAttribute("sharedTitle");
    String owner = (String) request.getAttribute("sharedOwner");
    StudyKit.SummaryBundle bundle = (StudyKit.SummaryBundle) request.getAttribute("sharedBundle");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= title %> · Shared via SnapNotes</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="icon" href="<%= ctx %>/assets/img/favicon.svg" type="image/svg+xml">
    <link rel="alternate icon" href="<%= ctx %>/assets/img/favicon.svg">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/snapnotes.css">
</head>
<body>
<%@ include file="partials/icons.jsp" %>
<nav class="navbar">
    <a class="brand" href="<%= ctx %>/index.jsp"><img class="logo-dot" src="<%= ctx %>/assets/img/logo.svg" alt="" width="26" height="26"> SnapNotes</a>
    <div class="nav-user">
        <span class="badge"><svg class="i"><use href="#i-eye"/></svg> View-only share</span>
        <a class="btn btn-primary btn-sm" href="<%= ctx %>/register">Make your own study kit</a>
    </div>
</nav>

<div class="container-narrow">
    <div class="page-head rise">
        <h1><%= title %></h1>
        <p>Shared by <strong><%= owner %></strong> · read-only preview</p>
    </div>

    <% if (bundle == null) { %>
    <div class="card rise rise-1">
        <p class="text-dim">This note hasn't had its study material generated yet.</p>
    </div>
    <% } else { %>
    <div class="card rise rise-1">
        <div class="card-title"><span class="icon"><svg class="i"><use href="#i-file"/></svg></span><h3>Summary</h3></div>
        <p style="color:var(--text); font-size:1.02rem"><%= bundle.getSummary() %></p>
    </div>
    <% if (bundle.getKeyConcepts() != null && !bundle.getKeyConcepts().isEmpty()) { %>
    <div class="card mt-3 rise rise-2">
        <div class="card-title"><span class="icon"><svg class="i"><use href="#i-key"/></svg></span><h3>Key Concepts</h3></div>
        <div class="chip-list">
            <% for (String c : bundle.getKeyConcepts()) { %><span class="chip"><%= c %></span><% } %>
        </div>
    </div>
    <% } %>
    <% if (bundle.getDefinitions() != null && !bundle.getDefinitions().isEmpty()) { %>
    <div class="card mt-3 rise rise-2">
        <div class="card-title"><span class="icon"><svg class="i"><use href="#i-book"/></svg></span><h3>Definitions</h3></div>
        <% for (String d : bundle.getDefinitions()) { %><div class="def-item"><%= d %></div><% } %>
    </div>
    <% } %>
    <% if (bundle.getExamTips() != null && !bundle.getExamTips().isEmpty()) { %>
    <div class="card mt-3 rise rise-3">
        <div class="card-title"><span class="icon"><svg class="i"><use href="#i-grad"/></svg></span><h3>Exam Tips</h3></div>
        <% for (String t : bundle.getExamTips()) { %><div class="tip-item"><svg class="i"><use href="#i-bulb"/></svg><span><%= t %></span></div><% } %>
    </div>
    <% } %>
    <% } %>

    <p class="hint" style="text-align:center; margin-top:24px">
        SnapNotes turns notes into summaries, flashcards, quizzes and audio in seconds.
    </p>
</div>
</body>
</html>

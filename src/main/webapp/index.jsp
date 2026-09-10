<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.filter.AuthFilter" %>
<%@ include file="WEB-INF/views/partials/flash.jsp" %>
<%
    String ctx = request.getContextPath();
    boolean loggedIn = request.getSession(false) != null
            && request.getSession().getAttribute(AuthFilter.SESSION_USER) != null;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Studily — AI-Powered Study Assistant</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Poppins:wght@600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/studily.css">
</head>
<body>
<nav class="navbar">
    <a class="brand" href="<%= ctx %>/index.jsp"><span class="logo-dot">S</span> Studily</a>
    <div class="nav-user">
        <% if (loggedIn) { %>
        <a class="btn btn-primary btn-sm" href="<%= ctx %>/dashboard">Go to Dashboard</a>
        <% } else { %>
        <a class="btn btn-ghost btn-sm" href="<%= ctx %>/login">Login</a>
        <a class="btn btn-primary btn-sm" href="<%= ctx %>/register">Get Started</a>
        <% } %>
    </div>
</nav>

<div class="hero rise">
    <span class="badge badge-primary" style="margin-bottom:18px">✨ Powered by Groq AI</span>
    <h1>Turn lecture notes into <span class="grad-text">study material</span> in seconds.</h1>
    <p>Upload a PDF or paste your notes. Studily generates concise summaries, interactive flashcards,
       and exam-ready MCQ quizzes — instantly.</p>
    <div class="hero-cta">
        <% if (loggedIn) { %>
        <a class="btn btn-primary" href="<%= ctx %>/dashboard">Open Dashboard</a>
        <a class="btn btn-secondary" href="<%= ctx %>/upload">Upload Notes</a>
        <% } else { %>
        <a class="btn btn-primary" href="<%= ctx %>/register">Start Free →</a>
        <a class="btn btn-secondary" href="<%= ctx %>/login">Log In</a>
        <% } %>
    </div>
</div>

<div class="features-grid">
    <div class="card hoverable feature-card rise rise-1">
        <div class="icon">📄</div>
        <h3>Instant Summaries</h3>
        <p>Key concepts, definitions, and exam tips extracted from your notes by AI.</p>
    </div>
    <div class="card hoverable feature-card rise rise-2">
        <div class="icon">🃏</div>
        <h3>Smart Flashcards</h3>
        <p>Flip, shuffle, and navigate AI-generated cards. Mark what you know.</p>
    </div>
    <div class="card hoverable feature-card rise rise-3">
        <div class="icon">🧠</div>
        <h3>MCQ Quizzes</h3>
        <p>Timed quizzes with instant feedback and AI explanations for every answer.</p>
    </div>
    <div class="card hoverable feature-card rise rise-4">
        <div class="icon">🔥</div>
        <h3>Track Progress</h3>
        <p>Streaks, scores, and study history — all your revision data in one dashboard.</p>
    </div>
</div>

<div class="footer">
    Studily — AI-Powered Study Assistant · Java EE · MySQL · Groq
</div>
<script src="<%= ctx %>/assets/js/app.js"></script>
</body>
</html>

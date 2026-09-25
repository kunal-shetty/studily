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
    <title>SnapNotes — Turn Notes Into Flashcards, Quizzes &amp; Audio</title>
    <meta name="description" content="SnapNotes turns your notes into AI-generated flashcards, quizzes and audio so you actually remember what you study.">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="icon" href="<%= ctx %>/assets/img/favicon.svg" type="image/svg+xml">
    <link rel="alternate icon" href="<%= ctx %>/assets/img/favicon.svg">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/snapnotes.css">
</head>
<body>
<%@ include file="WEB-INF/views/partials/icons.jsp" %>
<nav class="navbar">
    <a class="brand" href="<%= ctx %>/index.jsp"><img class="logo-dot" src="<%= ctx %>/assets/img/logo.svg" alt="" width="26" height="26"> SnapNotes</a>
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
    <span class="badge badge-primary" style="margin-bottom:18px"><svg class="i"><use href="#i-zap"/></svg> We're live now</span>
    <h1>Your notes. <span class="grad-text">Supercharged.</span></h1>
    <p>Stop re-reading the same notes and blanking in the exam. SnapNotes turns your notes into
       flashcards, quizzes, and audio — so you actually remember.</p>
    <div class="hero-cta">
        <% if (loggedIn) { %>
        <a class="btn btn-primary" href="<%= ctx %>/dashboard">Open Dashboard</a>
        <a class="btn btn-secondary" href="<%= ctx %>/upload">Upload Notes</a>
        <% } else { %>
        <a class="btn btn-primary" href="<%= ctx %>/register">Start Free →</a>
        <a class="btn btn-secondary" href="#features">See Features</a>
        <% } %>
    </div>
    <p class="hero-proof">Join 2,400+ students studying smarter.</p>
</div>

<div class="features-grid" id="features">
    <div class="card hoverable feature-card rise rise-1">
        <div class="icon"><svg class="i"><use href="#i-file"/></svg></div>
        <h3>Instant Summaries</h3>
        <p>Key concepts, definitions, and exam tips extracted from your notes by AI.</p>
    </div>
    <div class="card hoverable feature-card rise rise-2">
        <div class="icon"><svg class="i"><use href="#i-cards"/></svg></div>
        <h3>Smart Flashcards</h3>
        <p>Flip, shuffle, and navigate AI-generated cards with spaced repetition built in.</p>
    </div>
    <div class="card hoverable feature-card rise rise-2">
        <div class="icon"><svg class="i"><use href="#i-cpu"/></svg></div>
        <h3>MCQ Quizzes</h3>
        <p>Timed quizzes and mock tests with instant feedback and AI explanations.</p>
    </div>
    <div class="card hoverable feature-card rise rise-3">
        <div class="icon"><svg class="i"><use href="#i-volume"/></svg></div>
        <h3>Audio Revision</h3>
        <p>Listen to your summaries and flashcards read aloud — study on the move.</p>
    </div>
    <div class="card hoverable feature-card rise rise-3">
        <div class="icon"><svg class="i"><use href="#i-map"/></svg></div>
        <h3>Mind Maps</h3>
        <p>AI builds a visual hierarchy of your notes so the big picture sticks.</p>
    </div>
    <div class="card hoverable feature-card rise rise-4">
        <div class="icon"><svg class="i"><use href="#i-flame"/></svg></div>
        <h3>Track Progress</h3>
        <p>Streaks, scores, badges and leaderboards — all your revision data in one place.</p>
    </div>
</div>

<div class="footer">
    SnapNotes — AI-Powered Study Assistant · Java EE · PostgreSQL · Groq
</div>
<script src="<%= ctx %>/assets/js/app.js"></script>
</body>
</html>

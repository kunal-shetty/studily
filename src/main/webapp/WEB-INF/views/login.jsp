<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="partials/flash.jsp" %>
<%
    String prefillEmail = (String) session.getAttribute("prefillEmail");
    session.removeAttribute("prefillEmail");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login · SnapNotes</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="icon" href="<%= ctx %>/assets/img/favicon.svg" type="image/svg+xml">
    <link rel="alternate icon" href="<%= ctx %>/assets/img/favicon.svg">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/snapnotes.css">
</head>
<body>
<%@ include file="partials/icons.jsp" %>
<div class="auth-wrap">
    <div class="auth-card">
        <a class="brand" href="<%= ctx %>/index.jsp"><img class="logo-dot" src="<%= ctx %>/assets/img/logo.svg" alt="" width="26" height="26"> SnapNotes</a>
        <p class="auth-sub">Welcome back — your notes are waiting.</p>

        <form method="post" action="<%= ctx %>/login">
            <div class="form-group">
                <label for="email">Email</label>
                <input class="input" type="email" id="email" name="email" required
                       value="<%= prefillEmail != null ? prefillEmail : "" %>" placeholder="you@college.edu" autofocus>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input class="input" type="password" id="password" name="password" required placeholder="••••••••">
            </div>
            <button type="submit" class="btn btn-primary btn-block">Log In</button>
        </form>

        <p class="auth-foot">New to SnapNotes? <a href="<%= ctx %>/register">Create an account</a></p>

        <div class="auth-features">
            <span class="badge"><svg class="i"><use href="#i-sparkles"/></svg> AI Summaries</span>
            <span class="badge"><svg class="i"><use href="#i-cards"/></svg> Flashcards</span>
            <span class="badge"><svg class="i"><use href="#i-cpu"/></svg> MCQ Quizzes</span>
            <span class="badge"><svg class="i"><use href="#i-volume"/></svg> Audio Revision</span>
        </div>
    </div>
</div>
<script src="<%= ctx %>/assets/js/app.js"></script>
</body>
</html>

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
    <title>Login · Studily</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Poppins:wght@600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/studily.css">
</head>
<body>
<div class="auth-wrap">
    <div class="auth-card">
        <a class="brand" href="<%= ctx %>/index.jsp"><span class="logo-dot">S</span> Studily</a>
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

        <p class="auth-foot">New to Studily? <a href="<%= ctx %>/register">Create an account</a></p>

        <div class="auth-features">
            <span class="badge">✨ AI Summaries</span>
            <span class="badge">🃏 Flashcards</span>
            <span class="badge">🧠 MCQ Quizzes</span>
        </div>
    </div>
</div>
<script src="<%= ctx %>/assets/js/app.js"></script>
</body>
</html>

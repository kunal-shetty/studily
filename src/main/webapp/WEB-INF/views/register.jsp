<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="partials/flash.jsp" %>
<%
    String prefillName = (String) session.getAttribute("prefillName");
    String prefillEmail = (String) session.getAttribute("prefillEmail");
    session.removeAttribute("prefillName");
    session.removeAttribute("prefillEmail");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account · Studily</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Poppins:wght@600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/studily.css">
</head>
<body>
<div class="auth-wrap">
    <div class="auth-card">
        <a class="brand" href="<%= ctx %>/index.jsp"><span class="logo-dot">S</span> Studily</a>
        <p class="auth-sub">Turn your notes into study material in seconds.</p>

        <form method="post" action="<%= ctx %>/register">
            <div class="form-group">
                <label for="name">Full Name</label>
                <input class="input" type="text" id="name" name="name" required
                       value="<%= prefillName != null ? prefillName : "" %>" placeholder="Aarav Sharma">
            </div>
            <div class="form-group">
                <label for="email">Email</label>
                <input class="input" type="email" id="email" name="email" required
                       value="<%= prefillEmail != null ? prefillEmail : "" %>" placeholder="you@college.edu">
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input class="input" type="password" id="password" name="password" required minlength="8"
                       placeholder="At least 8 characters">
                <div class="hint">Minimum 8 characters, with letters and numbers.</div>
            </div>
            <div class="form-group">
                <label for="confirmPassword">Confirm Password</label>
                <input class="input" type="password" id="confirmPassword" name="confirmPassword" required
                       placeholder="Repeat your password">
            </div>
            <button type="submit" class="btn btn-primary btn-block">Create Account</button>
        </form>

        <p class="auth-foot">Already have an account? <a href="<%= ctx %>/login">Log in</a></p>
    </div>
</div>
<script src="<%= ctx %>/assets/js/app.js"></script>
</body>
</html>

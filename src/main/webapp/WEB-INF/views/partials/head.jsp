<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.User" %>
<%@ page import="com.studily.filter.AuthFilter" %>
<%
    User navUser = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    String navActive = (String) request.getAttribute("navActive");
    String ctx = request.getContextPath();
    String initials = "";
    if (navUser != null && navUser.getName() != null && navUser.getName().length() >= 2) {
        initials = navUser.getName().trim().substring(0, 1).toUpperCase()
                + navUser.getName().trim().split("\\s+")[navUser.getName().trim().split("\\s+").length - 1]
                    .substring(0, 1).toUpperCase();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "Studily" %> · Studily</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Poppins:wght@600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/studily.css">
</head>
<body>
<nav class="navbar">
    <a class="brand" href="<%= ctx %>/<%= navUser != null ? "dashboard" : "index.jsp" %>">
        <span class="logo-dot">S</span> Studily
    </a>
    <% if (navUser != null) { %>
    <div class="nav-links">
        <a href="<%= ctx %>/dashboard" class="<%= "dashboard".equals(navActive) ? "active" : "" %>">Dashboard</a>
        <a href="<%= ctx %>/upload" class="<%= "upload".equals(navActive) ? "active" : "" %>">Upload</a>
        <a href="<%= ctx %>/history" class="<%= "history".equals(navActive) ? "active" : "" %>">History</a>
    </div>
    <% } %>
    <div class="nav-user">
        <% if (navUser != null) { %>
        <span class="avatar" title="<%= navUser.getEmail() %>"><%= initials %></span>
        <a class="btn btn-ghost btn-sm" href="<%= ctx %>/logout">Logout</a>
        <% } else { %>
        <a class="btn btn-ghost btn-sm" href="<%= ctx %>/login">Login</a>
        <a class="btn btn-primary btn-sm" href="<%= ctx %>/register">Get Started</a>
        <% } %>
    </div>
</nav>

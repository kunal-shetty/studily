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
    Object dueCountAttr = request.getAttribute("dueCount");
    String dueCount = dueCountAttr == null ? "" : String.valueOf(dueCountAttr);
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
<div class="app-shell" id="app-shell">
    <aside class="sidebar" id="sidebar">
        <a class="brand" href="<%= ctx %>/dashboard"><span class="logo-dot">S</span> Studily</a>
        <nav class="side-nav">
            <span class="side-label">Study</span>
            <a href="<%= ctx %>/dashboard" class="<%= "dashboard".equals(navActive) ? "active" : "" %>"><span class="ico">🏠</span> Dashboard</a>
            <a href="<%= ctx %>/upload" class="<%= "upload".equals(navActive) ? "active" : "" %>"><span class="ico">📄</span> Upload Notes</a>
            <a href="<%= ctx %>/history" class="<%= "history".equals(navActive) ? "active" : "" %>"><span class="ico">🗂</span> My Notes</a>
            <span class="side-label">Practice</span>
            <a href="<%= ctx %>/review" class="<%= "flashcards".equals(navActive) ? "active" : "" %>"><span class="ico">🧠</span> Review <span class="side-badge"><%= dueCount.isEmpty() ? "•" : dueCount %></span></a>
            <a href="<%= ctx %>/analytics" class="<%= "analytics".equals(navActive) ? "active" : "" %>"><span class="ico">📊</span> Analytics</a>
        </nav>
        <div class="side-bottom">
            <div class="side-user">
                <span class="avatar"><%= initials %></span>
                <span class="side-user-name"><%= navUser != null ? navUser.getName() : "" %></span>
            </div>
            <a class="btn btn-ghost btn-sm btn-block" href="<%= ctx %>/logout">Logout</a>
        </div>
    </aside>
    <div class="sidebar-scrim" id="sidebar-scrim"></div>
    <main class="app-main">
        <button class="sidebar-toggle" id="sidebar-toggle" aria-label="Toggle menu">☰</button>
        <button class="search-trigger" id="search-trigger" title="Search (Ctrl+K)">🔍 Search… <kbd>Ctrl K</kbd></button>

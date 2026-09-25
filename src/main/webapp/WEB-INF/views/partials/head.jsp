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
    Object shellDueAttr = request.getAttribute("dueCount");
    String shellDue = shellDueAttr == null ? "" : String.valueOf(shellDueAttr);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "SnapNotes" %> · SnapNotes</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="icon" href="<%= ctx %>/assets/img/favicon.svg" type="image/svg+xml">
    <link rel="alternate icon" href="<%= ctx %>/assets/img/favicon.svg">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/snapnotes.css">
</head>
<body>
<%@ include file="icons.jsp" %>
<div class="app-shell" id="app-shell">
    <aside class="sidebar" id="sidebar">
        <a class="brand" href="<%= ctx %>/dashboard"><img class="logo-dot" src="<%= ctx %>/assets/img/logo.svg" alt="" width="26" height="26"> SnapNotes</a>
        <nav class="side-nav">
            <span class="side-label">Study</span>
            <a href="<%= ctx %>/dashboard" class="<%= "dashboard".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-home"/></svg></span> Dashboard</a>
            <a href="<%= ctx %>/session" class="<%= "session".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-zap"/></svg></span> AI Study Session</a>
            <a href="<%= ctx %>/upload" class="<%= "upload".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-upload"/></svg></span> Upload Notes</a>
            <a href="<%= ctx %>/history" class="<%= "history".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-folder"/></svg></span> My Notes</a>
            <span class="side-label">Practice</span>
            <a href="<%= ctx %>/review" class="<%= "flashcards".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-cpu"/></svg></span> Review<% if (!shellDue.isEmpty() && !"0".equals(shellDue)) { %> <span class="side-badge"><%= shellDue %></span><% } %></a>
            <a href="<%= ctx %>/audio" class="<%= "audio".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-headphones"/></svg></span> Listen</a>
            <a href="<%= ctx %>/analytics" class="<%= "analytics".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-chart"/></svg></span> Analytics</a>
            <a href="<%= ctx %>/calendar" class="<%= "calendar".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-calendar"/></svg></span> Calendar</a>
            <span class="side-label">Community</span>
            <a href="<%= ctx %>/leaderboard" class="<%= "leaderboard".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-trophy"/></svg></span> Leaderboard</a>
            <a href="<%= ctx %>/profile" class="<%= "profile".equals(navActive) ? "active" : "" %>"><span class="ico"><svg class="i"><use href="#i-user"/></svg></span> Profile</a>
        </nav>
        <div class="side-bottom">
            <div class="side-user">
                <span class="avatar"><%= initials %></span>
                <span class="side-user-name"><%= navUser != null ? navUser.getName() : "" %></span>
            </div>
            <div style="display:flex; gap:6px">
                <a class="btn btn-ghost btn-sm" style="flex:1" href="<%= ctx %>/settings" title="Settings"><svg class="i"><use href="#i-settings"/></svg><span class="sr-only">Settings</span></a>
                <a class="btn btn-ghost btn-sm" style="flex:1" href="<%= ctx %>/logout"><svg class="i"><use href="#i-logout"/></svg> Logout</a>
            </div>
        </div>
    </aside>
    <div class="sidebar-scrim" id="sidebar-scrim"></div>
    <main class="app-main">
        <button class="sidebar-toggle" id="sidebar-toggle" aria-label="Toggle menu"><svg class="i"><use href="#i-menu"/></svg></button>
        <button class="search-trigger" id="search-trigger" title="Search (Ctrl+K)"><svg class="i i-sm"><use href="#i-search"/></svg> Search… <kbd>Ctrl K</kbd></button>

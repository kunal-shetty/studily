<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.studily.model.UserSettings" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    UserSettings settings = (UserSettings) request.getAttribute("settings");
%>
<div class="container-narrow">
    <div class="page-head rise">
        <h1>Settings</h1>
        <p>Make SnapNotes yours — appearance, AI model, and alerts.</p>
    </div>

    <form class="card rise rise-1" method="post" action="<%= ctx %>/settings">
        <div class="card-title"><span class="icon"><svg class="i"><use href="#i-palette"/></svg></span><h3>Appearance</h3></div>

        <div class="form-group">
            <label>Theme</label>
            <div class="choice-row">
                <label class="choice selected">
                    <input type="radio" name="theme" value="dark" checked>
                    <svg class="i"><use href="#i-moon"/></svg>
                    <span>Dark</span>
                </label>
            </div>
            <div class="hint">SnapNotes is dark-only — tuned for long study sessions.</div>
        </div>

        <div class="form-group">
            <label>Accent color</label>
            <div class="choice-row">
                <% for (String a : UserSettings.ACCENTS) { %>
                <label class="choice accent-choice <%= settings.getAccent().equals(a) ? "selected" : "" %>">
                    <input type="radio" name="accent" value="<%= a %>" <%= settings.getAccent().equals(a) ? "checked" : "" %>>
                    <span class="accent-dot accent-<%= a %>"></span> <%= a.substring(0,1).toUpperCase() + a.substring(1) %>
                </label>
                <% } %>
            </div>
        </div>

        <div class="card-title mt-3"><span class="icon"><svg class="i"><use href="#i-bot"/></svg></span><h3>AI Model</h3></div>
        <div class="form-group">
            <label for="aiModel">Model used for summaries, chat, and quizzes</label>
            <select class="input" id="aiModel" name="aiModel">
                <% for (String m : UserSettings.AI_MODELS) { %>
                <option value="<%= m %>" <%= settings.getAiModel().equals(m) ? "selected" : "" %>><%= m %></option>
                <% } %>
            </select>
            <div class="hint">Smaller models are faster; larger models give richer explanations.</div>
        </div>

        <div class="card-title mt-3"><span class="icon"><svg class="i"><use href="#i-bell"/></svg></span><h3>Notifications</h3></div>
        <div class="form-group">
            <label class="check-row">
                <input type="checkbox" name="notifications" <%= settings.isNotifications() ? "checked" : "" %>>
                <span>Remind me when flashcards are due (shows the review badge)</span>
            </label>
        </div>

        <button type="submit" class="btn btn-primary btn-block mt-2">Save Settings</button>
    </form>
</div>
<script>
    // Persist appearance prefs so every page (client-side) can apply them.
    (function () {
        var prefs = {
            accent: '<%= settings.getAccent() %>'
        };
        try { localStorage.setItem('snapnotes-prefs', JSON.stringify(prefs)); } catch (e) { /* private mode */ }
    })();
</script>
<%@ include file="partials/footer.jsp" %>

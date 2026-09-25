<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    List<Map<String, Object>> tracks = (List<Map<String, Object>>) request.getAttribute("tracks");
    String tracksJson = (String) request.getAttribute("tracksJson");
    if (tracksJson == null) tracksJson = "[]";
    boolean hasAnySummary = false;
    if (tracks != null) {
        for (Map<String, Object> t : tracks) {
            if (Boolean.TRUE.equals(t.get("hasSummary"))) { hasAnySummary = true; break; }
        }
    }
%>
<div class="container-narrow">
    <div class="page-head rise">
        <h1>Listen</h1>
        <p>Turn your notes into audio. SnapNotes reads your summaries and notes aloud so you can
           revise while you walk, commute or rest your eyes.</p>
    </div>

    <% if (tracks == null || tracks.isEmpty()) { %>
    <div class="card empty-state rise rise-1">
        <div class="empty-icon"><svg class="i"><use href="#i-headphones"/></svg></div>
        <h3>Nothing to listen to yet</h3>
        <p>Upload a note and generate its study material — then come back here to hear it.</p>
        <a class="btn btn-primary mt-2" href="<%= ctx %>/upload">Upload Notes</a>
    </div>
    <% } else { %>

    <div class="card rise rise-1 mb-3">
        <div class="card-title"><span class="icon"><svg class="i"><use href="#i-headphones"/></svg></span><h3>Your audio library</h3></div>
        <p>Playback uses your browser's built-in voice. Use the player bar to pause, stop or change speed.</p>
        <div class="listen-row mt-2">
            <button type="button" class="btn btn-primary" id="play-all" <%= hasAnySummary ? "" : "disabled" %>>
                <svg class="i"><use href="#i-play"/></svg> Play all summaries
            </button>
            <span class="hint" style="margin:0">
                <%= tracks.size() %> note<%= tracks.size() == 1 ? "" : "s" %> available
            </span>
        </div>
    </div>

    <div class="track-list rise rise-2">
        <% for (Map<String, Object> t : tracks) {
               Integer id = (Integer) t.get("id");
               boolean hasSummary = Boolean.TRUE.equals(t.get("hasSummary"));
               boolean hasNotes = Boolean.TRUE.equals(t.get("hasNotes"));
        %>
        <div class="track">
            <span class="track-icon"><svg class="i"><use href="#i-headphones"/></svg></span>
            <div class="track-body">
                <div class="track-title"><%= t.get("title") %></div>
                <div class="track-sub">
                    <%= t.get("date") %>
                    · <%= hasSummary ? "Summary ready" : "No summary yet" %>
                    · <%= hasNotes ? "Full notes available" : "No note text" %>
                </div>
            </div>
            <div class="track-actions">
                <button type="button" class="btn btn-primary btn-sm" data-track="<%= id %>" data-mode="summary"
                        <%= hasSummary ? "" : "disabled" %>><svg class="i i-sm"><use href="#i-volume"/></svg> Summary</button>
                <button type="button" class="btn btn-ghost btn-sm" data-track="<%= id %>" data-mode="notes"
                        <%= hasNotes ? "" : "disabled" %>><svg class="i i-sm"><use href="#i-file"/></svg> Full notes</button>
            </div>
        </div>
        <% } %>
    </div>

    <% } %>
</div>

<script type="application/json" id="audio-tracks"><%= tracksJson.replace("</", "<\\/") %></script>
<script>
    (function () {
        var dataEl = document.getElementById('audio-tracks');
        if (!dataEl) return;
        var tracks;
        try { tracks = JSON.parse(dataEl.textContent); } catch (e) { return; }
        if (!tracks.length) return;

        function byId(id) {
            for (var i = 0; i < tracks.length; i++) if (String(tracks[i].id) === String(id)) return tracks[i];
            return null;
        }

        document.querySelectorAll('[data-track]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var t = byId(btn.dataset.track);
                if (!t) return;
                var mode = btn.dataset.mode;
                var text = mode === 'summary' ? t.summary : t.notes;
                if (!text) return;
                window.SnapNotesAudio.speak((mode === 'summary' ? 'Summary. ' : 'Notes. ') + text, t.title);
                document.querySelectorAll('.track').forEach(function (el) { el.classList.remove('active'); });
                var row = btn.closest('.track');
                if (row) row.classList.add('active');
            });
        });

        var playAll = document.getElementById('play-all');
        if (playAll) {
            playAll.addEventListener('click', function () {
                var parts = tracks.filter(function (t) { return t.hasSummary; })
                                   .map(function (t) { return t.title + '. ' + t.summary; });
                if (!parts.length) return;
                window.SnapNotesAudio.speak(parts.join('  '), 'All summaries');
            });
        }
    })();
</script>
<%@ include file="partials/footer.jsp" %>

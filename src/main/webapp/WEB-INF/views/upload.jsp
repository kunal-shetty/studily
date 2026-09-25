<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    request.setAttribute("navActive", "upload");
%>
<div class="container-narrow">
    <div class="page-head rise">
        <h1>Upload Notes</h1>
        <p>Upload a PDF or paste raw text — SnapNotes AI will build your study kit.</p>
    </div>

    <form class="card rise rise-1" method="post" action="<%= ctx %>/upload" enctype="multipart/form-data" id="upload-form">
        <div class="form-group">
            <label for="title">Title</label>
            <input class="input" type="text" id="title" name="title" required maxlength="200"
                   placeholder="e.g. DBMS — Unit 3: Normalization">
        </div>

        <div class="form-group">
            <label>Your Notes <span class="text-dim" style="font-weight:400">— upload queue: pick several PDFs at once</span></label>
            <div class="upload-zone" id="upload-zone">
                <div class="zone-icon"><svg class="i"><use href="#i-file"/></svg></div>
                <h3>Drag &amp; drop your PDFs here</h3>
                <p id="file-label">or click to browse — multiple PDFs welcome, up to 10 MB each</p>
                <input type="file" id="pdf-input" name="pdfFile" accept="application/pdf,.pdf" multiple hidden>
            </div>
            <div class="upload-queue" id="upload-queue"></div>
        </div>

        <div class="form-group">
            <label for="noteText">…or paste text</label>
            <textarea class="textarea" id="noteText" name="noteText"
                      placeholder="Paste your lecture notes here (minimum 100 characters)…"></textarea>
            <div class="hint">Provide a PDF or pasted text — at least one is required.</div>
        </div>

        <button type="submit" class="btn btn-primary btn-block" id="upload-btn"><svg class="i"><use href="#i-sparkles"/></svg> Upload &amp; Generate Study Material</button>
    </form>

    <div class="card mt-3 rise rise-2" id="ai-loader-card" style="display:none">
        <div class="ai-loader">
            <div class="orb"></div>
            <h3>Generating your study kit…</h3>
            <p>Summarizing, writing flashcards, and crafting MCQs. This usually takes a few seconds.</p>
            <div class="progress-bar" style="width:100%"><div class="fill" id="upload-progress" style="width:8%"></div></div>
        </div>
    </div>
</div>
<script>
    // Show a premium loading state while the AI generation round-trips.
    var pdfInput = document.getElementById('pdf-input');
    var queueBox = document.getElementById('upload-queue');

    function renderQueue(files) {
        queueBox.innerHTML = '';
        if (!files.length) { queueBox.style.display = 'none'; return; }
        queueBox.style.display = 'block';
        for (var i = 0; i < files.length; i++) {
            var f = files[i];
            var row = document.createElement('div');
            row.className = 'queue-row';
            row.innerHTML = '<span class="q-icon"><svg class="i i-sm"><use href="#i-file"/></svg></span><span class="q-name"></span>' +
                '<span class="q-size text-dim">' + (f.size / 1048576).toFixed(1) + ' MB</span>';
            row.querySelector('.q-name').textContent = f.name;
            queueBox.appendChild(row);
        }
        var label = document.getElementById('file-label');
        label.textContent = files.length === 1 ? files[0].name : files.length + ' PDFs queued';
    }

    pdfInput.addEventListener('change', function () { renderQueue(Array.from(pdfInput.files)); });

    document.getElementById('upload-form').addEventListener('submit', function () {
        var btn = document.getElementById('upload-btn');
        btn.disabled = true;
        btn.textContent = 'Uploading…';
        var card = document.getElementById('ai-loader-card');
        card.style.display = 'block';
        card.scrollIntoView({ behavior: 'smooth', block: 'center' });
        var fill = document.getElementById('upload-progress');
        var w = 8;
        var t = setInterval(function () {
            w = Math.min(w + Math.random() * 7, 92);
            fill.style.width = w + '%';
        }, 350);
    });
</script>
<%@ include file="partials/footer.jsp" %>

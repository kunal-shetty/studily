/* ============================================================
   SnapNotes — App JS
   Flash messages · Flashcards · Quiz timer · Upload UX
   Search · Modals · Heatmap · Charts · Review · Audio (TTS)
   ============================================================ */

/* Context path for fetch/POST endpoints (set in footer partial). */
window.snapnotesCtx = window.snapnotesCtx || '';

/* Inline SVG icon from the sprite rendered by partials/icons.jsp. */
window.snIcon = function snIcon(name, cls) {
    return '<svg class="' + (cls || 'i') + '" aria-hidden="true"><use href="#i-' + name + '"/></svg>';
};

/* ---------------- Flash toasts ---------------- */
(function () {
    const stack = document.getElementById('flash-stack');
    if (!stack) return;
    const icons = {
        success: snIcon('check-circle'),
        error: snIcon('x-circle'),
        info: snIcon('info')
    };
    stack.querySelectorAll('.flash').forEach((el) => {
        const type = el.dataset.type || 'info';
        const icon = document.createElement('span');
        icon.className = 'flash-icon';
        icon.innerHTML = icons[type] || icons.info;
        el.prepend(icon);
        const dismiss = () => {
            el.classList.add('leaving');
            setTimeout(() => el.remove(), 320);
        };
        setTimeout(dismiss, 5200);
        el.addEventListener('click', dismiss);
    });
})();

/* ---------------- Flashcards ---------------- */
(function () {
    const stage = document.getElementById('flashcard-stage');
    if (!stage) return;

    const dataEl = document.getElementById('flashcard-data');
    const cards = JSON.parse(dataEl.textContent);
    if (!cards.length) return;

    let order = cards.map((_, i) => i);
    let idx = 0;
    let flipped = false;
    let known = new Set();

    const qEl = document.getElementById('card-question');
    const aEl = document.getElementById('card-answer');
    const card = document.getElementById('flashcard');
    const counter = document.getElementById('card-counter');
    const progress = document.getElementById('card-progress');
    const knownBtn = document.getElementById('btn-known');
    const knownCount = document.getElementById('known-count');

    function render() {
        const c = cards[order[idx]];
        qEl.textContent = c.question;
        aEl.textContent = c.answer;
        counter.textContent = (idx + 1) + ' / ' + cards.length;
        if (progress) progress.style.width = (((idx + 1) / cards.length) * 100) + '%';
        card.classList.remove('flipped');
        flipped = false;
        if (knownCount) knownCount.textContent = known.size + ' marked';
        // Expose deck state for the card editor (and other consumers).
        window.snapnotesDeckOrder = order;
        window.snapnotesDeckPos = idx;
    }

    function flip() {
        flipped = !flipped;
        card.classList.toggle('flipped', flipped);
    }

    function next() { idx = (idx + 1) % cards.length; render(); }
    function prev() { idx = (idx - 1 + cards.length) % cards.length; render(); }

    function shuffle() {
        for (let i = order.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [order[i], order[j]] = [order[j], order[i]];
        }
        idx = 0;
        render();
    }

    card.addEventListener('click', flip);
    document.getElementById('btn-next')?.addEventListener('click', next);
    document.getElementById('btn-prev')?.addEventListener('click', prev);
    document.getElementById('btn-shuffle')?.addEventListener('click', shuffle);
    knownBtn?.addEventListener('click', () => {
        const id = order[idx];
        if (known.has(id)) known.delete(id); else known.add(id);
        knownBtn.classList.toggle('btn-secondary');
        knownBtn.classList.toggle('btn-primary');
        render();
    });

    // Read the current card aloud (browser text-to-speech).
    document.getElementById('btn-listen-card')?.addEventListener('click', () => {
        const c = cards[order[idx]];
        window.SnapNotesAudio.speak(
            'Question. ' + c.question + ' Answer. ' + c.answer,
            'Flashcard ' + (idx + 1) + ' of ' + cards.length);
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowRight' || e.key === ' ') { e.preventDefault(); next(); }
        if (e.key === 'ArrowLeft') { e.preventDefault(); prev(); }
        if (e.key === 'Enter') { e.preventDefault(); flip(); }
    });

    render();
})();

/* ---------------- Quiz timer ---------------- */
(function () {
    const timerEl = document.getElementById('quiz-timer');
    if (!timerEl) return;
    const deadline = parseInt(timerEl.dataset.deadline, 10);
    const form = document.getElementById('quiz-form');

    function fmt(s) {
        const m = Math.floor(s / 60);
        const sec = s % 60;
        return m + ':' + String(sec).padStart(2, '0');
    }

    function tick() {
        const remain = Math.max(0, Math.floor((deadline - Date.now()) / 1000));
        timerEl.innerHTML = snIcon('clock', 'i i-sm') + ' ' + fmt(remain);
        timerEl.classList.remove('warning', 'danger');
        if (remain <= 60) timerEl.classList.add('warning');
        if (remain <= 15) timerEl.classList.add('danger');
        if (remain === 0) {
            clearInterval(handle);
            if (form) {
                // Auto-submit when time is up.
                form.submit();
            }
        }
    }
    const handle = setInterval(tick, 250);
    tick();
})();

/* ---------------- Quiz option selection ---------------- */
(function () {
    const form = document.getElementById('quiz-form');
    if (!form) return;
    form.querySelectorAll('.option').forEach((opt) => {
        opt.addEventListener('click', () => {
            const input = opt.querySelector('input');
            if (!input) return;
            input.checked = true;
            const name = input.name;
            form.querySelectorAll('.option[data-for="' + name + '"]').forEach((o) => o.classList.remove('selected'));
            opt.classList.add('selected');
        });
    });
})();

/* ---------------- Upload zone ---------------- */
(function () {
    const zone = document.getElementById('upload-zone');
    const input = document.getElementById('pdf-input');
    if (!zone || !input) return;
    const label = document.getElementById('file-label');

    zone.addEventListener('click', () => input.click());
    zone.addEventListener('dragover', (e) => { e.preventDefault(); zone.classList.add('dragover'); });
    zone.addEventListener('dragleave', () => zone.classList.remove('dragover'));
    zone.addEventListener('drop', (e) => {
        e.preventDefault();
        zone.classList.remove('dragover');
        if (e.dataTransfer.files.length) {
            input.files = e.dataTransfer.files;
            updateLabel();
        }
    });
    input.addEventListener('change', updateLabel);

    function updateLabel() {
        if (input.files.length > 1) {
            label.textContent = input.files.length + ' PDFs queued';
            label.style.color = 'var(--primary)';
        } else if (input.files.length) {
            label.textContent = input.files[0].name;
            label.style.color = 'var(--primary)';
        } else {
            label.textContent = 'or click to browse — multiple PDFs welcome, up to 10 MB each';
            label.style.color = '';
        }
    }
})();

/* ---------------- Universal confirm modal (data-confirm forms) ---------------- */
(function () {
    var modal = document.getElementById('confirm-modal');
    if (!modal) return;
    var titleEl = document.getElementById('confirm-title');
    var textEl = document.getElementById('confirm-text');
    var okBtn = document.getElementById('confirm-ok');
    var cancelBtn = document.getElementById('confirm-cancel');
    var pendingForm = null;

    function open(message, form) {
        pendingForm = form;
        titleEl.textContent = 'Are you sure?';
        textEl.textContent = message;
        modal.hidden = false;
    }
    function close() { modal.hidden = true; pendingForm = null; }

    okBtn.addEventListener('click', function () {
        if (pendingForm) pendingForm.submit();
        close();
    });
    cancelBtn.addEventListener('click', close);
    modal.addEventListener('click', function (e) { if (e.target === modal) close(); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && !modal.hidden) close(); });

    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            open(form.getAttribute('data-confirm'), form);
        });
    });
})();

/* ---------------- v2.1: theme + accent from Settings ---------------- */
(function () {
    try {
        var prefs = JSON.parse(localStorage.getItem('snapnotes-prefs') || '{}');
        if (prefs.accent) document.body.classList.add('accent-' + prefs.accent);
    } catch (e) { /* first visit or blocked storage */ }
})();

/* ---------------- v2.1: flashcard editor ---------------- */
(function () {
    var editBtn = document.getElementById('btn-edit');
    var editor = document.getElementById('card-editor');
    if (!editBtn || !editor) return;
    var idField = document.getElementById('edit-card-id');
    var qField = document.getElementById('edit-question');
    var aField = document.getElementById('edit-answer');
    var cancel = document.getElementById('edit-cancel');

    function currentCard() {
        try {
            var data = JSON.parse(document.getElementById('flashcard-data').textContent);
            var order = window.snapnotesDeckOrder || data.map(function (_, i) { return i; });
            var idx = window.snapnotesDeckPos || 0;
            return data[order[idx]] || data[idx];
        } catch (e) { return null; }
    }

    editBtn.addEventListener('click', function () {
        var card = currentCard();
        if (!card) return;
        idField.value = card.id;
        qField.value = card.question;
        aField.value = card.answer;
        editor.style.display = 'block';
        editor.scrollIntoView({ behavior: 'smooth', block: 'center' });
        qField.focus();
    });
    cancel.addEventListener('click', function () { editor.style.display = 'none'; });
})();

/* ============================================================
   v2.0 — Sidebar, search, modals, heatmap, mind map
   ============================================================ */

/* ---------------- Sidebar (mobile) ---------------- */
(function () {
    const toggle = document.getElementById('sidebar-toggle');
    const sidebar = document.getElementById('sidebar');
    const scrim = document.getElementById('sidebar-scrim');
    if (!toggle || !sidebar) return;
    const close = () => { sidebar.classList.remove('open'); scrim && scrim.classList.remove('show'); };
    toggle.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        scrim && scrim.classList.toggle('show');
    });
    scrim && scrim.addEventListener('click', close);
})();

/* ---------------- Global search (Ctrl+K) ---------------- */
(function () {
    const trigger = document.getElementById('search-trigger');
    const modal = document.getElementById('search-modal');
    const input = document.getElementById('global-search');
    const results = document.getElementById('search-results');
    if (!trigger || !modal || !input) return;

    const icons = { note: snIcon('file'), flashcard: snIcon('cards'), quiz: snIcon('help') };
    let debounce = null;

    function open() {
        modal.hidden = false;
        input.value = '';
        results.innerHTML = '<div class="search-hint">Type at least 2 characters. Try “deadlock”.</div>';
        setTimeout(() => input.focus(), 30);
    }
    function close() { modal.hidden = true; }

    trigger.addEventListener('click', open);
    modal.addEventListener('click', (e) => { if (e.target === modal) close(); });
    document.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
            e.preventDefault();
            modal.hidden ? open() : close();
        }
        if (e.key === 'Escape') close();
    });

    input.addEventListener('input', () => {
        clearTimeout(debounce);
        const q = input.value.trim();
        if (q.length < 2) {
            results.innerHTML = '<div class="search-hint">Type at least 2 characters…</div>';
            return;
        }
        debounce = setTimeout(async () => {
            try {
                const res = await fetch(snapnotesCtx + '/search?q=' + encodeURIComponent(q));
                const data = await res.json();
                if (!data.results || !data.results.length) {
                    results.innerHTML = '<div class="search-hint">No matches found.</div>';
                    return;
                }
                results.innerHTML = data.results.map((r) =>
                    '<a class="search-item" href="' + r.href + '">' +
                    '<span class="si-icon">' + (icons[r.type] || snIcon('file')) + '</span>' +
                    '<span><span class="si-label">' + esc(r.label) + '</span><br>' +
                    '<span class="si-sub">' + esc(r.sub) + '</span></span></a>'
                ).join('');
            } catch (err) {
                results.innerHTML = '<div class="search-hint">Search failed. Try again.</div>';
            }
        }, 220);
    });

    function esc(s) {
        return String(s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }
})();

/* ---------------- Universal confirm modal ---------------- */
function showConfirm(title, text, onOk) {
    const modal = document.getElementById('confirm-modal');
    if (!modal) { if (window.confirm(text || title)) onOk && onOk(); return; }
    document.getElementById('confirm-title').textContent = title;
    document.getElementById('confirm-text').textContent = text || 'This action cannot be undone.';
    modal.hidden = false;
    const ok = document.getElementById('confirm-ok');
    const cancel = document.getElementById('confirm-cancel');
    const done = () => { modal.hidden = true; ok.onclick = null; cancel.onclick = null; };
    ok.onclick = () => { done(); onOk && onOk(); };
    cancel.onclick = done;
    modal.addEventListener('click', (e) => { if (e.target === modal) done(); }, { once: true });
}

/* ---------------- Heatmap ----------------
   Columns are calendar weeks (Monday first), so every row is the same
   weekday. Leading blanks pad the first week — otherwise the whole grid
   is stair-stepped and rows mean nothing.                              */
(function () {
    const el = document.getElementById('heatmap');
    if (!el) return;
    const data = JSON.parse(document.getElementById('heatmap-data').textContent);
    const entries = Object.entries(data);
    if (!entries.length) return;

    const mondayOffset = (iso) => {
        const d = new Date(iso + 'T00:00:00');
        return isNaN(d.getTime()) ? 0 : (d.getDay() + 6) % 7;
    };

    let html = '';
    const lead = mondayOffset(entries[0][0]);
    for (let i = 0; i < lead; i++) html += '<span class="heat-cell is-empty" aria-hidden="true"></span>';

    for (const [date, count] of entries) {
        const lvl = count === 0 ? 0 : count <= 2 ? 1 : count <= 4 ? 2 : count <= 7 ? 3 : 4;
        html += '<span class="heat-cell' + (lvl ? ' l' + lvl : '') +
                '" title="' + date + ': ' + count + ' activities"></span>';
    }

    // Pad the final week so the last column is a full week too.
    const tail = (7 - ((lead + entries.length) % 7)) % 7;
    for (let i = 0; i < tail; i++) html += '<span class="heat-cell is-empty" aria-hidden="true"></span>';

    el.innerHTML = html;
    el.scrollLeft = el.scrollWidth;
})();

/* ---------------- Accuracy trend chart ---------------- */
(function () {
    const canvas = document.getElementById('trendChart');
    const dataEl = document.getElementById('trend-data');
    if (!canvas || !dataEl || typeof Chart === 'undefined') return;
    const data = JSON.parse(dataEl.textContent);
    if (!data.length) return;
    new Chart(canvas, {
        type: 'line',
        data: {
            labels: data.map((d) => d.date),
            datasets: [{
                label: 'Quiz accuracy %',
                data: data.map((d) => d.pct),
                borderColor: '#f5b301',
                backgroundColor: 'rgba(245,179,1,0.12)',
                fill: true,
                tension: 0.35,
                pointRadius: 3,
                pointBackgroundColor: '#ffd166'
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            scales: {
                y: { min: 0, max: 100, ticks: { color: '#a1a1aa', font: { size: 11 } }, grid: { color: 'rgba(255,255,255,0.06)' }, border: { display: false } },
                x: { ticks: { color: '#a1a1aa', font: { size: 11 }, maxTicksLimit: 8 }, grid: { display: false }, border: { display: false } }
            }
        }
    });
})();

/* ---------------- Mind map tree ---------------- */
(function () {
    const el = document.getElementById('mindmap-tree');
    const dataEl = document.getElementById('mindmap-json');
    if (!el || !dataEl) return;
    let tree;
    try { tree = JSON.parse(dataEl.textContent); } catch (e) { return; }
    function esc(s) {
        return String(s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }
    function render(node, depth) {
        if (!node) return '';
        const cls = depth === 0 ? 'mm-node root' : depth === 1 ? 'mm-node branch' : 'mm-node';
        let html = '<span class="' + cls + '">' + esc(node.label) + '</span>';
        if (node.children && node.children.length) {
            html += '<ul>' + node.children.map((c) => '<li>' + render(c, depth + 1) + '</li>').join('') + '</ul>';
        }
        return html;
    }
    el.innerHTML = render(tree, 0);
})();

/* ---------------- Review session ---------------- */
(function () {
    const stage = document.getElementById('review-stage');
    if (!stage) return;
    const dataEl = document.getElementById('review-data');
    const cards = JSON.parse(dataEl.textContent);
    if (!cards.length) return;
    let idx = 0, flipped = false, rated = 0;

    const qEl = document.getElementById('review-q');
    const aEl = document.getElementById('review-a');
    const card = document.getElementById('review-card');
    const counter = document.getElementById('review-counter');
    const progress = document.getElementById('review-progress');

    function render() {
        const c = cards[idx];
        qEl.textContent = c.question;
        aEl.textContent = c.answer;
        counter.textContent = (idx + 1) + ' / ' + cards.length;
        progress.style.width = (rated / cards.length * 100) + '%';
        card.classList.remove('flipped');
        flipped = false;
    }
    card.addEventListener('click', () => { flipped = !flipped; card.classList.toggle('flipped', flipped); });

    document.querySelectorAll('.rating-btn').forEach((btn) => {
        btn.addEventListener('click', () => {
            rated++;
            const form = document.createElement('form');
            form.method = 'post';
            form.action = snapnotesCtx + '/review';
            form.innerHTML = '<input type="hidden" name="cardId" value="' + cards[idx].cardId + '">' +
                             '<input type="hidden" name="rating" value="' + btn.dataset.rating + '">';
            document.body.appendChild(form);
            form.submit();
        });
    });

    render();
})();

/* ============================================================
   Audio — Web Speech text-to-speech listener
   Usage:
     window.SnapNotesAudio.speak(text, title)
     <button data-tts-src="#element">…</button>
     <button data-tts-text="literal">…</button>
   ============================================================ */
(function () {
    /* Graceful degradation on browsers without speech synthesis. */
    if (!('speechSynthesis' in window)) {
        document.querySelectorAll('.js-tts, [data-tts-src], [data-tts-text]').forEach(function (b) { b.style.display = 'none'; });
        window.SnapNotesAudio = { speak: function () {}, stop: function () {}, supported: false };
        return;
    }

    var synth = window.speechSynthesis;
    var chunks = [];
    var cursor = 0;
    var current = null;
    var title = '';
    var rate = parseFloat(localStorage.getItem('snapnotes-tts-rate') || '1') || 1;
    var preferredVoice = localStorage.getItem('snapnotes-tts-voice') || '';
    var player, playBtn, stopBtn, titleEl, subEl, voiceSel, rateInput;

    function setPlayerIcon(name) { if (playBtn) playBtn.innerHTML = snIcon(name); }

    function buildPlayer() {
        player = document.createElement('div');
        player.className = 'audio-player';
        player.id = 'snapnotes-player';
        player.hidden = true;        player.innerHTML =
            '<span class="track-icon">' + snIcon('headphones') + '</span>' +
            '<div class="ap-info"><div class="ap-title" id="snapnotes-player-title">Audio</div>' +
            '<div class="ap-sub" id="snapnotes-player-sub">Ready</div></div>' +
            '<div class="ap-controls">' +
                '<button type="button" class="audio-btn" id="snapnotes-play" title="Play / Pause">' + snIcon('pause') + '</button>' +
                '<button type="button" class="btn btn-ghost btn-sm" id="snapnotes-stop">' + snIcon('stop') + ' Stop</button>' +
            '</div>' +
            '<label class="ap-speed">Speed <input type="range" id="snapnotes-rate" min="0.5" max="2" step="0.25" value="' + rate + '"></label>' +
            '<select class="select input-sm ap-voice" id="snapnotes-voice" title="Voice"></select>';
        document.body.appendChild(player);

        playBtn = player.querySelector('#snapnotes-play');
        stopBtn = player.querySelector('#snapnotes-stop');
        titleEl = player.querySelector('#snapnotes-player-title');
        subEl = player.querySelector('#snapnotes-player-sub');
        voiceSel = player.querySelector('#snapnotes-voice');
        rateInput = player.querySelector('#snapnotes-rate');

        playBtn.addEventListener('click', toggle);
        stopBtn.addEventListener('click', function () { stop(true); });
        rateInput.addEventListener('input', function () {
            rate = parseFloat(rateInput.value) || 1;
            try { localStorage.setItem('snapnotes-tts-rate', String(rate)); } catch (e) { /* private mode */ }
        });
        voiceSel.addEventListener('change', function () {
            preferredVoice = voiceSel.value;
            try { localStorage.setItem('snapnotes-tts-voice', preferredVoice); } catch (e) { /* private mode */ }
        });
        loadVoices();
    }

    function loadVoices() {
        if (!voiceSel) return;
        var voices = synth.getVoices() || [];
        if (!voices.length) return;
        voiceSel.innerHTML = voices.map(function (v) {
            var sel = (v.name === preferredVoice) ? ' selected' : '';
            return '<option value="' + v.name.replace(/"/g, '') + '"' + sel + '>' + v.name + ' (' + v.lang + ')</option>';
        }).join('');
        // Default to an English voice when nothing was chosen yet.
        if (!preferredVoice) {
            var en = voices.filter(function (v) { return /^en/i.test(v.lang); });
            if (en.length) { preferredVoice = en[0].name; voiceSel.value = preferredVoice; }
        }
    }

    function splitText(text) {
        var sentences = String(text).replace(/\s+/g, ' ').trim().match(/[^.!?]+[.!?]*/g) || [];
        var out = [];
        var buf = '';
        sentences.forEach(function (s) {
            if ((buf + s).length > 220) { if (buf) out.push(buf.trim()); buf = ''; }
            buf += s + ' ';
        });
        if (buf.trim()) out.push(buf.trim());
        return out.length ? out : [String(text).trim()];
    }

    function speakChunk(i) {
        cursor = i;
        if (i >= chunks.length) { subEl.textContent = 'Finished'; setPlayerIcon('play'); player.classList.remove('playing'); playBtn.classList.remove('playing'); return; }
        subEl.textContent = 'Reading ' + (i + 1) + ' / ' + chunks.length;
        current = new SpeechSynthesisUtterance(chunks[i]);
        current.rate = rate;
        if (preferredVoice && voiceSel) {
            var match = (synth.getVoices() || []).filter(function (v) { return v.name === preferredVoice; })[0];
            if (match) current.voice = match;
        }
        current.onend = function () { if (synth.speaking || cursor + 1 < chunks.length) speakChunk(i + 1); };
        current.onerror = function () { speakChunk(i + 1); };
        synth.speak(current);
    }

    function speak(text, t) {
        if (!text || !String(text).trim()) return;
        stop(false);
        chunks = splitText(text);
        title = t || 'SnapNotes Audio';
        if (!player) buildPlayer();
        titleEl.textContent = title;
        loadVoices();
        player.hidden = false;
        setPlayerIcon('pause');
        player.classList.add('playing');
        playBtn.classList.add('playing');
        speakChunk(0);
    }

    function toggle() {
        if (synth.speaking && !synth.paused) {
            synth.pause();
            setPlayerIcon('play');
            playBtn.classList.remove('playing');
            player.classList.remove('playing');
            if (subEl) subEl.textContent = 'Paused';
        } else if (synth.paused) {
            synth.resume();
            setPlayerIcon('pause');
            playBtn.classList.add('playing');
            player.classList.add('playing');
            if (subEl) subEl.textContent = 'Reading ' + (cursor + 1) + ' / ' + chunks.length;
        } else if (chunks.length) {
            speakChunk(cursor < chunks.length ? cursor : 0);
            setPlayerIcon('pause');
            playBtn.classList.add('playing');
            player.classList.add('playing');
        }
    }

    function stop(hide) {
        synth.cancel();
        current = null;
        if (player) {
            player.classList.remove('playing');
            playBtn.classList.remove('playing');
            setPlayerIcon('play');
            if (hide) { player.hidden = true; chunks = []; }
        }
    }

    /* Delegated wiring for any [data-tts-src] / [data-tts-text] button. */
    document.addEventListener('click', function (e) {
        var btn = e.target.closest('[data-tts-src],[data-tts-text]');
        if (!btn) return;
        e.preventDefault();
        var text = btn.getAttribute('data-tts-text');
        var sel = btn.getAttribute('data-tts-src');
        if (!text && sel) {
            var el = document.querySelector(sel);
            text = el ? (el.innerText || el.textContent) : '';
        }
        if (text && text.trim()) speak(text.trim(), btn.getAttribute('data-tts-title') || 'SnapNotes Audio');
    });

    if (synth.onvoiceschanged !== undefined) synth.onvoiceschanged = loadVoices;
    window.addEventListener('beforeunload', function () { synth.cancel(); });

    window.SnapNotesAudio = { speak: speak, stop: function () { stop(true); }, supported: true };
})();

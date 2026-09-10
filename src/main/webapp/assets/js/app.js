/* ============================================================
   Studily — App JS
   Flash messages · Flashcards · Quiz timer · Upload UX
   Search · Modals · Heatmap · Charts · Review
   ============================================================ */

/* Context path for fetch/POST endpoints (set in footer partial). */
window.studilyCtx = window.studilyCtx || '';

/* ---------------- Flash toasts ---------------- */
(function () {
    const stack = document.getElementById('flash-stack');
    if (!stack) return;
    const icons = {
        success: '✓',
        error: '✕',
        info: 'ⓘ'
    };
    stack.querySelectorAll('.flash').forEach((el) => {
        const type = el.dataset.type || 'info';
        const icon = document.createElement('span');
        icon.className = 'flash-icon';
        icon.textContent = icons[type] || icons.info;
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
        timerEl.textContent = '⏱ ' + fmt(remain);
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
        if (input.files.length) {
            label.textContent = input.files[0].name;
            label.style.color = 'var(--primary)';
        } else {
            label.textContent = 'or click to browse — PDF up to 10 MB';
            label.style.color = '';
        }
    }
})();

/* ---------------- Confirm delete ---------------- */
function confirmAction(message) {
    return window.confirm(message);
}

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

    const icons = { note: '📄', flashcard: '🃏', quiz: '❓' };
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
                const res = await fetch(studilyCtx + '/search?q=' + encodeURIComponent(q));
                const data = await res.json();
                if (!data.results || !data.results.length) {
                    results.innerHTML = '<div class="search-hint">No matches found.</div>';
                    return;
                }
                results.innerHTML = data.results.map((r) =>
                    '<a class="search-item" href="' + r.href + '">' +
                    '<span class="si-icon">' + (icons[r.type] || '•') + '</span>' +
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

/* ---------------- Heatmap ---------------- */
(function () {
    const el = document.getElementById('heatmap');
    if (!el) return;
    const data = JSON.parse(document.getElementById('heatmap-data').textContent);
    let html = '';
    for (const [date, count] of Object.entries(data)) {
        const lvl = count === 0 ? 0 : count <= 2 ? 1 : count <= 4 ? 2 : count <= 7 ? 3 : 4;
        html += '<span class="heat-cell' + (lvl ? ' l' + lvl : '') + '" title="' + date + ': ' + count + ' activities"></span>';
    }
    el.innerHTML = html;
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
                borderColor: '#4f7cff',
                backgroundColor: 'rgba(79,124,255,0.12)',
                fill: true,
                tension: 0.35,
                pointRadius: 3,
                pointBackgroundColor: '#7a5cff'
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            scales: {
                y: { min: 0, max: 100, ticks: { color: '#9aa3b5' }, grid: { color: 'rgba(255,255,255,0.06)' } },
                x: { ticks: { color: '#9aa3b5', maxTicksLimit: 8 }, grid: { display: false } }
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
            form.action = studilyCtx + '/review';
            form.innerHTML = '<input type="hidden" name="cardId" value="' + cards[idx].cardId + '">' +
                             '<input type="hidden" name="rating" value="' + btn.dataset.rating + '">';
            document.body.appendChild(form);
            form.submit();
        });
    });

    render();
})();

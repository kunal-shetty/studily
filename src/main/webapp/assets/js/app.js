/* ============================================================
   Studily — App JS
   Flash messages · Flashcards · Quiz timer · Upload UX
   ============================================================ */

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

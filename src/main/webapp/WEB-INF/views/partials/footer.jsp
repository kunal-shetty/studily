<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    </main>
</div>

<!-- Global search modal (Ctrl+K) -->
<div class="modal-overlay" id="search-modal" hidden>
    <div class="modal search-modal">
        <input class="input search-input" id="global-search" type="text" placeholder="Search notes, flashcards, quizzes…" autocomplete="off">
        <div class="search-results" id="search-results">
            <div class="search-hint">Type at least 2 characters. Try “deadlock”.</div>
        </div>
    </div>
</div>

<!-- Universal confirm modal -->
<div class="modal-overlay" id="confirm-modal" hidden>
    <div class="modal">
        <h3 id="confirm-title">Are you sure?</h3>
        <p class="text-dim mt-1" id="confirm-text">This action cannot be undone.</p>
        <div class="mt-3" style="display:flex; gap:10px; justify-content:flex-end">
            <button class="btn btn-ghost" id="confirm-cancel">Cancel</button>
            <button class="btn btn-danger" id="confirm-ok">Confirm</button>
        </div>
    </div>
</div>

<script>window.snapnotesCtx = '<%= request.getContextPath() %>';</script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/app.js"></script>
</body>
</html>

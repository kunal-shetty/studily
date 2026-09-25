-- =====================================================
-- SnapNotes v2.0 — Database Migration (Supabase PostgreSQL)
-- Run in Supabase Dashboard → SQL Editor (paste, Run)
-- Safe to re-run (IF NOT EXISTS everywhere).
-- =====================================================

-- AI Chat with Notes
CREATE TABLE IF NOT EXISTS chat_messages (
    id          BIGSERIAL PRIMARY KEY,
    user_id     INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    note_id     INT NOT NULL REFERENCES notes(note_id) ON DELETE CASCADE,
    role        VARCHAR(10) NOT NULL,               -- 'user' | 'assistant'
    content     TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_chat_user_note ON chat_messages(user_id, note_id, id);

-- Spaced repetition scheduling (SM-2 style)
CREATE TABLE IF NOT EXISTS flashcard_reviews (
    id                BIGSERIAL PRIMARY KEY,
    card_id           INT NOT NULL REFERENCES flashcards(id) ON DELETE CASCADE,
    user_id           INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    ease_factor       NUMERIC(4,2) NOT NULL DEFAULT 2.50,
    interval_days     INT NOT NULL DEFAULT 0,
    repetitions       INT NOT NULL DEFAULT 0,
    due_date          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_rating       INT,                          -- 1 again, 2 hard, 3 good, 4 easy
    last_reviewed_at  TIMESTAMPTZ,
    CONSTRAINT uq_review UNIQUE (card_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_review_due ON flashcard_reviews(user_id, due_date);

-- Per-question quiz answers (powers explanations + weak-topic detection)
CREATE TABLE IF NOT EXISTS quiz_answers (
    id             BIGSERIAL PRIMARY KEY,
    result_id      INT NOT NULL REFERENCES quiz_results(id) ON DELETE CASCADE,
    mcq_id         INT NOT NULL REFERENCES mcqs(id) ON DELETE CASCADE,
    user_id        INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    chosen_answer  VARCHAR(1),
    is_correct     BOOLEAN NOT NULL,
    note_question  TEXT NOT NULL                   -- denormalized: survives MCQ deletion
);
CREATE INDEX IF NOT EXISTS idx_qanswers_user ON quiz_answers(user_id);

-- Generated mind maps
ALTER TABLE notes ADD COLUMN IF NOT EXISTS mindmap_json TEXT;

-- Per-result elapsed time
ALTER TABLE quiz_results ADD COLUMN IF NOT EXISTS elapsed_seconds INT;

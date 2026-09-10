-- =====================================================
-- Studily Database Schema — Supabase (PostgreSQL)
-- Run in Supabase Dashboard → SQL Editor (paste whole file, Run)
-- =====================================================

-- Users
CREATE TABLE IF NOT EXISTS users (
    user_id       SERIAL PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(100)  NOT NULL,
    role          VARCHAR(20)   NOT NULL DEFAULT 'student',
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- Notes
CREATE TABLE IF NOT EXISTS notes (
    note_id        SERIAL PRIMARY KEY,
    user_id        INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title          VARCHAR(200) NOT NULL,
    pdf_path       VARCHAR(255),
    extracted_text TEXT,
    summary        TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notes_user        ON notes(user_id);
CREATE INDEX IF NOT EXISTS idx_notes_user_created ON notes(user_id, created_at);

-- Flashcards
CREATE TABLE IF NOT EXISTS flashcards (
    id       SERIAL PRIMARY KEY,
    note_id  INT NOT NULL REFERENCES notes(note_id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    answer   TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_flash_note ON flashcards(note_id);

-- MCQs
CREATE TABLE IF NOT EXISTS mcqs (
    id             SERIAL PRIMARY KEY,
    note_id        INT NOT NULL REFERENCES notes(note_id) ON DELETE CASCADE,
    question       TEXT NOT NULL,
    option_a       TEXT NOT NULL,
    option_b       TEXT NOT NULL,
    option_c       TEXT NOT NULL,
    option_d       TEXT NOT NULL,
    correct_answer VARCHAR(1) NOT NULL,
    explanation    TEXT NOT NULL,
    difficulty     VARCHAR(20) NOT NULL DEFAULT 'medium'
);
CREATE INDEX IF NOT EXISTS idx_mcq_note      ON mcqs(note_id);
CREATE INDEX IF NOT EXISTS idx_mcq_note_diff ON mcqs(note_id, difficulty);

-- Quiz results
CREATE TABLE IF NOT EXISTS quiz_results (
    id              SERIAL PRIMARY KEY,
    user_id         INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    note_id         INT NOT NULL REFERENCES notes(note_id) ON DELETE CASCADE,
    score           INT NOT NULL,
    total_questions INT NOT NULL,
    attempt_date    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_result_user      ON quiz_results(user_id);
CREATE INDEX IF NOT EXISTS idx_result_user_date ON quiz_results(user_id, attempt_date);

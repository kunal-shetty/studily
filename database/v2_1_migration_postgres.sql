-- =====================================================
-- Studily v2.1 Migration (Supabase PostgreSQL)
-- Settings, bookmarks, shares, subjects, leaderboard, badges
-- Safe to re-run.
-- =====================================================

-- User preferences
CREATE TABLE IF NOT EXISTS user_settings (
    user_id           INT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    theme             VARCHAR(10) NOT NULL DEFAULT 'dark',      -- dark | light
    accent            VARCHAR(20) NOT NULL DEFAULT 'blue',      -- blue | violet | emerald | rose
    ai_model          VARCHAR(60) NOT NULL DEFAULT 'llama-3.3-70b-versatile',
    notifications     BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Note bookmarks (favorites / quick revision)
ALTER TABLE notes ADD COLUMN IF NOT EXISTS bookmarked BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_notes_bookmark ON notes(user_id, bookmarked);

-- Subject folders
CREATE TABLE IF NOT EXISTS subjects (
    id         SERIAL PRIMARY KEY,
    user_id    INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name       VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_subject_name UNIQUE (user_id, name)
);

-- Assign notes to subjects
ALTER TABLE notes ADD COLUMN IF NOT EXISTS subject_id INT REFERENCES subjects(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_notes_subject ON notes(subject_id);

-- Share links (secret token, view-only)
CREATE TABLE IF NOT EXISTS note_shares (
    id         SERIAL PRIMARY KEY,
    note_id    INT NOT NULL REFERENCES notes(note_id) ON DELETE CASCADE,
    token      VARCHAR(43) NOT NULL UNIQUE,       -- base64url of 32 random bytes
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Weekly leaderboard source: XP events
CREATE TABLE IF NOT EXISTS xp_events (
    id         BIGSERIAL PRIMARY KEY,
    user_id    INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    xp         INT NOT NULL,
    reason     VARCHAR(60) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_xp_week ON xp_events(user_id, created_at);

-- Unlocked badges
CREATE TABLE IF NOT EXISTS user_badges (
    user_id    INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    badge_key  VARCHAR(40) NOT NULL,
    earned_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_badge PRIMARY KEY (user_id, badge_key)
);

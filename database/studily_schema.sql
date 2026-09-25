-- =====================================================
-- SnapNotes - Database Schema
-- MySQL 8.x  |  Run: mysql -u root -p < studily_schema.sql
-- =====================================================

DROP DATABASE IF EXISTS studily;
CREATE DATABASE studily CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE studily;

-- -----------------------------------------------------
-- users
-- -----------------------------------------------------
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(100)  NOT NULL,
    role          VARCHAR(20)   NOT NULL DEFAULT 'student',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- notes
-- -----------------------------------------------------
CREATE TABLE notes (
    note_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT      NOT NULL,
    title          VARCHAR(200) NOT NULL,
    pdf_path       VARCHAR(255) DEFAULT NULL,
    extracted_text LONGTEXT,
    summary        LONGTEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_notes_user (user_id),
    INDEX idx_notes_user_created (user_id, created_at)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- flashcards
-- -----------------------------------------------------
CREATE TABLE flashcards (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    note_id  INT NOT NULL,
    question TEXT NOT NULL,
    answer   TEXT NOT NULL,
    CONSTRAINT fk_flash_note FOREIGN KEY (note_id) REFERENCES notes(note_id) ON DELETE CASCADE,
    INDEX idx_flash_note (note_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- mcqs
-- -----------------------------------------------------
CREATE TABLE mcqs (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    note_id        INT NOT NULL,
    question       TEXT NOT NULL,
    option_a       TEXT NOT NULL,
    option_b       TEXT NOT NULL,
    option_c       TEXT NOT NULL,
    option_d       TEXT NOT NULL,
    correct_answer VARCHAR(1) NOT NULL,
    explanation    TEXT NOT NULL,
    difficulty     VARCHAR(20) NOT NULL DEFAULT 'medium',
    CONSTRAINT fk_mcq_note FOREIGN KEY (note_id) REFERENCES notes(note_id) ON DELETE CASCADE,
    INDEX idx_mcq_note (note_id),
    INDEX idx_mcq_note_diff (note_id, difficulty)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- quiz_results
-- -----------------------------------------------------
CREATE TABLE quiz_results (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    note_id        INT NOT NULL,
    score          INT NOT NULL,
    total_questions INT NOT NULL,
    attempt_date   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_result_note FOREIGN KEY (note_id) REFERENCES notes(note_id) ON DELETE CASCADE,
    INDEX idx_result_user (user_id),
    INDEX idx_result_user_date (user_id, attempt_date)
) ENGINE=InnoDB;

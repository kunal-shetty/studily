package com.studily.model;

import java.time.LocalDateTime;

/**
 * Spaced-repetition scheduling state for one flashcard (SM-2 style).
 */
public class FlashcardReview {

    public static final int RATING_AGAIN = 1;
    public static final int RATING_HARD = 2;
    public static final int RATING_GOOD = 3;
    public static final int RATING_EASY = 4;

    private long id;
    private int cardId;
    private int userId;
    private double easeFactor;
    private int intervalDays;
    private int repetitions;
    private LocalDateTime dueDate;
    private Integer lastRating;
    private LocalDateTime lastReviewedAt;

    // Joined for display
    private String question;
    private String answer;
    private int noteId;
    private String noteTitle;

    /** SM-2: recompute schedule after a review with the given rating. */
    public static void applyRating(FlashcardReview r, int rating) {
        double ef = r.easeFactor <= 0 ? 2.5 : r.easeFactor;
        if (rating == RATING_AGAIN) {
            r.repetitions = 0;
            r.intervalDays = 0;               // due again immediately
            ef = Math.max(1.3, ef - 0.20);
        } else {
            int q = rating;                    // hard=2 good=3 easy=4
            ef = ef + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02));
            ef = Math.max(1.3, Math.min(2.8, ef));
            r.repetitions = r.repetitions + 1;
            if (r.repetitions == 1) {
                r.intervalDays = rating == RATING_HARD ? 1 : rating == RATING_GOOD ? 1 : 3;
            } else if (r.repetitions == 2) {
                r.intervalDays = rating == RATING_HARD ? 3 : rating == RATING_GOOD ? 6 : 9;
            } else {
                r.intervalDays = (int) Math.round(r.intervalDays * ef * (rating == RATING_HARD ? 0.7 : rating == RATING_EASY ? 1.3 : 1.0));
                r.intervalDays = Math.max(1, Math.min(365, r.intervalDays));
            }
        }
        r.easeFactor = ef;
        r.lastRating = rating;
        r.lastReviewedAt = LocalDateTime.now();
        r.dueDate = r.intervalDays == 0
                ? LocalDateTime.now()
                : LocalDateTime.now().plusDays(r.intervalDays);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Integer getLastRating() { return lastRating; }
    public void setLastRating(Integer lastRating) { this.lastRating = lastRating; }

    public LocalDateTime getLastReviewedAt() { return lastReviewedAt; }
    public void setLastReviewedAt(LocalDateTime lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public int getNoteId() { return noteId; }
    public void setNoteId(int noteId) { this.noteId = noteId; }

    public String getNoteTitle() { return noteTitle; }
    public void setNoteTitle(String noteTitle) { this.noteTitle = noteTitle; }
}

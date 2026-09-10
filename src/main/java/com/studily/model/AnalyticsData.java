package com.studily.model;

import java.util.List;
import java.util.Map;

/**
 * View model for the analytics page.
 */
public class AnalyticsData {

    /** Daily activity counts for the GitHub-style heatmap (date ISO -> count). */
    private Map<String, Integer> heatmap;
    private List<QuizResult> quizTrend;          // chronological attempts
    private List<WeakTopic> weakTopics;          // AI-ranked
    private int totalReviews;                    // spaced-rep reviews done
    private int dueCount;                        // cards due now

    public Map<String, Integer> getHeatmap() { return heatmap; }
    public void setHeatmap(Map<String, Integer> heatmap) { this.heatmap = heatmap; }

    public List<QuizResult> getQuizTrend() { return quizTrend; }
    public void setQuizTrend(List<QuizResult> quizTrend) { this.quizTrend = quizTrend; }

    public List<WeakTopic> getWeakTopics() { return weakTopics; }
    public void setWeakTopics(List<WeakTopic> weakTopics) { this.weakTopics = weakTopics; }

    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }

    public int getDueCount() { return dueCount; }
    public void setDueCount(int dueCount) { this.dueCount = dueCount; }

    public static class WeakTopic {
        private String topic;
        private String reason;
        private int noteId;

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public int getNoteId() { return noteId; }
        public void setNoteId(int noteId) { this.noteId = noteId; }
    }
}

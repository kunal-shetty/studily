package com.studily.model;

import java.util.List;

/**
 * Quiz-taking view model: the MCQs for a note, plus the index of the
 * question currently being answered (session-driven step-through quiz).
 */
public class QuizDetail {

    private Note note;
    private List<MCQ> mcqs;
    private int currentQuestionIndex;
    private int totalQuestions;
    private int timeLimitSeconds;

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public List<MCQ> getMcqs() { return mcqs; }
    public void setMcqs(List<MCQ> mcqs) { this.mcqs = mcqs; }

    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public MCQ getCurrentMcq() {
        if (mcqs == null || mcqs.isEmpty()) return null;
        int idx = Math.min(currentQuestionIndex, mcqs.size() - 1);
        return mcqs.get(idx);
    }
}

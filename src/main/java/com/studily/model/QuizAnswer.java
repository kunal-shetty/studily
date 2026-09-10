package com.studily.model;

/**
 * One answered MCQ inside a quiz attempt. Powers per-question explanations
 * and cross-quiz weak-topic detection.
 */
public class QuizAnswer {

    private long id;
    private int resultId;
    private int mcqId;
    private int userId;
    private String chosenAnswer;
    private boolean correct;
    private String noteQuestion;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getMcqId() { return mcqId; }
    public void setMcqId(int mcqId) { this.mcqId = mcqId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getChosenAnswer() { return chosenAnswer; }
    public void setChosenAnswer(String chosenAnswer) { this.chosenAnswer = chosenAnswer; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public String getNoteQuestion() { return noteQuestion; }
    public void setNoteQuestion(String noteQuestion) { this.noteQuestion = noteQuestion; }
}

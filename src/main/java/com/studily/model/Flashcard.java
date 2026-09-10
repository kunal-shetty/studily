package com.studily.model;

/**
 * A single AI-generated flashcard (question/answer pair).
 */
public class Flashcard {

    private int id;
    private int noteId;
    private String question;
    private String answer;

    public Flashcard() {
    }

    public Flashcard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNoteId() { return noteId; }
    public void setNoteId(int noteId) { this.noteId = noteId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}

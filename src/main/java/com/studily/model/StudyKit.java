package com.studily.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Aggregate of everything AI-generated for one note.
 */
public class StudyKit {

    private Note note;
    private String summary;
    private List<String> keyConcepts;
    private List<String> definitions;
    private List<String> examTips;
    private List<Flashcard> flashcards;
    private List<MCQ> mcqs;

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getKeyConcepts() { return keyConcepts; }
    public void setKeyConcepts(List<String> keyConcepts) { this.keyConcepts = keyConcepts; }

    public List<String> getDefinitions() { return definitions; }
    public void setDefinitions(List<String> definitions) { this.definitions = definitions; }

    public List<String> getExamTips() { return examTips; }
    public void setExamTips(List<String> examTips) { this.examTips = examTips; }

    public List<Flashcard> getFlashcards() { return flashcards; }
    public void setFlashcards(List<Flashcard> flashcards) { this.flashcards = flashcards; }

    public List<MCQ> getMcqs() { return mcqs; }
    public void setMcqs(List<MCQ> mcqs) { this.mcqs = mcqs; }

    /**
     * Structured summary document the AI must return.
     */
    public static class SummaryBundle {

        @SerializedName("summary")
        private String summary;

        @SerializedName("key_concepts")
        private List<String> keyConcepts;

        @SerializedName("definitions")
        private List<String> definitions;

        @SerializedName("exam_tips")
        private List<String> examTips;

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public List<String> getKeyConcepts() { return keyConcepts; }
        public void setKeyConcepts(List<String> keyConcepts) { this.keyConcepts = keyConcepts; }

        public List<String> getDefinitions() { return definitions; }
        public void setDefinitions(List<String> definitions) { this.definitions = definitions; }
        public void setExamTips(List<String> examTips) { this.examTips = examTips; }
        public List<String> getExamTips() { return examTips; }
    }

    /**
     * Structured flashcard the AI must return.
     */
    public static class FlashcardItem {

        @SerializedName("question")
        private String question;

        @SerializedName("answer")
        private String answer;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    /**
     * Structured MCQ the AI must return.
     */
    public static class MCQItem {

        @SerializedName("question")
        private String question;

        @SerializedName("option_a") private String optionA;
        @SerializedName("option_b") private String optionB;
        @SerializedName("option_c") private String optionC;
        @SerializedName("option_d") private String optionD;

        @SerializedName("correct_answer")
        private String correctAnswer;

        @SerializedName("explanation")
        private String explanation;

        @SerializedName("difficulty")
        private String difficulty;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getOptionA() { return optionA; }
        public void setOptionA(String optionA) { this.optionA = optionA; }
        public String getOptionB() { return optionB; }
        public void setOptionB(String optionB) { this.optionB = optionB; }
        public String getOptionC() { return optionC; }
        public void setOptionC(String optionC) { this.optionC = optionC; }
        public String getOptionD() { return optionD; }
        public void setOptionD(String optionD) { this.optionD = optionD; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    }

    /**
     * Top-level AI response document.
     */
    public static class AIPayload {

        @SerializedName("summary")
        private SummaryBundle summary;

        @SerializedName("flashcards")
        private List<FlashcardItem> flashcards;

        @SerializedName("mcqs")
        private List<MCQItem> mcqs;

        public SummaryBundle getSummary() { return summary; }
        public void setSummary(SummaryBundle summary) { this.summary = summary; }

        public List<FlashcardItem> getFlashcards() { return flashcards; }
        public void setFlashcards(List<FlashcardItem> flashcards) { this.flashcards = flashcards; }

        public List<MCQItem> getMcqs() { return mcqs; }
        public void setMcqs(List<MCQItem> mcqs) { this.mcqs = mcqs; }
    }
}

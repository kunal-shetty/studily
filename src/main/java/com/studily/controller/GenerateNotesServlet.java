package com.studily.controller;

import com.studily.dao.FlashcardDAO;
import com.studily.dao.MCQDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.*;
import com.studily.service.AIService;
import com.studily.util.Flash;
import com.studily.util.Log;
import com.studily.util.Validators;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * AI generation pipeline: load note -> call Groq -> parse JSON -> persist
 * summary + flashcards + MCQs -> redirect to the summary page.
 */
@WebServlet(name = "generateNotesServlet", urlPatterns = {"/generate-notes"})
public class GenerateNotesServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final FlashcardDAO flashcardDAO = new FlashcardDAO();
    private final MCQDAO mcqDAO = new MCQDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));

        if (noteId == null) {
            Flash.error(request, "Invalid note reference.");
            redirect(request, response, "/dashboard");
            return;
        }

        Note note;
        try {
            note = notesDAO.findByIdAndUser(noteId, user.getUserId());
        } catch (SQLException e) {
            Flash.error(request, "Database error while loading the note.");
            redirect(request, response, "/dashboard");
            return;
        }
        if (note == null) {
            Flash.error(request, "Note not found.");
            redirect(request, response, "/dashboard");
            return;
        }

        // Skip regeneration if AI content already exists.
        try {
            if (note.getSummaryJson() != null && !note.getSummaryJson().isBlank()
                    && mcqDAO.countByNote(noteId) > 0) {
                redirect(request, response, "/summary?noteId=" + noteId);
                return;
            }
        } catch (SQLException e) {
            Flash.error(request, "Database error while checking existing material.");
            redirect(request, response, "/dashboard");
            return;
        }

        if (note.getExtractedText() == null || note.getExtractedText().isBlank()) {
            Flash.error(request, "This note has no text to analyze.");
            redirect(request, response, "/upload");
            return;
        }

        // --- Call the AI service ---
        StudyKit.AIPayload payload;
        try {
            payload = AIService.generateStudyKit(note.getExtractedText());
        } catch (AIService.AIServiceException e) {
            // Fallback: keep uploaded file, allow retry.
            Flash.error(request, e.getMessage() + " You can retry from the note's page.");
            redirect(request, response, "/summary?noteId=" + noteId);
            return;
        }

        // --- Persist everything ---
        try {
            StudyKit.SummaryBundle s = payload.getSummary();
            StudyKit kit = new StudyKit();
            kit.setNote(note);
            kit.setSummary(s.getSummary());
            kit.setKeyConcepts(s.getKeyConcepts() == null ? List.of() : s.getKeyConcepts());
            kit.setDefinitions(s.getDefinitions() == null ? List.of() : s.getDefinitions());
            kit.setExamTips(s.getExamTips() == null ? List.of() : s.getExamTips());

            List<Flashcard> cards = new ArrayList<>();
            for (StudyKit.FlashcardItem item : payload.getFlashcards()) {
                if (item.getQuestion() == null || item.getAnswer() == null) continue;
                cards.add(new Flashcard(item.getQuestion(), item.getAnswer()));
            }

            List<MCQ> mcqs = new ArrayList<>();
            for (StudyKit.MCQItem item : payload.getMcqs()) {
                if (item.getQuestion() == null || item.getCorrectAnswer() == null) continue;
                MCQ m = new MCQ();
                m.setNoteId(noteId);
                m.setQuestion(item.getQuestion());
                m.setOptionA(item.getOptionA());
                m.setOptionB(item.getOptionB());
                m.setOptionC(item.getOptionC());
                m.setOptionD(item.getOptionD());
                m.setCorrectAnswer(item.getCorrectAnswer().trim().toUpperCase().substring(0, 1));
                m.setExplanation(item.getExplanation() == null ? "" : item.getExplanation());
                String diff = item.getDifficulty() == null ? "medium" : item.getDifficulty().trim().toLowerCase();
                m.setDifficulty(List.of("easy", "medium", "hard").contains(diff) ? diff : "medium");
                mcqs.add(m);
            }

            if (cards.isEmpty() || mcqs.isEmpty()) {
                throw new AIService.AIServiceException("The AI returned incomplete study material.");
            }

            notesDAO.updateSummary(noteId, gson.toJson(kitToBundle(kit)));
            flashcardDAO.insertBatch(noteId, cards);
            mcqDAO.insertBatch(noteId, mcqs);

            Flash.success(request, "Study material generated!");
            redirect(request, response, "/summary?noteId=" + noteId);
        } catch (SQLException e) {
            Log.severe("Generate-notes DB error (note " + noteId + "): " + e.getMessage(), e);
            Flash.error(request, "Database error while saving AI results. You can retry generation.");
            redirect(request, response, "/summary?noteId=" + noteId);
        } catch (AIService.AIServiceException e) {
            Flash.error(request, e.getMessage());
            redirect(request, response, "/summary?noteId=" + noteId);
        }
    }

    /** Converts the aggregate StudyKit into the persisted summary JSON shape. */
    private StudyKit.SummaryBundle kitToBundle(StudyKit kit) {
        StudyKit.SummaryBundle bundle = new StudyKit.SummaryBundle();
        bundle.setSummary(kit.getSummary());
        bundle.setKeyConcepts(kit.getKeyConcepts());
        bundle.setDefinitions(kit.getDefinitions());
        bundle.setExamTips(kit.getExamTips());
        return bundle;
    }
}

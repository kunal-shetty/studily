package com.studily.controller;

import com.studily.dao.ChatDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.ChatMessage;
import com.studily.model.Note;
import com.studily.model.User;
import com.studily.service.AIService;
import com.studily.util.Flash;
import com.studily.util.Log;
import com.studily.util.Validators;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * NotebookLM-style AI chat bound to one note. History persists in MySQL.
 */
@WebServlet(name = "chatServlet", urlPatterns = {"/chat"})
public class ChatServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final ChatDAO chatDAO = new ChatDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));
        if (noteId == null) {
            Flash.error(request, "Choose a note to chat about.");
            redirect(request, response, "/dashboard");
            return;
        }

        Note note;
        List<ChatMessage> history;
        try {
            note = notesDAO.findByIdAndUser(noteId, user.getUserId());
            if (note == null) {
                Flash.error(request, "Note not found.");
                redirect(request, response, "/dashboard");
                return;
            }
            history = chatDAO.findByNote(user.getUserId(), noteId, 50);
        } catch (SQLException e) {
            Log.severe("Chat load failed (note " + noteId + "): " + e.getMessage(), e);
            Flash.error(request, "Could not load the chat.");
            redirect(request, response, "/dashboard");
            return;
        }

        request.setAttribute("note", note);
        request.setAttribute("chatHistory", history);
        request.setAttribute("navActive", "chat");
        render(request, response, "chat.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Integer noteId = Validators.parseIntOrNull(request.getParameter("noteId"));
        String question = Validators.sanitizeText(request.getParameter("question"));

        if (noteId == null || question == null || question.isBlank()) {
            Flash.error(request, "Type a question first.");
            redirect(request, response, "/chat?noteId=" + (noteId == null ? "" : noteId));
            return;
        }
        if (question.length() > 1200) question = question.substring(0, 1200);

        Note note;
        try {
            note = notesDAO.findByIdAndUser(noteId, user.getUserId());
        } catch (SQLException e) {
            Log.severe("Chat note load failed: " + e.getMessage(), e);
            Flash.error(request, "Could not load the note.");
            redirect(request, response, "/dashboard");
            return;
        }
        if (note == null) {
            Flash.error(request, "Note not found.");
            redirect(request, response, "/dashboard");
            return;
        }

        try {
            List<ChatMessage> past = chatDAO.findByNote(user.getUserId(), noteId, 20);
            List<String[]> history = new ArrayList<>();
            for (ChatMessage m : past) history.add(new String[]{m.getRole(), m.getContent()});

            chatDAO.insert(user.getUserId(), noteId, "user", question);

            String answer = AIService.chat(user.getUserId(), note.getTitle(),
                    note.getExtractedText() == null ? "" : note.getExtractedText(),
                    history, question);
            chatDAO.insert(user.getUserId(), noteId, "assistant", answer);
        } catch (AIService.AIServiceException e) {
            Flash.error(request, e.getMessage());
        } catch (SQLException e) {
            Log.severe("Chat persist failed: " + e.getMessage(), e);
            Flash.error(request, "Could not save the conversation.");
        }

        redirect(request, response, "/chat?noteId=" + noteId + "#chat-bottom");
    }
}

package com.studily.controller;

import com.studily.dao.FlashcardDAO;
import com.studily.dao.MCQDAO;
import com.studily.dao.NotesDAO;
import com.studily.model.User;
import com.studily.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Raycast-style global search (Ctrl+K). Returns JSON for the search modal:
 * notes, flashcards, and MCQs matching a query — all ownership-scoped.
 */
@WebServlet(name = "searchServlet", urlPatterns = {"/search"})
public class SearchServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();
    private final FlashcardDAO flashcardDAO = new FlashcardDAO();
    private final MCQDAO mcqDAO = new MCQDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String q = request.getParameter("q");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject out = new JsonObject();
        JsonArray results = new JsonArray();

        if (q != null && q.trim().length() >= 2) {
            String term = q.trim().toLowerCase();
            int capped = Math.min(term.length(), 60);
            try {
                // Notes (title match)
                notesDAO.findAllByUser(user.getUserId()).stream()
                        .filter(n -> n.getTitle() != null && n.getTitle().toLowerCase().contains(term))
                        .limit(5)
                        .forEach(n -> {
                            JsonObject o = new JsonObject();
                            o.addProperty("type", "note");
                            o.addProperty("label", n.getTitle());
                            o.addProperty("sub", "Note");
                            o.addProperty("href", request.getContextPath() + "/summary?noteId=" + n.getNoteId());
                            results.add(o);
                        });

                // Flashcards (question match) — limited scan of recent notes' cards
                for (var n : notesDAO.findRecentByUser(user.getUserId(), 20)) {
                    if (results.size() >= 12) break;
                    for (var c : flashcardDAO.findByNote(n.getNoteId())) {
                        if (results.size() >= 12) break;
                        if (c.getQuestion() != null && c.getQuestion().toLowerCase().contains(term)) {
                            JsonObject o = new JsonObject();
                            o.addProperty("type", "flashcard");
                            o.addProperty("label", clip(c.getQuestion(), 80));
                            o.addProperty("sub", "Flashcard · " + n.getTitle());
                            o.addProperty("href", request.getContextPath() + "/flashcards?noteId=" + n.getNoteId());
                            results.add(o);
                        }
                    }
                }

                // MCQs (question match)
                for (var n : notesDAO.findRecentByUser(user.getUserId(), 20)) {
                    if (results.size() >= 15) break;
                    for (var m : mcqDAO.findByNote(n.getNoteId(), null)) {
                        if (results.size() >= 15) break;
                        if (m.getQuestion() != null && m.getQuestion().toLowerCase().contains(term)) {
                            JsonObject o = new JsonObject();
                            o.addProperty("type", "quiz");
                            o.addProperty("label", clip(m.getQuestion(), 80));
                            o.addProperty("sub", "MCQ · " + n.getTitle());
                            o.addProperty("href", request.getContextPath() + "/quiz?noteId=" + n.getNoteId());
                            results.add(o);
                        }
                    }
                }
            } catch (SQLException e) {
                Log.severe("Search failed: " + e.getMessage(), e);
                out.addProperty("error", "Search failed.");
            }
        }

        out.add("results", results);
        response.getWriter().write(out.toString());
    }

    private String clip(String s, int max) {
        return s == null ? "" : (s.length() <= max ? s : s.substring(0, max)) ;
    }
}

package com.studily.controller;

import com.google.gson.Gson;
import com.studily.dao.NotesDAO;
import com.studily.model.Note;
import com.studily.model.StudyKit;
import com.studily.model.User;
import com.studily.util.Flash;
import com.studily.util.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Audio revision: lists the user's notes and exposes the text each one can be
 * read aloud from (summary + extracted notes). Playback itself is done in the
 * browser with the Web Speech API — no audio is generated server-side.
 */
@WebServlet(name = "audioServlet", urlPatterns = {"/audio"})
public class AudioServlet extends BaseAppServlet {

    /** Keep the page payload reasonable; long notes are read from the first part. */
    private static final int MAX_NOTES_CHARS = 6000;

    private final NotesDAO notesDAO = new NotesDAO();
    private final Gson gson = new Gson();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);

        List<Map<String, Object>> tracks = new ArrayList<>();
        try {
            List<Note> notes = notesDAO.findAllByUser(user.getUserId());
            for (Note note : notes) {
                Map<String, Object> track = new LinkedHashMap<>();
                track.put("id", note.getNoteId());
                track.put("title", note.getTitle());
                track.put("date", note.getCreatedAt() == null ? "" : note.getCreatedAt().format(FMT));

                String summary = extractSummary(note.getSummaryJson());
                track.put("summary", summary);
                track.put("hasSummary", summary != null && !summary.isBlank());

                String notesText = clip(note.getExtractedText(), MAX_NOTES_CHARS);
                track.put("notes", notesText);
                track.put("hasNotes", notesText != null && !notesText.isBlank());

                tracks.add(track);
            }
            request.setAttribute("tracks", tracks);
            request.setAttribute("tracksJson", gson.toJson(tracks));
            request.setAttribute("navActive", "audio");
        } catch (SQLException e) {
            Log.severe("Audio page load failed for user " + user.getUserId() + ": " + e.getMessage(), e);
            Flash.error(request, "Could not load your notes for audio playback.");
            render(request, response, "error.jsp");
            return;
        }
        render(request, response, "audio.jsp");
    }

    /** Pull the readable summary paragraph out of the stored summary JSON. */
    private String extractSummary(String summaryJson) {
        if (summaryJson == null || summaryJson.isBlank()) return null;
        try {
            StudyKit.SummaryBundle bundle = gson.fromJson(summaryJson, StudyKit.SummaryBundle.class);
            return bundle == null ? null : bundle.getSummary();
        } catch (Exception e) {
            return null;
        }
    }

    private String clip(String text, int max) {
        if (text == null) return null;
        return text.length() <= max ? text : text.substring(0, max);
    }
}

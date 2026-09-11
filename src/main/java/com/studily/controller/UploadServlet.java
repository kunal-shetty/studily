package com.studily.controller;

import com.studily.dao.NotesDAO;
import com.studily.model.User;
import com.studily.service.PDFService;
import com.studily.util.Flash;
import com.studily.util.Log;
import com.studily.util.Validators;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Upload: accepts a PDF (validated) or pasted text and stores the note
 * with pending AI generation.
 */
@WebServlet(name = "uploadServlet", urlPatterns = {"/upload"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB in memory
        maxFileSize = 10L * 1024 * 1024,      // 10 MB per file
        maxRequestSize = 60L * 1024 * 1024    // 60 MB per request (multi-file queue)
)
public class UploadServlet extends BaseAppServlet {

    private final NotesDAO notesDAO = new NotesDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        render(request, response, "upload.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String title = request.getParameter("title");
        String pastedText = request.getParameter("noteText");
        Collection<Part> fileParts;
        try {
            fileParts = request.getParts();
        } catch (ServletException e) {
            fileParts = List.of();
        }

        // Collect every uploaded PDF (upload queue: multiple files at once).
        List<Part> pdfs = new ArrayList<>();
        for (Part p : fileParts) {
            if ("pdfFile".equals(p.getName()) && p.getSize() > 0
                    && p.getSubmittedFileName() != null && !p.getSubmittedFileName().isBlank()) {
                pdfs.add(p);
            }
        }

        boolean hasFiles = !pdfs.isEmpty();
        boolean hasText = pastedText != null && !pastedText.isBlank();

        if (!Validators.isValidTitle(title)) {
            Flash.error(request, "Please provide a valid title (2-200 characters).");
            redirect(request, response, "/upload");
            return;
        }
        if (!hasFiles && !hasText) {
            Flash.error(request, "Upload a PDF (or several) or paste your notes text.");
            redirect(request, response, "/upload");
            return;
        }

        // --- Multi-file queue: each PDF becomes its own note, titled "<title> — <filename>".
        if (hasFiles) {
            List<Integer> queuedIds = new ArrayList<>();
            List<String> failedNames = new ArrayList<>();
            String baseTitle = title.trim();
            for (Part filePart : pdfs) {
                String submittedName = filePart.getSubmittedFileName();
                String lowerName = submittedName.toLowerCase();
                if (!lowerName.endsWith(".pdf")) {
                    failedNames.add(submittedName + " (not a PDF)");
                    continue;
                }
                if (filePart.getSize() > PDFService.getMaxUploadBytes()) {
                    failedNames.add(submittedName + " (over 10 MB)");
                    continue;
                }
                try (var input = filePart.getInputStream()) {
                    byte[] header = input.readNBytes(5);
                    if (!new String(header, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) {
                        failedNames.add(submittedName + " (invalid PDF)");
                        continue;
                    }
                }
                try {
                    Path uploadDir = Paths.get(System.getProperty("studily.upload.dir",
                            System.getenv("STUDILY_UPLOAD_DIR") != null
                                    ? System.getenv("STUDILY_UPLOAD_DIR")
                                    : "studily-uploads"));
                    Files.createDirectories(uploadDir);
                    String safeName = "note_" + user.getUserId() + "_" + System.currentTimeMillis()
                            + "_" + queuedIds.size() + ".pdf";
                    File target = uploadDir.resolve(safeName).toFile();
                    filePart.write(target.getAbsolutePath());

                    String extractedText;
                    try {
                        extractedText = PDFService.extractText(target);
                    } catch (IOException | IllegalArgumentException e) {
                        target.delete();
                        failedNames.add(submittedName + " (unreadable)");
                        continue;
                    }
                    // Derive per-file title: base for the first, base — filename for the rest.
                    String noteTitle = queuedIds.isEmpty()
                            ? baseTitle
                            : baseTitle + " — " + submittedName.replaceAll("(?i)\\.pdf$", "");
                    int noteId = notesDAO.insert(user.getUserId(), noteTitle, target.getAbsolutePath(), extractedText);
                    queuedIds.add(noteId);
                } catch (SQLException e) {
                    Log.severe("Upload DB error for user " + user.getUserId() + ": " + e.getMessage(), e);
                    failedNames.add(submittedName + " (database error)");
                }
            }

            if (!queuedIds.isEmpty()) {
                Flash.success(request, "📥 " + queuedIds.size() + " note"
                        + (queuedIds.size() == 1 ? "" : "s") + " queued for AI generation.");
            }
            if (!failedNames.isEmpty()) {
                Flash.error(request, "Skipped: " + String.join(", ", failedNames));
            }
            if (queuedIds.isEmpty()) {
                redirect(request, response, "/upload");
            } else {
                // Generate the first one now; the rest can be generated from history.
                redirect(request, response, "/generate-notes?noteId=" + queuedIds.get(0));
            }
            return;
        }

        // --- Single pasted-text flow ---
        String extractedText = Validators.sanitizeText(pastedText);
        if (extractedText == null || extractedText.length() < 100) {
            Flash.error(request, "Pasted notes must be at least 100 characters for good AI results.");
            redirect(request, response, "/upload");
            return;
        }

        try {
            int noteId = notesDAO.insert(user.getUserId(), title.trim(), null, extractedText);
            Flash.success(request, "Note saved. Generating your study material…");
            redirect(request, response, "/generate-notes?noteId=" + noteId);
        } catch (SQLException e) {
            Log.severe("Upload DB error for user " + user.getUserId() + ": " + e.getMessage(), e);
            Flash.error(request, "Database error while saving the note. Please retry.");
            redirect(request, response, "/upload");
        }
    }
}

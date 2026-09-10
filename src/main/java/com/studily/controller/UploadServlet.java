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

/**
 * Upload: accepts a PDF (validated) or pasted text and stores the note
 * with pending AI generation.
 */
@WebServlet(name = "uploadServlet", urlPatterns = {"/upload"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB in memory
        maxFileSize = 10L * 1024 * 1024,      // 10 MB per file
        maxRequestSize = 12L * 1024 * 1024    // 12 MB per request
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
        Part filePart = null;
        try {
            filePart = request.getPart("pdfFile");
        } catch (ServletException ignored) {
            // No file part or bad multipart: handled by validation below.
        }

        boolean hasFile = filePart != null && filePart.getSize() > 0
                && filePart.getSubmittedFileName() != null
                && !filePart.getSubmittedFileName().isBlank();
        boolean hasText = pastedText != null && !pastedText.isBlank();

        if (!Validators.isValidTitle(title)) {
            Flash.error(request, "Please provide a valid title (2-200 characters).");
            redirect(request, response, "/upload");
            return;
        }

        if (!hasFile && !hasText) {
            Flash.error(request, "Upload a PDF or paste your notes text.");
            redirect(request, response, "/upload");
            return;
        }

        String extractedText;
        String storedPdfPath = null;

        if (hasFile) {
            // --- File validation (never trust client-side validation) ---
            String submittedName = filePart.getSubmittedFileName();
            String lowerName = submittedName.toLowerCase();
            if (!lowerName.endsWith(".pdf")) {
                Flash.error(request, "Only PDF files are supported.");
                redirect(request, response, "/upload");
                return;
            }
            if (filePart.getSize() > PDFService.getMaxUploadBytes()) {
                Flash.error(request, "PDF exceeds the 10 MB size limit.");
                redirect(request, response, "/upload");
                return;
            }
            try (var input = filePart.getInputStream()) {
                byte[] header = input.readNBytes(5);
                if (!new String(header, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) {
                    Flash.error(request, "This file is not a valid PDF.");
                    redirect(request, response, "/upload");
                    return;
                }
            }

            // --- Secure storage ---
            Path uploadDir = Paths.get(System.getProperty("studily.upload.dir",
                    System.getenv("STUDILY_UPLOAD_DIR") != null
                            ? System.getenv("STUDILY_UPLOAD_DIR")
                            : "studily-uploads"));
            Files.createDirectories(uploadDir);
            String safeName = "note_" + user.getUserId() + "_" + System.currentTimeMillis() + ".pdf";
            File target = uploadDir.resolve(safeName).toFile();
            filePart.write(target.getAbsolutePath());
            storedPdfPath = target.getAbsolutePath();

            try {
                extractedText = PDFService.extractText(target);
            } catch (IOException e) {
                target.delete();
                Flash.error(request, "Could not read this PDF: " + e.getMessage());
                redirect(request, response, "/upload");
                return;
            } catch (IllegalArgumentException e) {
                target.delete();
                Flash.error(request, e.getMessage());
                redirect(request, response, "/upload");
                return;
            }
        } else {
            extractedText = Validators.sanitizeText(pastedText);
            if (extractedText == null || extractedText.length() < 100) {
                Flash.error(request, "Pasted notes must be at least 100 characters for good AI results.");
                redirect(request, response, "/upload");
                return;
            }
        }

        // --- Persist note ---
        try {
            int noteId = notesDAO.insert(user.getUserId(), title.trim(), storedPdfPath, extractedText);
            Flash.success(request, "Note saved. Generating your study material…");
            redirect(request, response, "/generate-notes?noteId=" + noteId);
        } catch (SQLException e) {
            Log.severe("Upload DB error for user " + user.getUserId() + ": " + e.getMessage(), e);
            Flash.error(request, "Database error while saving the note. Please retry.");
            redirect(request, response, "/upload");
        }
    }
}

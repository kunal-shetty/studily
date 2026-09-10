package com.studily.model;

import java.time.LocalDateTime;

/**
 * An uploaded study note: pasted text or a PDF whose text was extracted
 * with PDFBox, plus the AI-generated summary stored as JSON.
 */
public class Note {

    private int noteId;
    private int userId;
    private String title;
    private String pdfPath;
    private String extractedText;
    private String summaryJson;
    private LocalDateTime createdAt;

    public Note() {
    }

    public int getNoteId() { return noteId; }
    public void setNoteId(int noteId) { this.noteId = noteId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getSummaryJson() { return summaryJson; }
    public void setSummaryJson(String summaryJson) { this.summaryJson = summaryJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

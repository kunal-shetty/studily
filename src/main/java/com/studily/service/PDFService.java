package com.studily.service;

import com.studily.util.AppConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

/**
 * Extracts plain text from uploaded PDFs using Apache PDFBox 3.x.
 */
public final class PDFService {

    private PDFService() {
    }

    /**
     * Extract text from a PDF file on disk.
     *
     * @throws IOException if the file is corrupt, encrypted, or unreadable
     * @throws IllegalArgumentException if the PDF contains no extractable text
     */
    public static String extractText(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (document.isEncrypted()) {
                throw new IOException("This PDF is password-protected and cannot be read.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text found. The PDF may be a scanned image (OCR is not supported yet).");
            }
            return text.trim();
        }
    }

    public static int getMaxUploadBytes() {
        return AppConfig.getInt("upload.max.size.bytes", 10 * 1024 * 1024);
    }
}

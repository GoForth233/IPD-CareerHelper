package com.group1.career.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Lightweight PDF utility for extracting plain text from raw PDF bytes.
 * Uses Apache PDFBox 3.x (Loader API). The bytes are typically obtained
 * via FileService.downloadBytes (authenticated OSS GET) since the bucket
 * is private and anonymous URL access returns AccessDenied.
 */
@Slf4j
public final class PdfTextExtractor {

    private static final int MAX_TEXT_CHARS = 20_000; // Cap to avoid blowing up AI token budget

    private PdfTextExtractor() {}

    /**
     * Extract plain text from raw PDF bytes.
     * @return extracted text, never null (empty string on failure)
     */
    public static String extractFromBytes(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) return "";
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            String text = new PDFTextStripper().getText(doc);
            if (text == null) return "";
            if (text.length() > MAX_TEXT_CHARS) {
                log.info("PDF text truncated from {} -> {} chars", text.length(), MAX_TEXT_CHARS);
                return text.substring(0, MAX_TEXT_CHARS);
            }
            return text;
        } catch (Exception e) {
            log.error("PDF text extraction failed: {}", e.getMessage(), e);
            return "";
        }
    }
}

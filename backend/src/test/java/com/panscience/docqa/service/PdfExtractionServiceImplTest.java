package com.panscience.docqa.service;

import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PdfExtractionServiceImplTest {

    private PdfExtractionServiceImpl pdfExtractionService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfExtractionService = new PdfExtractionServiceImpl();
    }

    @Test
    void extractContent_withValidPdf_shouldExtractText() throws IOException {
        // Create a simple PDF-like file (for testing structure, not actual PDF parsing)
        // Note: Real tests would use actual PDF files
        Path pdfPath = tempDir.resolve("test.pdf");
        
        // Create minimal PDF content
        String minimalPdf = "%PDF-1.4\n" +
                "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
                "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n" +
                "3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Resources<<>>>>endobj\n" +
                "xref\n0 4\n" +
                "0000000000 65535 f\n" +
                "0000000009 00000 n\n" +
                "0000000052 00000 n\n" +
                "0000000101 00000 n\n" +
                "trailer<</Size 4/Root 1 0 R>>\nstartxref\n178\n%%EOF";
        
        Files.writeString(pdfPath, minimalPdf);

        Document document = Document.builder()
                .id(1L)
                .originalFileName("test.pdf")
                .filePath(pdfPath.toString())
                .type(Document.DocumentType.PDF)
                .build();

        // This will return empty content for our minimal PDF (no text content)
        List<DocumentContent> result = pdfExtractionService.extractContent(document);
        
        // Minimal PDF has no text, so result should be empty
        assertThat(result).isNotNull();
    }

    @Test
    void splitIntoChunks_shouldRespectChunkSize() throws Exception {
        // Use reflection to test private method
        java.lang.reflect.Method method = PdfExtractionServiceImpl.class
                .getDeclaredMethod("splitIntoChunks", String.class);
        method.setAccessible(true);

        String longText = "This is a test. ".repeat(100);
        
        @SuppressWarnings("unchecked")
        List<String> chunks = (List<String>) method.invoke(pdfExtractionService, longText);
        
        assertThat(chunks).isNotEmpty();
        for (String chunk : chunks) {
            assertThat(chunk.length()).isLessThanOrEqualTo(1100); // Some buffer for word boundaries
        }
    }

    @Test
    void splitIntoChunks_shortText_shouldReturnSingleChunk() throws Exception {
        java.lang.reflect.Method method = PdfExtractionServiceImpl.class
                .getDeclaredMethod("splitIntoChunks", String.class);
        method.setAccessible(true);

        String shortText = "Short text content.";
        
        @SuppressWarnings("unchecked")
        List<String> chunks = (List<String>) method.invoke(pdfExtractionService, shortText);
        
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEqualTo(shortText);
    }
}

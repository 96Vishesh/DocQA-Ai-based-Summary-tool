package com.panscience.docqa.service;

import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;
import com.panscience.docqa.exception.DocumentProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExtractionServiceImpl implements PdfExtractionService {

    private static final int CHUNK_SIZE = 1000; // characters per chunk

    @Override
    public List<DocumentContent> extractContent(Document document) {
        List<DocumentContent> contents = new ArrayList<>();

        try (PDDocument pdf = Loader.loadPDF(new File(document.getFilePath()))) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = pdf.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(pdf).trim();

                if (pageText.isEmpty()) {
                    continue;
                }

                // Split large pages into chunks
                List<String> chunks = splitIntoChunks(pageText);
                
                for (int i = 0; i < chunks.size(); i++) {
                    DocumentContent content = DocumentContent.builder()
                            .document(document)
                            .content(chunks.get(i))
                            .pageNumber(page)
                            .chunkIndex(contents.size())
                            .build();
                    contents.add(content);
                }
            }

            log.info("Extracted {} content chunks from PDF: {}", contents.size(), document.getOriginalFileName());
            return contents;

        } catch (IOException e) {
            log.error("Failed to extract PDF content: {}", document.getOriginalFileName(), e);
            throw new DocumentProcessingException("Failed to extract PDF content", e);
        }
    }

    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        
        if (text.length() <= CHUNK_SIZE) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            
            // Try to break at a sentence or word boundary
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastSpace = text.lastIndexOf(' ', end);
                
                if (lastPeriod > start + CHUNK_SIZE / 2) {
                    end = lastPeriod + 1;
                } else if (lastSpace > start + CHUNK_SIZE / 2) {
                    end = lastSpace;
                }
            }

            chunks.add(text.substring(start, end).trim());
            start = end;
        }

        return chunks;
    }
}

package com.panscience.docqa.controller;

import com.panscience.docqa.dto.DocumentDto;
import com.panscience.docqa.dto.TimestampResponse;
import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;
import com.panscience.docqa.repository.DocumentContentRepository;
import com.panscience.docqa.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentContentRepository documentContentRepository;

    @PostMapping("/upload")
    public ResponseEntity<DocumentDto> uploadDocument(@RequestParam("file") MultipartFile file) {
        DocumentDto document = documentService.uploadDocument(file);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocument(id));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByType(@PathVariable Document.DocumentType type) {
        return ResponseEntity.ok(documentService.getDocumentsByType(type));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<SummaryResponse> getSummary(@PathVariable Long id) {
        DocumentDto document = documentService.getDocument(id);
        return ResponseEntity.ok(new SummaryResponse(document.getId(), document.getSummary()));
    }

    @GetMapping("/{id}/timestamps")
    public ResponseEntity<TimestampResponse> getTimestamps(@PathVariable Long id) {
        List<DocumentContent> contents = documentContentRepository.findTimestampedContentByDocumentId(id);
        
        List<TimestampResponse.TimestampEntry> entries = contents.stream()
                .map(content -> TimestampResponse.TimestampEntry.builder()
                        .startTime(content.getStartTime())
                        .endTime(content.getEndTime())
                        .formattedStartTime(formatTime(content.getStartTime()))
                        .formattedEndTime(formatTime(content.getEndTime()))
                        .topic(extractTopic(content.getContent()))
                        .content(content.getContent())
                        .build())
                .toList();

        return ResponseEntity.ok(TimestampResponse.builder()
                .documentId(id)
                .timestamps(entries)
                .build());
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> getDocumentContent(@PathVariable Long id) {
        byte[] content = documentService.getDocumentContent(id);
        String mimeType = documentService.getDocumentMimeType(id);
        DocumentDto doc = documentService.getDocument(id);

        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamMedia(@PathVariable Long id) {
        byte[] content = documentService.getDocumentContent(id);
        String mimeType = documentService.getDocumentMimeType(id);

        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }

    private String formatTime(Double seconds) {
        if (seconds == null) return "00:00";
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d", mins, secs);
    }

    private String extractTopic(String content) {
        // Extract first sentence as topic
        if (content == null) return "";
        int endIndex = content.indexOf('.');
        if (endIndex > 0 && endIndex < 100) {
            return content.substring(0, endIndex + 1);
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }

    public record SummaryResponse(Long documentId, String summary) {}
}

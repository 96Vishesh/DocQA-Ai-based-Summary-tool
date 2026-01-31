package com.panscience.docqa.service;

import com.panscience.docqa.dto.DocumentDto;
import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;
import com.panscience.docqa.exception.DocumentNotFoundException;
import com.panscience.docqa.exception.FileStorageException;
import com.panscience.docqa.repository.DocumentContentRepository;
import com.panscience.docqa.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final PdfExtractionService pdfExtractionService;
    private final TranscriptionService transcriptionService;
    private final SummaryService summaryService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public DocumentDto uploadDocument(MultipartFile file) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
        Document.DocumentType type = determineDocumentType(file.getContentType());

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Document document = Document.builder()
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .type(type)
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(targetLocation.toString())
                    .status(Document.ProcessingStatus.PENDING)
                    .build();

            document = documentRepository.save(document);
            
            // Process document asynchronously
            processDocumentAsync(document.getId());

            return DocumentDto.fromEntity(document);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFileName, e);
        }
    }

    @Async
    @Transactional
    public void processDocumentAsync(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        try {
            document.setStatus(Document.ProcessingStatus.PROCESSING);
            documentRepository.save(document);

            List<DocumentContent> contents;
            
            switch (document.getType()) {
                case PDF:
                    contents = pdfExtractionService.extractContent(document);
                    break;
                case AUDIO:
                case VIDEO:
                    contents = transcriptionService.transcribe(document);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported document type: " + document.getType());
            }

            documentContentRepository.saveAll(contents);

            // Generate summary
            String fullContent = contents.stream()
                    .map(DocumentContent::getContent)
                    .reduce("", (a, b) -> a + "\n" + b);
            
            String summary = summaryService.generateSummary(fullContent);
            document.setSummary(summary);
            document.setStatus(Document.ProcessingStatus.COMPLETED);
            document.setProcessedAt(LocalDateTime.now());
            documentRepository.save(document);

            log.info("Successfully processed document: {}", document.getOriginalFileName());
        } catch (Exception e) {
            log.error("Failed to process document: {}", document.getOriginalFileName(), e);
            document.setStatus(Document.ProcessingStatus.FAILED);
            documentRepository.save(document);
        }
    }

    @Override
    public DocumentDto getDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
        return DocumentDto.fromEntity(document);
    }

    @Override
    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(DocumentDto::fromEntity)
                .toList();
    }

    @Override
    public List<DocumentDto> getDocumentsByType(Document.DocumentType type) {
        return documentRepository.findByType(type).stream()
                .map(DocumentDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", document.getFilePath(), e);
        }

        documentContentRepository.deleteByDocumentId(id);
        documentRepository.delete(document);
    }

    @Override
    public byte[] getDocumentContent(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        try {
            return Files.readAllBytes(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            throw new FileStorageException("Failed to read file: " + document.getFilePath(), e);
        }
    }

    @Override
    public String getDocumentMimeType(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
        return document.getMimeType();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileStorageException("Cannot determine file type");
        }

        if (!isValidContentType(contentType)) {
            throw new FileStorageException("Unsupported file type: " + contentType);
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.startsWith("audio/") ||
               contentType.startsWith("video/");
    }

    private Document.DocumentType determineDocumentType(String contentType) {
        if (contentType.equals("application/pdf")) {
            return Document.DocumentType.PDF;
        } else if (contentType.startsWith("audio/")) {
            return Document.DocumentType.AUDIO;
        } else if (contentType.startsWith("video/")) {
            return Document.DocumentType.VIDEO;
        }
        throw new FileStorageException("Unsupported content type: " + contentType);
    }
}

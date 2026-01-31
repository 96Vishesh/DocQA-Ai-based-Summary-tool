package com.panscience.docqa.service;

import com.panscience.docqa.dto.DocumentDto;
import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;
import com.panscience.docqa.exception.DocumentNotFoundException;
import com.panscience.docqa.exception.FileStorageException;
import com.panscience.docqa.repository.DocumentContentRepository;
import com.panscience.docqa.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentContentRepository documentContentRepository;

    @Mock
    private PdfExtractionService pdfExtractionService;

    @Mock
    private TranscriptionService transcriptionService;

    @Mock
    private SummaryService summaryService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @TempDir
    Path tempDir;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());

        testDocument = Document.builder()
                .id(1L)
                .fileName("test-file.pdf")
                .originalFileName("test.pdf")
                .type(Document.DocumentType.PDF)
                .mimeType("application/pdf")
                .fileSize(1024L)
                .filePath(tempDir.resolve("test-file.pdf").toString())
                .status(Document.ProcessingStatus.COMPLETED)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void uploadDocument_withPdf_shouldSucceed() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes()
        );

        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        DocumentDto result = documentService.uploadDocument(file);

        assertThat(result).isNotNull();
        assertThat(result.getOriginalFileName()).isEqualTo("test.pdf");
        assertThat(result.getType()).isEqualTo(Document.DocumentType.PDF);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void uploadDocument_withAudio_shouldSucceed() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp3", "audio/mpeg", "audio content".getBytes()
        );

        Document audioDoc = Document.builder()
                .id(2L)
                .originalFileName("test.mp3")
                .type(Document.DocumentType.AUDIO)
                .build();

        when(documentRepository.save(any(Document.class))).thenReturn(audioDoc);

        DocumentDto result = documentService.uploadDocument(file);

        assertThat(result.getType()).isEqualTo(Document.DocumentType.AUDIO);
    }

    @Test
    void uploadDocument_withVideo_shouldSucceed() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "video content".getBytes()
        );

        Document videoDoc = Document.builder()
                .id(3L)
                .originalFileName("test.mp4")
                .type(Document.DocumentType.VIDEO)
                .build();

        when(documentRepository.save(any(Document.class))).thenReturn(videoDoc);

        DocumentDto result = documentService.uploadDocument(file);

        assertThat(result.getType()).isEqualTo(Document.DocumentType.VIDEO);
    }

    @Test
    void uploadDocument_withEmptyFile_shouldThrowException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );

        assertThatThrownBy(() -> documentService.uploadDocument(emptyFile))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void uploadDocument_withUnsupportedType_shouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes()
        );

        assertThatThrownBy(() -> documentService.uploadDocument(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Unsupported");
    }

    @Test
    void getDocument_existingId_shouldReturnDocument() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        DocumentDto result = documentService.getDocument(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getDocument_nonExistingId_shouldThrowException() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(999L))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void getAllDocuments_shouldReturnList() {
        when(documentRepository.findAll()).thenReturn(List.of(testDocument));

        List<DocumentDto> result = documentService.getAllDocuments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOriginalFileName()).isEqualTo("test.pdf");
    }

    @Test
    void getAllDocuments_empty_shouldReturnEmptyList() {
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        List<DocumentDto> result = documentService.getAllDocuments();

        assertThat(result).isEmpty();
    }

    @Test
    void getDocumentsByType_shouldFilterByType() {
        when(documentRepository.findByType(Document.DocumentType.PDF))
                .thenReturn(List.of(testDocument));

        List<DocumentDto> result = documentService.getDocumentsByType(Document.DocumentType.PDF);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(Document.DocumentType.PDF);
    }

    @Test
    void deleteDocument_existingId_shouldDelete() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        documentService.deleteDocument(1L);

        verify(documentContentRepository).deleteByDocumentId(1L);
        verify(documentRepository).delete(testDocument);
    }

    @Test
    void deleteDocument_nonExistingId_shouldThrowException() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteDocument(999L))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void getDocumentMimeType_shouldReturnMimeType() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        String mimeType = documentService.getDocumentMimeType(1L);

        assertThat(mimeType).isEqualTo("application/pdf");
    }
}

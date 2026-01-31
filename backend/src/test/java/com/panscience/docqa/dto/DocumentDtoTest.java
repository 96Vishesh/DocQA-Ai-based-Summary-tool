package com.panscience.docqa.dto;

import com.panscience.docqa.entity.Document;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class DocumentDtoTest {

    @Test
    void fromEntity_shouldMapAllFields() {
        Document document = Document.builder()
                .id(1L)
                .fileName("test-uuid.pdf")
                .originalFileName("test.pdf")
                .type(Document.DocumentType.PDF)
                .mimeType("application/pdf")
                .fileSize(1024L)
                .summary("Test summary")
                .uploadedAt(LocalDateTime.of(2024, 1, 1, 12, 0))
                .processedAt(LocalDateTime.of(2024, 1, 1, 12, 5))
                .status(Document.ProcessingStatus.COMPLETED)
                .build();

        DocumentDto dto = DocumentDto.fromEntity(document);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFileName()).isEqualTo("test-uuid.pdf");
        assertThat(dto.getOriginalFileName()).isEqualTo("test.pdf");
        assertThat(dto.getType()).isEqualTo(Document.DocumentType.PDF);
        assertThat(dto.getMimeType()).isEqualTo("application/pdf");
        assertThat(dto.getFileSize()).isEqualTo(1024L);
        assertThat(dto.getSummary()).isEqualTo("Test summary");
        assertThat(dto.getStatus()).isEqualTo(Document.ProcessingStatus.COMPLETED);
    }

    @Test
    void builder_shouldCreateDto() {
        DocumentDto dto = DocumentDto.builder()
                .id(1L)
                .fileName("test.pdf")
                .type(Document.DocumentType.PDF)
                .build();

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void fromEntity_withNullFields_shouldHandleGracefully() {
        Document document = Document.builder()
                .id(1L)
                .fileName("test.pdf")
                .originalFileName("test.pdf")
                .type(Document.DocumentType.AUDIO)
                .mimeType("audio/mpeg")
                .fileSize(500L)
                .filePath("/tmp/test.mp3")
                .status(Document.ProcessingStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                // summary and processedAt are null
                .build();

        DocumentDto dto = DocumentDto.fromEntity(document);

        assertThat(dto.getSummary()).isNull();
        assertThat(dto.getProcessedAt()).isNull();
    }
}

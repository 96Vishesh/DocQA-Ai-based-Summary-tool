package com.panscience.docqa.repository;

import com.panscience.docqa.entity.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        testDocument = Document.builder()
                .fileName("test-file.pdf")
                .originalFileName("test.pdf")
                .type(Document.DocumentType.PDF)
                .mimeType("application/pdf")
                .fileSize(1024L)
                .filePath("/tmp/test.pdf")
                .status(Document.ProcessingStatus.COMPLETED)
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(testDocument);
    }

    @Test
    void findByStatus_shouldReturnMatchingDocuments() {
        List<Document> results = documentRepository.findByStatus(Document.ProcessingStatus.COMPLETED);
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(Document.ProcessingStatus.COMPLETED);
    }

    @Test
    void findByType_shouldReturnMatchingDocuments() {
        List<Document> results = documentRepository.findByType(Document.DocumentType.PDF);
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo(Document.DocumentType.PDF);
    }

    @Test
    void findAllProcessedDocuments_shouldReturnCompletedDocuments() {
        Document pendingDoc = Document.builder()
                .fileName("pending.pdf")
                .originalFileName("pending.pdf")
                .type(Document.DocumentType.PDF)
                .mimeType("application/pdf")
                .fileSize(512L)
                .filePath("/tmp/pending.pdf")
                .status(Document.ProcessingStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(pendingDoc);

        List<Document> results = documentRepository.findAllProcessedDocuments();
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(Document.ProcessingStatus.COMPLETED);
    }

    @Test
    void findByFileNameContainingIgnoreCase_shouldFindMatchingDocuments() {
        List<Document> results = documentRepository.findByFileNameContainingIgnoreCase("test");
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFileName()).contains("test");
    }

    @Test
    void save_shouldPersistDocument() {
        Document newDoc = Document.builder()
                .fileName("new-file.pdf")
                .originalFileName("new.pdf")
                .type(Document.DocumentType.PDF)
                .mimeType("application/pdf")
                .fileSize(2048L)
                .filePath("/tmp/new.pdf")
                .status(Document.ProcessingStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                .build();

        Document saved = documentRepository.save(newDoc);

        assertThat(saved.getId()).isNotNull();
        assertThat(entityManager.find(Document.class, saved.getId())).isNotNull();
    }

    @Test
    void delete_shouldRemoveDocument() {
        documentRepository.delete(testDocument);
        entityManager.flush();

        Document found = entityManager.find(Document.class, testDocument.getId());
        assertThat(found).isNull();
    }
}

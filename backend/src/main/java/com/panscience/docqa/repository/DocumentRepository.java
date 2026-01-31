package com.panscience.docqa.repository;

import com.panscience.docqa.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByStatus(Document.ProcessingStatus status);

    List<Document> findByType(Document.DocumentType type);

    @Query("SELECT d FROM Document d WHERE d.status = 'COMPLETED' ORDER BY d.uploadedAt DESC")
    List<Document> findAllProcessedDocuments();

    List<Document> findByFileNameContainingIgnoreCase(String fileName);
}

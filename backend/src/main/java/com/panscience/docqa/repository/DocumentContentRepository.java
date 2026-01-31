package com.panscience.docqa.repository;

import com.panscience.docqa.entity.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentContentRepository extends JpaRepository<DocumentContent, Long> {

    List<DocumentContent> findByDocumentIdOrderByChunkIndex(Long documentId);

    @Query("SELECT dc FROM DocumentContent dc WHERE dc.document.id = :documentId " +
           "AND dc.startTime IS NOT NULL ORDER BY dc.startTime")
    List<DocumentContent> findTimestampedContentByDocumentId(@Param("documentId") Long documentId);

    @Query("SELECT dc FROM DocumentContent dc WHERE dc.document.id = :documentId " +
           "AND dc.content LIKE %:keyword% ORDER BY dc.chunkIndex")
    List<DocumentContent> searchByKeyword(@Param("documentId") Long documentId, 
                                          @Param("keyword") String keyword);

    void deleteByDocumentId(Long documentId);
}

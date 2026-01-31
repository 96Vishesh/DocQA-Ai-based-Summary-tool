package com.panscience.docqa.repository;

import com.panscience.docqa.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<ChatMessage> findByDocumentIdOrderByCreatedAtDesc(Long documentId);

    void deleteBySessionId(String sessionId);

    void deleteByDocumentId(Long documentId);
}

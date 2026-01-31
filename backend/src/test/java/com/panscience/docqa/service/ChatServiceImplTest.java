package com.panscience.docqa.service;

import com.panscience.docqa.dto.ChatRequest;
import com.panscience.docqa.dto.ChatResponse;
import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;
import com.panscience.docqa.exception.DocumentNotFoundException;
import com.panscience.docqa.repository.ChatMessageRepository;
import com.panscience.docqa.repository.DocumentContentRepository;
import com.panscience.docqa.repository.DocumentRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentContentRepository documentContentRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private VectorSearchService vectorSearchService;

    @Mock
    private ChatLanguageModel chatModel;

    @InjectMocks
    private ChatServiceImpl chatService;

    private Document testDocument;
    private DocumentContent testContent;

    @BeforeEach
    void setUp() {
        testDocument = Document.builder()
                .id(1L)
                .originalFileName("test.pdf")
                .type(Document.DocumentType.PDF)
                .status(Document.ProcessingStatus.COMPLETED)
                .build();

        testContent = DocumentContent.builder()
                .id(1L)
                .document(testDocument)
                .content("This is test content about machine learning.")
                .chunkIndex(0)
                .build();

        // Inject mock ChatModel
        ReflectionTestUtils.setField(chatService, "chatModel", chatModel);
    }

    @Test
    void chat_withValidRequest_shouldReturnResponse() {
        ChatRequest request = ChatRequest.builder()
                .documentId(1L)
                .message("What is this about?")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(vectorSearchService.search(anyLong(), anyString(), anyInt()))
                .thenReturn(List.of(testContent));
        when(chatModel.generate(anyString()))
                .thenReturn("This document is about machine learning.");
        when(chatMessageRepository.save(any())).thenReturn(null);

        ChatResponse response = chatService.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.getResponse()).isEqualTo("This document is about machine learning.");
        assertThat(response.getSessionId()).isNotNull();
    }

    @Test
    void chat_withNonExistingDocument_shouldThrowException() {
        ChatRequest request = ChatRequest.builder()
                .documentId(999L)
                .message("Test message")
                .build();

        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.chat(request))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void chat_withExistingSessionId_shouldMaintainSession() {
        ChatRequest request = ChatRequest.builder()
                .documentId(1L)
                .message("Follow-up question")
                .sessionId("existing-session-123")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(vectorSearchService.search(anyLong(), anyString(), anyInt()))
                .thenReturn(List.of(testContent));
        when(chatModel.generate(anyString())).thenReturn("Follow-up response");
        when(chatMessageRepository.save(any())).thenReturn(null);

        ChatResponse response = chatService.chat(request);

        assertThat(response.getSessionId()).isEqualTo("existing-session-123");
    }

    @Test
    void chat_withAudioDocument_shouldIncludeTimestamps() {
        testDocument = Document.builder()
                .id(2L)
                .originalFileName("test.mp3")
                .type(Document.DocumentType.AUDIO)
                .status(Document.ProcessingStatus.COMPLETED)
                .build();

        DocumentContent audioContent = DocumentContent.builder()
                .id(2L)
                .document(testDocument)
                .content("Content at this timestamp")
                .startTime(30.0)
                .endTime(45.0)
                .chunkIndex(0)
                .build();

        ChatRequest request = ChatRequest.builder()
                .documentId(2L)
                .message("What is discussed at 30 seconds?")
                .build();

        when(documentRepository.findById(2L)).thenReturn(Optional.of(testDocument));
        when(vectorSearchService.search(anyLong(), anyString(), anyInt()))
                .thenReturn(List.of(audioContent));
        when(chatModel.generate(anyString())).thenReturn("At 30 seconds, the topic is...");
        when(chatMessageRepository.save(any())).thenReturn(null);

        ChatResponse response = chatService.chat(request);

        assertThat(response.getTimestamps()).isNotEmpty();
        assertThat(response.getTimestamps().get(0).getStartTime()).isEqualTo(30.0);
    }

    @Test
    void chat_withFallbackToKeywordSearch_shouldWork() {
        ChatRequest request = ChatRequest.builder()
                .documentId(1L)
                .message("Find information about learning")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(vectorSearchService.search(anyLong(), anyString(), anyInt()))
                .thenReturn(List.of()); // Empty vector search result
        when(documentContentRepository.searchByKeyword(anyLong(), anyString()))
                .thenReturn(List.of(testContent));
        when(chatModel.generate(anyString())).thenReturn("Based on keyword search...");
        when(chatMessageRepository.save(any())).thenReturn(null);

        ChatResponse response = chatService.chat(request);

        assertThat(response).isNotNull();
        verify(documentContentRepository).searchByKeyword(anyLong(), anyString());
    }
}

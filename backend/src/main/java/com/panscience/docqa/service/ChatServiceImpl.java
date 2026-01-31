package com.panscience.docqa.service;

import com.panscience.docqa.dto.ChatRequest;
import com.panscience.docqa.dto.ChatResponse;
import com.panscience.docqa.entity.ChatMessage;
import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;
import com.panscience.docqa.exception.DocumentNotFoundException;
import com.panscience.docqa.repository.ChatMessageRepository;
import com.panscience.docqa.repository.DocumentContentRepository;
import com.panscience.docqa.repository.DocumentRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final VectorSearchService vectorSearchService;

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String modelName;

    @Value("${app.mock-ai:true}")
    private boolean useMockAi;

    private ChatLanguageModel chatModel;

    @PostConstruct
    public void init() {
        if (!useMockAi && openaiApiKey != null && !openaiApiKey.contains("placeholder")) {
            try {
                this.chatModel = OpenAiChatModel.builder()
                        .apiKey(openaiApiKey)
                        .modelName(modelName)
                        .maxTokens(2000)
                        .temperature(0.7)
                        .build();
                log.info("OpenAI ChatModel initialized for chat service");
            } catch (Exception e) {
                log.warn("Failed to initialize OpenAI for chat, using mock mode: {}", e.getMessage());
                useMockAi = true;
            }
        } else {
            log.info("Using mock AI mode for chat (no OpenAI cost)");
            useMockAi = true;
        }
    }

    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new DocumentNotFoundException(request.getDocumentId()));

        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        // Get relevant content chunks using vector search or keyword search
        List<DocumentContent> relevantContent = findRelevantContent(document, request.getMessage());

        // Build context from relevant content
        String context = buildContext(relevantContent);

        // Get AI response (mock or real)
        String aiResponse;
        if (useMockAi) {
            aiResponse = generateMockChatResponse(request.getMessage(), document, context);
        } else {
            String prompt = buildPrompt(request.getMessage(), context, document);
            aiResponse = chatModel.generate(prompt);
        }

        // Extract timestamps if applicable
        List<ChatResponse.TimestampReference> timestamps = extractTimestamps(relevantContent, document);

        // Save chat message
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .document(document)
                .userMessage(request.getMessage())
                .aiResponse(aiResponse)
                .timestampReferences(serializeTimestamps(timestamps))
                .build();
        chatMessageRepository.save(chatMessage);

        return ChatResponse.builder()
                .response(aiResponse)
                .sessionId(sessionId)
                .timestamps(timestamps)
                .build();
    }

    private String generateMockChatResponse(String question, Document document, String context) {
        String preview = context.length() > 300 ? context.substring(0, 300) + "..." : context;
        
        return String.format("""
                **Response (Demo Mode)**
                
                You asked: "%s"
                
                **Document:** %s (%s)
                
                **Relevant Content Preview:**
                %s
                
                ---
                *This is a mock response for demo/development purposes. To enable real AI-powered Q&A, configure a valid OpenAI API key with available quota and set `app.mock-ai=false` in application.yml.*
                """, question, document.getOriginalFileName(), document.getType(), preview);
    }


    private List<DocumentContent> findRelevantContent(Document document, String query) {
        // Try vector search first
        List<DocumentContent> results = vectorSearchService.search(document.getId(), query, 5);
        
        if (results.isEmpty()) {
            // Fallback to keyword search
            results = documentContentRepository.searchByKeyword(document.getId(), extractKeywords(query));
        }

        if (results.isEmpty()) {
            // Return all content if no specific matches
            results = documentContentRepository.findByDocumentIdOrderByChunkIndex(document.getId());
        }

        return results;
    }

    private String extractKeywords(String query) {
        // Simple keyword extraction - take first significant word
        String[] words = query.toLowerCase().split("\\s+");
        for (String word : words) {
            if (word.length() > 3 && !isStopWord(word)) {
                return word;
            }
        }
        return query.split("\\s+")[0];
    }

    private boolean isStopWord(String word) {
        List<String> stopWords = List.of("what", "where", "when", "which", "who", "whom", 
                "this", "that", "these", "those", "about", "from", "with", "have", "been");
        return stopWords.contains(word);
    }

    private String buildContext(List<DocumentContent> contents) {
        return contents.stream()
                .map(content -> {
                    StringBuilder sb = new StringBuilder();
                    if (content.getStartTime() != null) {
                        sb.append("[").append(formatTime(content.getStartTime())).append("] ");
                    } else if (content.getPageNumber() != null) {
                        sb.append("[Page ").append(content.getPageNumber()).append("] ");
                    }
                    sb.append(content.getContent());
                    return sb.toString();
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String buildPrompt(String question, String context, Document document) {
        String docType = document.getType().name().toLowerCase();
        
        return """
                You are a helpful assistant that answers questions based on the provided document content.
                The document is a %s file named "%s".
                
                Document Content:
                %s
                
                User Question: %s
                
                Instructions:
                1. Answer based only on the provided document content
                2. If the answer is not in the document, say so clearly
                3. For audio/video content, reference the timestamps when relevant
                4. Be concise but comprehensive
                
                Answer:
                """.formatted(docType, document.getOriginalFileName(), context, question);
    }

    private List<ChatResponse.TimestampReference> extractTimestamps(List<DocumentContent> contents, Document document) {
        if (document.getType() == Document.DocumentType.PDF) {
            return new ArrayList<>();
        }

        return contents.stream()
                .filter(c -> c.getStartTime() != null)
                .map(c -> ChatResponse.TimestampReference.builder()
                        .startTime(c.getStartTime())
                        .endTime(c.getEndTime())
                        .content(truncateContent(c.getContent()))
                        .formattedTime(formatTime(c.getStartTime()))
                        .build())
                .limit(5)
                .collect(Collectors.toList());
    }

    private String truncateContent(String content) {
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }

    private String formatTime(Double seconds) {
        if (seconds == null) return "00:00";
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d", mins, secs);
    }

    private String serializeTimestamps(List<ChatResponse.TimestampReference> timestamps) {
        if (timestamps == null || timestamps.isEmpty()) return null;
        return timestamps.stream()
                .map(t -> t.getFormattedTime() + ":" + t.getContent())
                .collect(Collectors.joining("|"));
    }
}

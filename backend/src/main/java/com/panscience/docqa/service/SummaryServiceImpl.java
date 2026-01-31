package com.panscience.docqa.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SummaryServiceImpl implements SummaryService {

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
                        .maxTokens(1000)
                        .temperature(0.3)
                        .build();
                log.info("OpenAI ChatModel initialized successfully");
            } catch (Exception e) {
                log.warn("Failed to initialize OpenAI, falling back to mock mode: {}", e.getMessage());
                useMockAi = true;
            }
        } else {
            log.info("Using mock AI mode for summaries (no OpenAI cost)");
            useMockAi = true;
        }
    }

    @Override
    @Cacheable(value = "summaries", key = "#content.hashCode()")
    public String generateSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "No content available for summarization.";
        }

        // Use mock summary for demo/assignment purposes
        if (useMockAi) {
            return generateMockSummary(content);
        }

        // Truncate very long content
        String truncatedContent = content.length() > 15000 
                ? content.substring(0, 15000) + "..." 
                : content;

        String prompt = """
                Please provide a comprehensive summary of the following content. 
                The summary should:
                1. Capture the main topics and key points
                2. Be concise but informative (3-5 paragraphs)
                3. Highlight any important details, dates, or figures
                
                Content:
                %s
                """.formatted(truncatedContent);

        try {
            String summary = chatModel.generate(prompt);
            log.info("Generated summary of length: {}", summary.length());
            return summary;
        } catch (Exception e) {
            log.error("Failed to generate summary with OpenAI, using mock: {}", e.getMessage());
            return generateMockSummary(content);
        }
    }

    /**
     * Generate a mock summary for demo/development purposes.
     * This allows testing without API costs.
     */
    private String generateMockSummary(String content) {
        int wordCount = content.split("\\s+").length;
        int charCount = content.length();
        
        // Extract first few sentences as preview
        String preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
        preview = preview.replaceAll("\\s+", " ").trim();
        
        String summary = String.format("""
                **Document Summary (Demo Mode)**
                
                This document contains approximately %d words and %d characters.
                
                **Preview:** %s
                
                **Note:** This is a mock summary generated for demo/development purposes. 
                To enable AI-powered summaries, configure a valid OpenAI API key with available quota 
                and set `app.mock-ai=false` in application.yml.
                
                The document has been successfully uploaded and stored. You can view, download, 
                or delete it using the document management features.
                """, wordCount, charCount, preview);
        
        log.info("Generated mock summary for document ({} words)", wordCount);
        return summary;
    }
}

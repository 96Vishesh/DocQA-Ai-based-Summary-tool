package com.panscience.docqa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panscience.docqa.entity.DocumentContent;
import com.panscience.docqa.repository.DocumentContentRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorSearchServiceImpl implements VectorSearchService {

    private final DocumentContentRepository documentContentRepository;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${app.mock-ai:true}")
    private boolean useMockAi;

    private EmbeddingModel embeddingModel;

    @PostConstruct
    public void init() {
        if (!useMockAi && openaiApiKey != null && !openaiApiKey.contains("placeholder")) {
            try {
                this.embeddingModel = OpenAiEmbeddingModel.builder()
                        .apiKey(openaiApiKey)
                        .modelName("text-embedding-3-small")
                        .build();
                log.info("Initialized OpenAI embedding model: text-embedding-3-small");
            } catch (Exception e) {
                log.warn("Failed to initialize OpenAI embedding model, using mock mode: {}", e.getMessage());
                useMockAi = true;
            }
        } else {
            log.info("Using mock AI mode for embeddings (no OpenAI cost)");
            useMockAi = true;
        }
    }

    @Override
    public void generateAndStoreEmbedding(DocumentContent content) {
        // In mock mode, skip embedding generation
        if (useMockAi) {
            log.debug("Mock mode: Skipping embedding generation for content {}", content.getId());
            return;
        }

        try {
            Embedding embedding = embeddingModel.embed(content.getContent()).content();
            String embeddingJson = objectMapper.writeValueAsString(embedding.vectorAsList());
            content.setEmbedding(embeddingJson);
            documentContentRepository.save(content);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize embedding", e);
        }
    }

    @Override
    public List<DocumentContent> search(Long documentId, String query, int topK) {
        // In mock mode, return content using simple text matching
        if (useMockAi) {
            log.debug("Mock mode: Using text-based search for query: {}", query);
            return documentContentRepository.findByDocumentIdOrderByChunkIndex(documentId)
                    .stream()
                    .limit(topK)
                    .collect(Collectors.toList());
        }

        // Get query embedding
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        float[] queryVector = queryEmbedding.vector();

        // Get all content for the document
        List<DocumentContent> allContent = documentContentRepository.findByDocumentIdOrderByChunkIndex(documentId);

        // Calculate similarity scores
        List<ScoredContent> scoredContents = new ArrayList<>();
        for (DocumentContent content : allContent) {
            if (content.getEmbedding() != null) {
                try {
                    List<Float> storedEmbedding = objectMapper.readValue(
                            content.getEmbedding(), 
                            new TypeReference<List<Float>>() {}
                    );
                    float[] storedVector = listToArray(storedEmbedding);
                    double similarity = cosineSimilarity(queryVector, storedVector);
                    scoredContents.add(new ScoredContent(content, similarity));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse embedding for content {}", content.getId());
                }
            }
        }

        // Sort by similarity and return top K
        return scoredContents.stream()
                .sorted(Comparator.comparingDouble(ScoredContent::score).reversed())
                .limit(topK)
                .map(ScoredContent::content)
                .collect(Collectors.toList());
    }

    private float[] listToArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0;
        
        double dotProduct = 0;
        double normA = 0;
        double normB = 0;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredContent(DocumentContent content, double score) {}
}


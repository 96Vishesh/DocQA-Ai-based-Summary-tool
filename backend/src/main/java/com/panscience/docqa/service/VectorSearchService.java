package com.panscience.docqa.service;

import com.panscience.docqa.entity.DocumentContent;

import java.util.List;

public interface VectorSearchService {
    void generateAndStoreEmbedding(DocumentContent content);
    List<DocumentContent> search(Long documentId, String query, int topK);
}

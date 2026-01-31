package com.panscience.docqa.service;

import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;

import java.util.List;

public interface PdfExtractionService {
    List<DocumentContent> extractContent(Document document);
}

package com.panscience.docqa.service;

import com.panscience.docqa.dto.DocumentDto;
import com.panscience.docqa.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentDto uploadDocument(MultipartFile file);

    DocumentDto getDocument(Long id);

    List<DocumentDto> getAllDocuments();

    List<DocumentDto> getDocumentsByType(Document.DocumentType type);

    void deleteDocument(Long id);

    byte[] getDocumentContent(Long id);

    String getDocumentMimeType(Long id);
}

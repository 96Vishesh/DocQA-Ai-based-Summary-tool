package com.panscience.docqa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panscience.docqa.dto.DocumentDto;
import com.panscience.docqa.entity.Document;
import com.panscience.docqa.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private com.panscience.docqa.repository.DocumentContentRepository documentContentRepository;

    @MockBean
    private com.panscience.docqa.security.JwtService jwtService;

    @MockBean
    private com.panscience.docqa.security.CustomUserDetailsService userDetailsService;

    private DocumentDto testDocumentDto;

    @BeforeEach
    void setUp() {
        testDocumentDto = DocumentDto.builder()
                .id(1L)
                .fileName("test-uuid.pdf")
                .originalFileName("test.pdf")
                .type(Document.DocumentType.PDF)
                .mimeType("application/pdf")
                .fileSize(1024L)
                .status(Document.ProcessingStatus.COMPLETED)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    void uploadDocument_shouldReturnCreatedDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes()
        );

        when(documentService.uploadDocument(any())).thenReturn(testDocumentDto);

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFileName").value("test.pdf"))
                .andExpect(jsonPath("$.type").value("PDF"));
    }

    @Test
    @WithMockUser
    void getDocument_shouldReturnDocument() throws Exception {
        when(documentService.getDocument(1L)).thenReturn(testDocumentDto);

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalFileName").value("test.pdf"));
    }

    @Test
    @WithMockUser
    void getAllDocuments_shouldReturnList() throws Exception {
        when(documentService.getAllDocuments()).thenReturn(List.of(testDocumentDto));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void deleteDocument_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/documents/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getSummary_shouldReturnSummary() throws Exception {
        testDocumentDto = DocumentDto.builder()
                .id(1L)
                .summary("This is a test summary")
                .build();

        when(documentService.getDocument(1L)).thenReturn(testDocumentDto);

        mockMvc.perform(get("/api/documents/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(1))
                .andExpect(jsonPath("$.summary").value("This is a test summary"));
    }

    @Test
    @WithMockUser
    void getTimestamps_shouldReturnEmptyList() throws Exception {
        when(documentContentRepository.findTimestampedContentByDocumentId(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/documents/1/timestamps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(1))
                .andExpect(jsonPath("$.timestamps").isArray());
    }

    @Test
    @WithMockUser
    void getDocumentContent_shouldReturnBytes() throws Exception {
        byte[] content = "PDF content".getBytes();
        
        when(documentService.getDocumentContent(1L)).thenReturn(content);
        when(documentService.getDocumentMimeType(1L)).thenReturn("application/pdf");
        when(documentService.getDocument(1L)).thenReturn(testDocumentDto);

        mockMvc.perform(get("/api/documents/1/content"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}

package com.panscience.docqa.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.panscience.docqa.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    private String fileName;
    private String originalFileName;
    private Document.DocumentType type;
    private String mimeType;
    private Long fileSize;
    private String summary;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime uploadedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
    
    private Document.ProcessingStatus status;

    public static DocumentDto fromEntity(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .originalFileName(document.getOriginalFileName())
                .type(document.getType())
                .mimeType(document.getMimeType())
                .fileSize(document.getFileSize())
                .summary(document.getSummary())
                .uploadedAt(document.getUploadedAt())
                .processedAt(document.getProcessedAt())
                .status(document.getStatus())
                .build();
    }
}

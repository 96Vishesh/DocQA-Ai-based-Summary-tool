package com.panscience.docqa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType type;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column
    private LocalDateTime processedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProcessingStatus status = ProcessingStatus.PENDING;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentContent> contents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    public enum DocumentType {
        PDF, AUDIO, VIDEO
    }

    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}

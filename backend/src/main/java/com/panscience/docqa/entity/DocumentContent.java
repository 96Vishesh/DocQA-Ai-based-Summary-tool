package com.panscience.docqa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_contents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column
    private Integer chunkIndex;

    // For audio/video - timestamps in seconds
    @Column
    private Double startTime;

    @Column
    private Double endTime;

    // For PDF - page number
    @Column
    private Integer pageNumber;

    // Vector embedding for semantic search (stored as JSON array)
    @Column(columnDefinition = "TEXT")
    private String embedding;
}

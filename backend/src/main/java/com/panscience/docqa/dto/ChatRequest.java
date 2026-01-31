package com.panscience.docqa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotBlank(message = "Message is required")
    private String message;

    private String sessionId;
}

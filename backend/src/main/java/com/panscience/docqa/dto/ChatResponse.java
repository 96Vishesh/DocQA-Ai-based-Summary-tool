package com.panscience.docqa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private String sessionId;
    private List<TimestampReference> timestamps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimestampReference {
        private Double startTime;
        private Double endTime;
        private String content;
        private String formattedTime;  // e.g., "02:35"
    }
}

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
public class TimestampResponse {
    private Long documentId;
    private List<TimestampEntry> timestamps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimestampEntry {
        private Double startTime;
        private Double endTime;
        private String formattedStartTime;
        private String formattedEndTime;
        private String topic;
        private String content;
    }
}

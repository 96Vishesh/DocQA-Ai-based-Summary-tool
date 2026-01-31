package com.panscience.docqa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panscience.docqa.entity.Document;
import com.panscience.docqa.entity.DocumentContent;
import com.panscience.docqa.exception.DocumentProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptionServiceImpl implements TranscriptionService {

    private static final String WHISPER_API_URL = "https://api.openai.com/v1/audio/transcriptions";

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.whisper-model}")
    private String whisperModel;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<DocumentContent> transcribe(Document document) {
        List<DocumentContent> contents = new ArrayList<>();

        try {
            File audioFile = new File(document.getFilePath());
            
            // Request transcription with timestamps
            String transcriptionJson = callWhisperApi(audioFile);
            
            // Parse the response and create content chunks
            contents = parseTranscriptionResponse(transcriptionJson, document);
            
            log.info("Transcribed {} segments from: {}", contents.size(), document.getOriginalFileName());
            return contents;

        } catch (Exception e) {
            log.error("Failed to transcribe document: {}", document.getOriginalFileName(), e);
            throw new DocumentProcessingException("Failed to transcribe audio/video", e);
        }
    }

    private String callWhisperApi(File audioFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(openaiApiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audioFile));
        body.add("model", whisperModel);
        body.add("response_format", "verbose_json");
        body.add("timestamp_granularities[]", "segment");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                WHISPER_API_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new DocumentProcessingException("Whisper API returned status: " + response.getStatusCode());
        }

        return response.getBody();
    }

    private List<DocumentContent> parseTranscriptionResponse(String json, Document document) {
        List<DocumentContent> contents = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode segments = root.path("segments");

            if (segments.isArray()) {
                int index = 0;
                for (JsonNode segment : segments) {
                    double start = segment.path("start").asDouble();
                    double end = segment.path("end").asDouble();
                    String text = segment.path("text").asText().trim();

                    if (!text.isEmpty()) {
                        DocumentContent content = DocumentContent.builder()
                                .document(document)
                                .content(text)
                                .startTime(start)
                                .endTime(end)
                                .chunkIndex(index++)
                                .build();
                        contents.add(content);
                    }
                }
            }

            // If no segments, use the full text
            if (contents.isEmpty()) {
                String fullText = root.path("text").asText().trim();
                if (!fullText.isEmpty()) {
                    DocumentContent content = DocumentContent.builder()
                            .document(document)
                            .content(fullText)
                            .startTime(0.0)
                            .endTime(root.path("duration").asDouble(0))
                            .chunkIndex(0)
                            .build();
                    contents.add(content);
                }
            }

        } catch (Exception e) {
            throw new DocumentProcessingException("Failed to parse transcription response", e);
        }

        return contents;
    }
}

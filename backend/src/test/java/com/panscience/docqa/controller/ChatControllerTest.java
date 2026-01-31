package com.panscience.docqa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panscience.docqa.dto.ChatRequest;
import com.panscience.docqa.dto.ChatResponse;
import com.panscience.docqa.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private com.panscience.docqa.security.JwtService jwtService;

    @MockBean
    private com.panscience.docqa.security.CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void chat_withValidRequest_shouldReturnResponse() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .documentId(1L)
                .message("What is this about?")
                .build();

        ChatResponse response = ChatResponse.builder()
                .response("This document discusses...")
                .sessionId("session-123")
                .timestamps(List.of())
                .build();

        when(chatService.chat(any(ChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("This document discusses..."))
                .andExpect(jsonPath("$.sessionId").value("session-123"));
    }

    @Test
    @WithMockUser
    void chat_withMissingDocumentId_shouldReturnBadRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("What is this about?")
                .build();

        mockMvc.perform(post("/api/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void chat_withEmptyMessage_shouldReturnBadRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .documentId(1L)
                .message("")
                .build();

        mockMvc.perform(post("/api/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void chat_withExistingSession_shouldMaintainSession() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .documentId(1L)
                .message("Follow-up question")
                .sessionId("existing-session")
                .build();

        ChatResponse response = ChatResponse.builder()
                .response("Follow-up response...")
                .sessionId("existing-session")
                .timestamps(List.of())
                .build();

        when(chatService.chat(any(ChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("existing-session"));
    }
}

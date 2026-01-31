package com.panscience.docqa.controller;

import com.panscience.docqa.dto.ChatRequest;
import com.panscience.docqa.dto.ChatResponse;
import com.panscience.docqa.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }
}

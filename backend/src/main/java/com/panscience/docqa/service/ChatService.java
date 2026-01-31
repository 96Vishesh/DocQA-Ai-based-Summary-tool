package com.panscience.docqa.service;

import com.panscience.docqa.dto.ChatRequest;
import com.panscience.docqa.dto.ChatResponse;

public interface ChatService {
    ChatResponse chat(ChatRequest request);
}

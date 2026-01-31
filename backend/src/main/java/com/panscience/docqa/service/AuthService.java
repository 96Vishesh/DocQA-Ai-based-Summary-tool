package com.panscience.docqa.service;

import com.panscience.docqa.dto.AuthRequest;
import com.panscience.docqa.dto.AuthResponse;
import com.panscience.docqa.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    AuthResponse register(RegisterRequest request);
}

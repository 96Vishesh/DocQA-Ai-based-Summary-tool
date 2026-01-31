package com.panscience.docqa.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Base64 encoded secret key
        ReflectionTestUtils.setField(jwtService, "secretKey", 
                "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLXRlc3Rpbmc=");
        ReflectionTestUtils.setField(jwtService, "expirationTime", 86400000L);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String email = "test@example.com";
        
        String token = jwtService.generateToken(email);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        
        String extractedEmail = jwtService.extractEmail(token);
        
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        
        boolean isValid = jwtService.isTokenValid(token, email);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_withWrongEmail_shouldReturnFalse() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        
        boolean isValid = jwtService.isTokenValid(token, "wrong@example.com");
        
        assertThat(isValid).isFalse();
    }

    @Test
    void getExpirationTime_shouldReturnConfiguredTime() {
        long expirationTime = jwtService.getExpirationTime();
        
        assertThat(expirationTime).isEqualTo(86400000L);
    }

    @Test
    void generateToken_differentEmails_shouldGenerateDifferentTokens() {
        String token1 = jwtService.generateToken("user1@example.com");
        String token2 = jwtService.generateToken("user2@example.com");
        
        assertThat(token1).isNotEqualTo(token2);
    }
}

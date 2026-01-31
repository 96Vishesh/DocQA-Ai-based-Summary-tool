package com.panscience.docqa.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDocumentNotFound_shouldReturn404() {
        DocumentNotFoundException ex = new DocumentNotFoundException(123L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleDocumentNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("123");
    }

    @Test
    void handleFileStorage_shouldReturn500() {
        FileStorageException ex = new FileStorageException("File storage failed");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleFileStorage(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("File storage failed");
    }

    @Test
    void handleDocumentProcessing_shouldReturn500() {
        DocumentProcessingException ex = new DocumentProcessingException("Processing failed");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleDocumentProcessing(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Processing failed");
    }

    @Test
    void handleMaxUploadSize_shouldReturn413() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(100);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleMaxUploadSize(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody().message()).contains("maximum limit");
    }

    @Test
    void handleBadCredentials_shouldReturn401() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).contains("Invalid email or password");
    }

    @Test
    void handleGeneral_shouldReturn500() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleValidationErrors_shouldReturn400WithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "email", "Invalid email");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("errors");
    }
}

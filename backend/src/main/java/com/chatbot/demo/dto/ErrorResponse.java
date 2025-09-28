package com.chatbot.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Error response DTO for API error handling.
 * 
 * Schema from API specification for 400/500 error responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * Error message
     */
    private String error;
    
    /**
     * Error code for client handling
     */
    private String code;
    
    /**
     * Error timestamp
     */
    private LocalDateTime timestamp;
    
    /**
     * Convenience constructor with current timestamp
     */
    public ErrorResponse(String error, String code) {
        this.error = error;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
}
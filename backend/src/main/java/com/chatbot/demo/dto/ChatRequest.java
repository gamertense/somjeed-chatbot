package com.chatbot.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Chat request DTO for incoming chat messages.
 * 
 * Schema from API specification:
 * - message: required string (user's chat message)
 * - sessionId: optional string (session ID for maintaining context)
 * - userId: optional string (user ID for personalized responses)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * User's chat message - required
     */
    @NotBlank(message = "Message cannot be empty")
    private String message;
    
    /**
     * Optional session ID (backend creates new session if not provided)
     */
    private String sessionId;
    
    /**
     * Optional user ID for personalized responses
     */
    private String userId;
}
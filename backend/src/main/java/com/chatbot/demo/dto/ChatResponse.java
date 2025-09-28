package com.chatbot.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat response DTO for bot responses.
 * 
 * Schema from API specification includes all fields for frontend handling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    
    /**
     * Session identifier for maintaining conversation context
     */
    private String sessionId;
    
    /**
     * Bot's response message
     */
    private String botMessage;
    
    /**
     * Type of bot message for frontend handling
     */
    private MessageType messageType;
    
    /**
     * Optional quick reply buttons for user
     */
    private List<String> quickReplies;
    
    /**
     * Feedback options when requesting user satisfaction
     */
    private List<FeedbackOption> feedbackOptions;
    
    /**
     * Indicates if conversation session is finished
     */
    private Boolean isSessionComplete;
    
    /**
     * Response timestamp
     */
    private LocalDateTime timestamp;
    
    /**
     * Message type enum as per API specification
     */
    public enum MessageType {
        GREETING, RESPONSE, FEEDBACK_REQUEST, GOODBYE
    }
    
    /**
     * Feedback option enum as per API specification
     */
    public enum FeedbackOption {
        HAPPY, NEUTRAL, SAD
    }
}
package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores anonymous user feedback for service improvement.
 * 
 * TypeScript Interface:
 * interface FeedbackResponse {
 *   feedbackId: string;
 *   sessionId: string;
 *   rating: "HAPPY" | "NEUTRAL" | "SAD";
 *   comment?: string;
 *   timestamp: string; // ISO datetime format
 *   anonymous: boolean;
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    
    /**
     * Unique feedback identifier
     */
    private String feedbackId;
    
    /**
     * Reference to chat session
     */
    private String sessionId;
    
    /**
     * User satisfaction rating
     */
    private FeedbackRating rating;
    
    /**
     * Optional text feedback
     */
    private String comment;
    
    /**
     * Feedback submission time
     */
    private LocalDateTime timestamp;
    
    /**
     * Whether feedback is anonymous
     */
    private Boolean anonymous;
    
    /**
     * Feedback rating enum
     */
    public enum FeedbackRating {
        HAPPY, NEUTRAL, SAD
    }
}
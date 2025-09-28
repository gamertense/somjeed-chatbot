package com.chatbot.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.chatbot.demo.model.FeedbackResponse.FeedbackRating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Feedback submission request DTO.
 * 
 * Used for feedback endpoint POST /api/v1/chat/feedback
 * 
 * TypeScript Interface:
 * interface FeedbackSubmissionRequest {
 *   sessionId: string;
 *   userId: string;
 *   rating: 'HAPPY' | 'NEUTRAL' | 'SAD';
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    
    /**
     * Session ID for feedback context
     */
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    /**
     * User ID for tracking (anonymous processing)
     */
    @NotBlank(message = "User ID is required")
    private String userId;
    
    /**
     * Rating value: HAPPY, NEUTRAL, or SAD
     */
    @NotNull(message = "Rating is required")
    private FeedbackRating rating;
}
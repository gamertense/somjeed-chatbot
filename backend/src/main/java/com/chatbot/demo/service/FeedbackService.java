package com.chatbot.demo.service;

import com.chatbot.demo.model.FeedbackResponse;
import com.chatbot.demo.model.FeedbackResponse.FeedbackRating;
import com.chatbot.demo.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling feedback submission and management.
 * 
 * Features:
 * - Anonymous feedback storage
 * - Session completion tracking
 * - Duplicate feedback prevention
 * - Basic analytics support
 */
@Service
public class FeedbackService {
    
    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private SessionManager sessionManager;
    
    /**
     * Submit user feedback for a chat session
     * 
     * @param sessionId chat session identifier
     * @param userId user identifier (stored anonymously)
     * @param rating user satisfaction rating
     * @return submitted feedback response
     * @throws IllegalArgumentException if session doesn't exist or feedback already submitted
     */
    public FeedbackResponse submitFeedback(String sessionId, String userId, FeedbackRating rating) {
        logger.info("Submitting feedback: sessionId={}, userId={}, rating={}", sessionId, userId, rating);
        
        // Validate session exists
        if (!sessionManager.sessionExists(sessionId)) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        
        // Check if feedback already submitted for this session
        if (feedbackRepository.existsBySessionId(sessionId)) {
            logger.warn("Feedback already exists for session: {}", sessionId);
            // Return existing feedback instead of throwing error (graceful handling)
            List<FeedbackResponse> existing = feedbackRepository.findBySessionId(sessionId);
            return existing.get(0);
        }
        
        // Create feedback response
        FeedbackResponse feedbackResponse = new FeedbackResponse();
        feedbackResponse.setFeedbackId(UUID.randomUUID().toString());
        feedbackResponse.setSessionId(sessionId);
        feedbackResponse.setRating(rating);
        feedbackResponse.setTimestamp(LocalDateTime.now());
        feedbackResponse.setAnonymous(true); // All feedback is anonymous for privacy
        
        // Save feedback
        FeedbackResponse savedFeedback = feedbackRepository.save(feedbackResponse);
        
        // Update session state to completed
        try {
            sessionManager.completeSession(sessionId);
            logger.info("Session completed after feedback submission: {}", sessionId);
        } catch (Exception e) {
            logger.warn("Failed to complete session {}: {}", sessionId, e.getMessage());
            // Don't fail feedback submission if session completion fails
        }
        
        logger.info("Feedback submitted successfully: feedbackId={}", savedFeedback.getFeedbackId());
        return savedFeedback;
    }
    
    /**
     * Get feedback for a specific session
     * 
     * @param sessionId session identifier
     * @return list of feedback for the session
     */
    public List<FeedbackResponse> getFeedbackBySession(String sessionId) {
        return feedbackRepository.findBySessionId(sessionId);
    }
    
    /**
     * Get basic feedback statistics
     * 
     * @return feedback statistics summary
     */
    public FeedbackStatistics getFeedbackStatistics() {
        List<FeedbackResponse> allFeedback = feedbackRepository.findAll();
        
        long totalFeedback = allFeedback.size();
        long happyCount = allFeedback.stream()
                .mapToLong(f -> f.getRating() == FeedbackRating.HAPPY ? 1 : 0)
                .sum();
        long neutralCount = allFeedback.stream()
                .mapToLong(f -> f.getRating() == FeedbackRating.NEUTRAL ? 1 : 0)
                .sum();
        long sadCount = allFeedback.stream()
                .mapToLong(f -> f.getRating() == FeedbackRating.SAD ? 1 : 0)
                .sum();
        
        return new FeedbackStatistics(totalFeedback, happyCount, neutralCount, sadCount);
    }
    
    /**
     * Check if feedback exists for session
     * 
     * @param sessionId session identifier
     * @return true if feedback exists
     */
    public boolean hasFeedback(String sessionId) {
        return feedbackRepository.existsBySessionId(sessionId);
    }
    
    /**
     * Feedback statistics data class
     */
    public record FeedbackStatistics(
            long totalFeedback,
            long happyCount,
            long neutralCount,
            long sadCount
    ) {
        public double getHappyPercentage() {
            return totalFeedback > 0 ? (double) happyCount / totalFeedback * 100 : 0;
        }
        
        public double getNeutralPercentage() {
            return totalFeedback > 0 ? (double) neutralCount / totalFeedback * 100 : 0;
        }
        
        public double getSadPercentage() {
            return totalFeedback > 0 ? (double) sadCount / totalFeedback * 100 : 0;
        }
    }
}
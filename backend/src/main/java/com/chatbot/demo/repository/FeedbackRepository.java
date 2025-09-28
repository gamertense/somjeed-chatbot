package com.chatbot.demo.repository;

import com.chatbot.demo.model.FeedbackResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for feedback storage.
 * 
 * For MVP, uses ConcurrentHashMap to store feedback data.
 * In production, this would be replaced with JPA repository for database persistence.
 * 
 * Features:
 * - Thread-safe concurrent storage
 * - Session-based feedback lookup
 * - Automatic cleanup capabilities
 */
@Repository
public class FeedbackRepository {
    
    private final ConcurrentHashMap<String, FeedbackResponse> feedbackStorage = new ConcurrentHashMap<>();
    
    /**
     * Save feedback response
     * 
     * @param feedbackResponse the feedback to save
     * @return saved feedback with generated ID
     */
    public FeedbackResponse save(FeedbackResponse feedbackResponse) {
        feedbackStorage.put(feedbackResponse.getFeedbackId(), feedbackResponse);
        return feedbackResponse;
    }
    
    /**
     * Find feedback by session ID
     * 
     * @param sessionId session identifier
     * @return list of feedback for the session
     */
    public List<FeedbackResponse> findBySessionId(String sessionId) {
        return feedbackStorage.values().stream()
                .filter(feedback -> sessionId.equals(feedback.getSessionId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Find feedback by ID
     * 
     * @param feedbackId feedback identifier
     * @return feedback response or null if not found
     */
    public FeedbackResponse findById(String feedbackId) {
        return feedbackStorage.get(feedbackId);
    }
    
    /**
     * Get all feedback (for admin/analytics purposes)
     * 
     * @return all stored feedback
     */
    public List<FeedbackResponse> findAll() {
        return new ArrayList<>(feedbackStorage.values());
    }
    
    /**
     * Count total feedback entries
     * 
     * @return total count
     */
    public long count() {
        return feedbackStorage.size();
    }
    
    /**
     * Check if feedback exists for session
     * 
     * @param sessionId session identifier
     * @return true if feedback exists
     */
    public boolean existsBySessionId(String sessionId) {
        return feedbackStorage.values().stream()
                .anyMatch(feedback -> sessionId.equals(feedback.getSessionId()));
    }
    
    /**
     * Clean up old feedback entries (older than specified days)
     * Useful for memory management in production
     * 
     * @param daysOld number of days to keep feedback
     * @return number of entries removed
     */
    public int cleanupOldFeedback(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<String> toRemove = feedbackStorage.values().stream()
                .filter(feedback -> feedback.getTimestamp().isBefore(cutoffDate))
                .map(FeedbackResponse::getFeedbackId)
                .collect(Collectors.toList());
        
        toRemove.forEach(feedbackStorage::remove);
        return toRemove.size();
    }
}
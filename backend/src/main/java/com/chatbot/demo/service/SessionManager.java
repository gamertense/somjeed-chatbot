package com.chatbot.demo.service;

import com.chatbot.demo.model.SessionContext;
import com.chatbot.demo.model.User;
import com.chatbot.demo.model.enums.ConversationState;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * Thread-safe session management for conversation state tracking.
 * Maintains conversation context across message exchanges and handles session cleanup.
 */
@Service
public class SessionManager {
    
    private final ConcurrentHashMap<String, SessionContext> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * Session timeout in minutes - cleanup sessions inactive longer than this
     */
    private static final long SESSION_TIMEOUT_MINUTES = 30;
    
    public SessionManager() {
        // Schedule cleanup task every 10 minutes
        scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions, 10, 10, TimeUnit.MINUTES);
    }
    
    /**
     * Create a new session for a user
     * 
     * @param user User context for the session
     * @return New session ID
     */
    public String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        SessionContext context = new SessionContext(sessionId, user);
        sessions.put(sessionId, context);
        return sessionId;
    }
    
    /**
     * Retrieve session context by session ID
     * 
     * @param sessionId The session identifier
     * @return SessionContext or null if not found
     */
    public SessionContext getSessionContext(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.updateActivity();
        }
        return context;
    }
    
    /**
     * Update conversation state for a session
     * 
     * @param sessionId The session identifier
     * @param newState New conversation state
     * @return Updated SessionContext or null if session not found
     */
    public SessionContext updateSessionState(String sessionId, ConversationState newState) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.setConversationState(newState);
            context.updateActivity();
            return context;
        }
        return null;
    }
    
    /**
     * Increment message count and update activity for a session
     * 
     * @param sessionId The session identifier
     */
    public void incrementMessageCount(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.incrementMessageCount();
        }
    }
    
    /**
     * Mark that user has received greeting for this session
     * 
     * @param sessionId The session identifier
     */
    public void markGreetingSent(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.setHasReceivedGreeting(true);
            context.updateActivity();
        }
    }
    
    /**
     * Remove a specific session
     * 
     * @param sessionId The session identifier
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
    
    /**
     * Get current number of active sessions
     * 
     * @return Active session count
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
    
    /**
     * Check if session exists and is valid
     * 
     * @param sessionId The session identifier
     * @return true if session exists and is active
     */
    public boolean isValidSession(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        return context != null && !isSessionExpired(context);
    }
    
    /**
     * Check if session exists (for feedback service)
     * 
     * @param sessionId The session identifier
     * @return true if session exists
     */
    public boolean sessionExists(String sessionId) {
        return sessions.containsKey(sessionId);
    }
    
    /**
     * Complete a session and mark it for cleanup
     * Used after feedback submission to finalize session
     * 
     * @param sessionId The session identifier
     */
    public void completeSession(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.setConversationState(ConversationState.COMPLETE);
            context.updateActivity();
            // Session remains in memory for a short time for any final operations
            // but will be cleaned up by the scheduled cleanup task
        }
    }
    
    /**
     * Cleanup inactive sessions based on timeout
     * Automatically called by scheduled task every 10 minutes
     */
    public void cleanupInactiveSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);
        final int[] removedCount = {0}; // Use array to make it effectively final
        
        sessions.entrySet().removeIf(entry -> {
            SessionContext context = entry.getValue();
            boolean expired = context.getLastActivity().isBefore(cutoffTime);
            if (expired) {
                removedCount[0]++;
            }
            return expired;
        });
        
        if (removedCount[0] > 0) {
            System.out.println("SessionManager: Cleaned up " + removedCount[0] + " inactive sessions");
        }
    }
    
    /**
     * Check if session is expired based on last activity
     */
    private boolean isSessionExpired(SessionContext context) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);
        return context.getLastActivity().isBefore(cutoffTime);
    }
    
    /**
     * Shutdown cleanup task when service is destroyed
     */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
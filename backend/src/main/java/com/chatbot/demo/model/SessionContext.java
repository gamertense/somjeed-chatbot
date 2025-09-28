package com.chatbot.demo.model;

import com.chatbot.demo.model.enums.ConversationState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Holds session data for conversation state tracking and context management.
 * Used by SessionManager to maintain conversation flow across message exchanges.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionContext {
    
    /**
     * Unique session identifier
     */
    private String sessionId;
    
    /**
     * Current state of the conversation
     */
    private ConversationState conversationState;
    
    /**
     * Associated user context for personalized responses
     */
    private User user;
    
    /**
     * Current intent being processed (if any)
     */
    private Intent currentIntent;
    
    /**
     * Last activity timestamp for session cleanup
     */
    private LocalDateTime lastActivity;
    
    /**
     * Number of messages exchanged in this session
     */
    private int messageCount;
    
    /**
     * Whether user has received greeting for this session
     */
    private boolean hasReceivedGreeting;
    
    /**
     * Constructor for new session
     */
    public SessionContext(String sessionId, User user) {
        this.sessionId = sessionId;
        this.user = user;
        this.conversationState = ConversationState.GREETING;
        this.lastActivity = LocalDateTime.now();
        this.messageCount = 0;
        this.hasReceivedGreeting = false;
    }
    
    /**
     * Update last activity timestamp
     */
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Increment message count and update activity
     */
    public void incrementMessageCount() {
        this.messageCount++;
        updateActivity();
    }
}
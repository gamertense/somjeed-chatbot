package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Maintains conversation context and enables intent prediction across chat interactions.
 * 
 * TypeScript Interface:
 * interface ChatSession {
 *   sessionId: string;
 *   userId?: string; // Optional for anonymous sessions
 *   startTime: string; // ISO datetime format
 *   lastActivityTime: string; // ISO datetime format
 *   currentIntent?: Intent;
 *   conversationState: "GREETING" | "INTENT_PREDICTION" | "INTENT_HANDLING" | "FEEDBACK" | "COMPLETE";
 *   contextData: Record<string, any>;
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    
    /**
     * Unique session identifier
     */
    private String sessionId;
    
    /**
     * Reference to user (nullable for anonymous sessions)
     */
    private String userId;
    
    /**
     * Session start timestamp
     */
    private LocalDateTime startTime;
    
    /**
     * Last user interaction
     */
    private LocalDateTime lastActivityTime;
    
    /**
     * Currently detected or predicted intent
     */
    private Intent currentIntent;
    
    /**
     * Current state in conversation flow
     */
    private ConversationState conversationState;
    
    /**
     * Flexible context storage
     */
    private Map<String, Object> contextData;
    
    /**
     * Conversation state enum
     */
    public enum ConversationState {
        GREETING, INTENT_PREDICTION, INTENT_HANDLING, FEEDBACK, COMPLETE
    }
}
package com.chatbot.demo.model.enums;

/**
 * Represents the current state of a conversation session.
 * Used by SessionManager to track conversation flow and determine appropriate responses.
 */
public enum ConversationState {
    /**
     * Initial state when user starts conversation, expecting greeting response
     */
    GREETING,
    
    /**
     * System is providing proactive intent predictions based on user context
     */
    INTENT_PREDICTION,
    
    /**
     * Processing and responding to a detected user intent
     */
    INTENT_HANDLING,
    
    /**
     * Collecting user feedback about the conversation experience
     */
    FEEDBACK,
    
    /**
     * Conversation has been completed successfully
     */
    COMPLETE
}
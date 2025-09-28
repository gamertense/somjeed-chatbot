package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores individual messages in chat conversation for context and debugging.
 * 
 * TypeScript Interface:
 * interface ChatMessage {
 *   messageId: string;
 *   sessionId: string;
 *   sender: "USER" | "BOT";
 *   content: string;
 *   timestamp: string; // ISO datetime format
 *   messageType: "TEXT" | "QUICK_REPLY" | "FEEDBACK";
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    /**
     * Unique message identifier
     */
    private String messageId;
    
    /**
     * Reference to chat session
     */
    private String sessionId;
    
    /**
     * Who sent the message
     */
    private MessageSender sender;
    
    /**
     * Message content
     */
    private String content;
    
    /**
     * Message timestamp
     */
    private LocalDateTime timestamp;
    
    /**
     * Type of message
     */
    private MessageType messageType;
    
    /**
     * Message sender enum
     */
    public enum MessageSender {
        USER, BOT
    }
    
    /**
     * Message type enum
     */
    public enum MessageType {
        TEXT, QUICK_REPLY, FEEDBACK
    }
}
package com.chatbot.demo.controller;

import com.chatbot.demo.dto.ChatRequest;
import com.chatbot.demo.dto.ChatResponse;
import com.chatbot.demo.dto.ErrorResponse;
import com.chatbot.demo.model.ChatMessage;
import com.chatbot.demo.service.ConversationService;
import com.chatbot.demo.service.MockDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Chat controller handling the single /api/v1/chat endpoint.
 * 
 * Handles:
 * - Session management (creates new session if none provided)
 * - Intent detection and prediction using ConversationService
 * - Context-aware responses with conversation state tracking
 * - Weather-based greetings
 * - User data lookups and personalization
 * - Feedback collection with proper state management
 */
@RestController
@RequestMapping("/api/v1")
@Validated
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private MockDataService mockDataService;
    
    /**
     * Single endpoint for all chat interactions.
     * POST /api/v1/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody ChatRequest request) {
        try {
            logger.info("Received chat request: sessionId={}, userId={}, message='{}'", 
                request.getSessionId(), request.getUserId(), request.getMessage());
            
            // Generate or use existing session ID
            String sessionId = request.getSessionId() != null ? 
                request.getSessionId() : UUID.randomUUID().toString();
            
            // Process message through ConversationService
            ChatMessage chatMessage = conversationService.processMessage(
                request.getMessage(), 
                sessionId, 
                request.getUserId()
            );
            
            // Convert to ChatResponse DTO
            ChatResponse response = buildChatResponseFromMessage(chatMessage);
            
            logger.info("Sending chat response: sessionId={}, messageType={}", 
                sessionId, response.getMessageType());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            ErrorResponse error = new ErrorResponse(
                "Internal server error occurred while processing your request",
                "CHAT_PROCESSING_ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Convert ChatMessage to ChatResponse DTO
     */
    private ChatResponse buildChatResponseFromMessage(ChatMessage chatMessage) {
        // Determine message type based on content
        ChatResponse.MessageType messageType = determineMessageType(chatMessage.getContent());
        
        // Generate appropriate quick replies based on message type
        java.util.List<String> quickReplies = generateQuickReplies(messageType);
        
        return ChatResponse.builder()
            .sessionId(chatMessage.getSessionId())
            .botMessage(chatMessage.getContent())
            .messageType(messageType)
            .quickReplies(quickReplies)
            .isSessionComplete(messageType == ChatResponse.MessageType.GOODBYE)
            .timestamp(chatMessage.getTimestamp())
            .build();
    }
    
    /**
     * Determine message type based on content
     */
    private ChatResponse.MessageType determineMessageType(String content) {
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("good morning") || lowerContent.contains("good afternoon") || 
            lowerContent.contains("good evening") || lowerContent.contains("sunshine day")) {
            return ChatResponse.MessageType.GREETING;
        } else if (lowerContent.contains("feedback") || lowerContent.contains("rate")) {
            return ChatResponse.MessageType.FEEDBACK_REQUEST;
        } else if (lowerContent.contains("thank you for using") || lowerContent.contains("have a great day")) {
            return ChatResponse.MessageType.GOODBYE;
        } else {
            return ChatResponse.MessageType.RESPONSE;
        }
    }
    
    /**
     * Generate quick replies based on message type
     */
    private java.util.List<String> generateQuickReplies(ChatResponse.MessageType messageType) {
        return switch (messageType) {
            case GREETING -> Arrays.asList("Check Balance", "Recent Transactions", "Payment Due Date");
            case FEEDBACK_REQUEST -> Arrays.asList("Great Service", "Good", "Could Be Better");
            case GOODBYE -> Arrays.asList();
            default -> Arrays.asList("Check Balance", "Recent Transactions", "Help");
        };
    }
    
    /**
     * Global exception handler for validation errors
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        
        logger.error("Validation error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            "Invalid request: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
            "VALIDATION_ERROR"
        );
        return ResponseEntity.badRequest().body(error);
    }
}
package com.chatbot.demo.controller;

import com.chatbot.demo.dto.ChatRequest;
import com.chatbot.demo.dto.ChatResponse;
import com.chatbot.demo.dto.ErrorResponse;
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
 * - Intent detection and prediction
 * - Context-aware responses
 * - Weather-based greetings
 * - User data lookups
 * - Feedback collection
 */
@RestController
@RequestMapping("/api/v1")
@Validated
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
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
            
            // Basic intent detection based on message content
            ChatResponse response = buildChatResponse(request, sessionId);
            
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
     * Build chat response based on request content and context
     */
    private ChatResponse buildChatResponse(ChatRequest request, String sessionId) {
        String message = request.getMessage().toLowerCase();
        String userId = request.getUserId();
        
        // Simple intent detection based on keywords
        if (isGreeting(message)) {
            return buildGreetingResponse(sessionId, userId);
        } else if (isPaymentInquiry(message)) {
            return buildPaymentResponse(sessionId, userId);
        } else if (isTransactionInquiry(message)) {
            return buildTransactionResponse(sessionId, userId);
        } else if (isFeedback(message)) {
            return buildFeedbackResponse(sessionId);
        } else if (isGoodbye(message)) {
            return buildGoodbyeResponse(sessionId);
        } else {
            return buildDefaultResponse(sessionId);
        }
    }
    
    private boolean isGreeting(String message) {
        return message.contains("hello") || message.contains("hi") || 
               message.contains("good morning") || message.contains("good afternoon") ||
               message.contains("good evening") || message.contains("hey");
    }
    
    private boolean isPaymentInquiry(String message) {
        return message.contains("balance") || message.contains("payment") || 
               message.contains("due") || message.contains("owe");
    }
    
    private boolean isTransactionInquiry(String message) {
        return message.contains("transaction") || message.contains("purchase") ||
               message.contains("charge") || message.contains("spending");
    }
    
    private boolean isFeedback(String message) {
        return message.contains("feedback") || message.contains("rate") ||
               message.contains("review") || message.contains("satisfied");
    }
    
    private boolean isGoodbye(String message) {
        return message.contains("bye") || message.contains("goodbye") ||
               message.contains("thanks") || message.contains("thank you");
    }
    
    private ChatResponse buildGreetingResponse(String sessionId, String userId) {
        var weather = mockDataService.getCurrentWeatherContext();
        String weatherGreeting = String.format("Good day! It's a %s day with %d°C. ", 
            weather.getDescription().toLowerCase(), weather.getTemperature());
        
        String botMessage;
        if (userId != null) {
            var user = mockDataService.getUserById(userId);
            if (user != null) {
                botMessage = weatherGreeting + String.format(
                    "Hello! I'm here to help you with your credit card ending in %s. How can I assist you today?",
                    user.getCardNumber().substring(user.getCardNumber().length() - 4)
                );
            } else {
                botMessage = weatherGreeting + "Hello! I'm your credit card assistant. How can I help you today?";
            }
        } else {
            botMessage = weatherGreeting + "Hello! I'm your credit card assistant. How can I help you today?";
        }
        
        return ChatResponse.builder()
            .sessionId(sessionId)
            .botMessage(botMessage)
            .messageType(ChatResponse.MessageType.GREETING)
            .quickReplies(Arrays.asList("Check Balance", "Recent Transactions", "Payment Due Date"))
            .isSessionComplete(false)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private ChatResponse buildPaymentResponse(String sessionId, String userId) {
        String botMessage;
        if (userId != null) {
            var user = mockDataService.getUserById(userId);
            if (user != null) {
                botMessage = String.format(
                    "Your current balance is $%.2f with $%.2f available credit. Your next payment of $%.2f is due on %s.",
                    user.getCurrentBalance(), user.getAvailableCredit(),
                    user.getCurrentBalance(), user.getDueDate()
                );
                
                if (user.getPaymentStatus() == com.chatbot.demo.model.User.PaymentStatus.OVERDUE) {
                    botMessage += " Note: Your payment is currently overdue. Please make a payment as soon as possible.";
                }
            } else {
                botMessage = "I'd be happy to help with payment information. Could you please provide your user ID?";
            }
        } else {
            botMessage = "I'd be happy to help with payment information. Could you please provide your user ID?";
        }
        
        return ChatResponse.builder()
            .sessionId(sessionId)
            .botMessage(botMessage)
            .messageType(ChatResponse.MessageType.RESPONSE)
            .quickReplies(Arrays.asList("Make Payment", "Payment History", "Set Up Auto-Pay"))
            .isSessionComplete(false)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private ChatResponse buildTransactionResponse(String sessionId, String userId) {
        String botMessage;
        if (userId != null) {
            var transactions = mockDataService.getTransactionsByUserId(userId);
            if (!transactions.isEmpty()) {
                var latest = transactions.get(0);
                botMessage = String.format(
                    "Here are your recent transactions. Your latest transaction was $%.2f at %s on %s.",
                    latest.getAmount(), latest.getMerchantName(),
                    latest.getTransactionDate().toLocalDate()
                );
            } else {
                botMessage = "No recent transactions found for your account.";
            }
        } else {
            botMessage = "I'd be happy to help with transaction information. Could you please provide your user ID?";
        }
        
        return ChatResponse.builder()
            .sessionId(sessionId)
            .botMessage(botMessage)
            .messageType(ChatResponse.MessageType.RESPONSE)
            .quickReplies(Arrays.asList("View All Transactions", "Dispute Transaction", "Transaction Categories"))
            .isSessionComplete(false)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private ChatResponse buildFeedbackResponse(String sessionId) {
        return ChatResponse.builder()
            .sessionId(sessionId)
            .botMessage("Thank you for wanting to provide feedback! How would you rate your experience with me today?")
            .messageType(ChatResponse.MessageType.FEEDBACK_REQUEST)
            .feedbackOptions(Arrays.asList(
                ChatResponse.FeedbackOption.HAPPY,
                ChatResponse.FeedbackOption.NEUTRAL,
                ChatResponse.FeedbackOption.SAD
            ))
            .isSessionComplete(false)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private ChatResponse buildGoodbyeResponse(String sessionId) {
        return ChatResponse.builder()
            .sessionId(sessionId)
            .botMessage("Thank you for using our service! Have a great day, and feel free to reach out anytime.")
            .messageType(ChatResponse.MessageType.GOODBYE)
            .isSessionComplete(true)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private ChatResponse buildDefaultResponse(String sessionId) {
        return ChatResponse.builder()
            .sessionId(sessionId)
            .botMessage("I'm here to help with your credit card account. You can ask me about your balance, recent transactions, payment due dates, or provide feedback.")
            .messageType(ChatResponse.MessageType.RESPONSE)
            .quickReplies(Arrays.asList("Check Balance", "Recent Transactions", "Help"))
            .isSessionComplete(false)
            .timestamp(LocalDateTime.now())
            .build();
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
package com.chatbot.demo.service;

import com.chatbot.demo.model.*;
import com.chatbot.demo.model.enums.ConversationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main conversation orchestration service.
 * Processes chat messages and generates contextual, intelligent responses.
 */
@Service
public class ConversationService {
    
    private final IntentDetectionService intentDetectionService;
    private final MockDataService mockDataService;
    private final WeatherService weatherService;
    private final SessionManager sessionManager;
    
    @Autowired
    public ConversationService(
        IntentDetectionService intentDetectionService,
        MockDataService mockDataService,
        WeatherService weatherService,
        SessionManager sessionManager
    ) {
        this.intentDetectionService = intentDetectionService;
        this.mockDataService = mockDataService;
        this.weatherService = weatherService;
        this.sessionManager = sessionManager;
    }
    
    /**
     * Process incoming chat message and orchestrate conversation flow
     * 
     * @param message User message text
     * @param sessionId Session identifier
     * @return Chat response with appropriate content
     */
    public ChatMessage processMessage(String message, String sessionId) {
        // Get or create session context
        SessionContext sessionContext = sessionManager.getSessionContext(sessionId);
        String activeSessionId = sessionId;
        
        if (sessionContext == null) {
            // Create new session with default user (user123 for demo)
            User user = mockDataService.getUserById("user123");
            activeSessionId = sessionManager.createSession(user);
            sessionContext = sessionManager.getSessionContext(activeSessionId);
        }
        
        // Increment message count
        sessionManager.incrementMessageCount(activeSessionId);
        
        // Handle conversation based on current state
        ChatMessage response = switch (sessionContext.getConversationState()) {
            case GREETING -> handleGreetingState(message, sessionContext);
            case INTENT_PREDICTION -> handleIntentPredictionState(message, sessionContext);
            case INTENT_HANDLING -> handleIntentHandlingState(message, sessionContext);
            case FEEDBACK -> handleFeedbackState(message, sessionContext);
            case COMPLETE -> handleCompleteState(message, sessionContext);
        };
        
        return response;
    }
    
    /**
     * Generate contextual greeting with time and weather information
     * 
     * @param user User context for personalization
     * @return Contextual greeting message
     */
    public String generateContextualGreeting(User user) {
        WeatherContext weather = weatherService.getCurrentWeather();
        String timeGreeting = getTimeBasedGreeting();
        String weatherText = weatherService.getGreetingWeatherText(weather.getCondition());
        
        return String.format("%s, %s", timeGreeting, weatherText);
    }
    
    /**
     * Handle intent-specific responses
     * 
     * @param intent Detected intent
     * @param user User context
     * @return Intent-appropriate response
     */
    public String handleIntentResponse(Intent intent, User user) {
        return switch (intent.getIntentName()) {
            case GREETING -> generateContextualGreeting(user);
            case PAYMENT_INQUIRY -> generatePaymentInquiryResponse(user);
            case E_STATEMENT -> generateStatementResponse(user);
            case TRANSACTION_DISPUTE -> generateDisputeResponse(user);
            case FEEDBACK_COLLECTION -> generateFeedbackResponse();
            case UNKNOWN -> "I'm not sure how to help with that. Could you please rephrase your question about your credit card?";
        };
    }
    
    /**
     * Handle greeting state - provide welcome and move to intent prediction
     */
    private ChatMessage handleGreetingState(String message, SessionContext context) {
        Intent detectedIntent = intentDetectionService.detectIntent(message);
        
        if (detectedIntent.getIntentName() == Intent.IntentName.GREETING || !context.isHasReceivedGreeting()) {
            String greeting = generateContextualGreeting(context.getUser());
            sessionManager.markGreetingSent(context.getSessionId());
            sessionManager.updateSessionState(context.getSessionId(), ConversationState.INTENT_PREDICTION);
            
            // Add proactive suggestions
            List<Intent> predictions = intentDetectionService.predictIntent(context.getUser());
            String proactiveSuggestions = generateProactiveSuggestions(predictions);
            
            String fullResponse = greeting + "\n\n" + proactiveSuggestions;
            
            return new ChatMessage(
                "msg_" + System.currentTimeMillis(),
                context.getSessionId(),
                ChatMessage.MessageSender.BOT,
                fullResponse,
                java.time.LocalDateTime.now(),
                ChatMessage.MessageType.TEXT
            );
        } else {
            // Handle non-greeting intent
            return handleIntentHandlingState(message, context);
        }
    }
    
    /**
     * Handle intent prediction state - provide proactive suggestions
     */
    private ChatMessage handleIntentPredictionState(String message, SessionContext context) {
        Intent detectedIntent = intentDetectionService.detectIntent(message);
        
        if (detectedIntent.getIntentName() != Intent.IntentName.UNKNOWN) {
            context.setCurrentIntent(detectedIntent);
            sessionManager.updateSessionState(context.getSessionId(), ConversationState.INTENT_HANDLING);
            return handleIntentHandlingState(message, context);
        } else {
            // Provide proactive suggestions
            List<Intent> predictions = intentDetectionService.predictIntent(context.getUser());
            String suggestions = generateProactiveSuggestions(predictions);
            
            return new ChatMessage(
                "msg_" + System.currentTimeMillis(),
                context.getSessionId(),
                ChatMessage.MessageSender.BOT,
                suggestions,
                java.time.LocalDateTime.now(),
                ChatMessage.MessageType.TEXT
            );
        }
    }
    
    /**
     * Handle intent handling state - process detected intent
     */
    private ChatMessage handleIntentHandlingState(String message, SessionContext context) {
        Intent detectedIntent = intentDetectionService.detectIntent(message);
        context.setCurrentIntent(detectedIntent);
        
        String response = handleIntentResponse(detectedIntent, context.getUser());
        
        // Move to feedback state after handling intent
        sessionManager.updateSessionState(context.getSessionId(), ConversationState.FEEDBACK);
        
        response += "\n\nIs there anything else I can help you with today?";
        
        return new ChatMessage(
            "msg_" + System.currentTimeMillis(),
            context.getSessionId(),
            ChatMessage.MessageSender.BOT,
            response,
            java.time.LocalDateTime.now(),
            ChatMessage.MessageType.TEXT
        );
    }
    
    /**
     * Handle feedback state - collect user feedback
     */
    private ChatMessage handleFeedbackState(String message, SessionContext context) {
        Intent detectedIntent = intentDetectionService.detectIntent(message);
        
        if (detectedIntent.getIntentName() == Intent.IntentName.FEEDBACK_COLLECTION) {
            sessionManager.updateSessionState(context.getSessionId(), ConversationState.COMPLETE);
            return new ChatMessage(
                "msg_" + System.currentTimeMillis(),
                context.getSessionId(),
                ChatMessage.MessageSender.BOT,
                "Thank you for your feedback! It helps us improve our service. Have a great day!",
                java.time.LocalDateTime.now(),
                ChatMessage.MessageType.FEEDBACK
            );
        } else if (detectedIntent.getIntentName() != Intent.IntentName.UNKNOWN) {
            // Handle new intent
            sessionManager.updateSessionState(context.getSessionId(), ConversationState.INTENT_HANDLING);
            return handleIntentHandlingState(message, context);
        } else {
            // Default response for feedback state
            return new ChatMessage(
                "msg_" + System.currentTimeMillis(),
                context.getSessionId(),
                ChatMessage.MessageSender.BOT,
                "Thank you for using our service! If you need help with anything else, just let me know.",
                java.time.LocalDateTime.now(),
                ChatMessage.MessageType.TEXT
            );
        }
    }
    
    /**
     * Handle complete state - conversation ended
     */
    private ChatMessage handleCompleteState(String message, SessionContext context) {
        // Check for new intent to restart conversation
        Intent detectedIntent = intentDetectionService.detectIntent(message);
        
        if (detectedIntent.getIntentName() != Intent.IntentName.UNKNOWN) {
            sessionManager.updateSessionState(context.getSessionId(), ConversationState.INTENT_HANDLING);
            return handleIntentHandlingState(message, context);
        } else {
            return new ChatMessage(
                "msg_" + System.currentTimeMillis(),
                context.getSessionId(),
                ChatMessage.MessageSender.BOT,
                "Hello again! How can I assist you with your credit card today?",
                java.time.LocalDateTime.now(),
                ChatMessage.MessageType.TEXT
            );
        }
    }
    
    /**
     * Get time-based greeting
     */
    private String getTimeBasedGreeting() {
        LocalTime currentTime = LocalTime.now();
        
        if (currentTime.isBefore(LocalTime.NOON)) {
            return "Good morning";
        } else if (currentTime.isBefore(LocalTime.of(17, 0))) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }
    
    /**
     * Generate proactive suggestions based on predictions
     */
    private String generateProactiveSuggestions(List<Intent> predictions) {
        if (predictions.isEmpty()) {
            return "How can I help you with your credit card today?";
        }
        
        StringBuilder suggestions = new StringBuilder();
        for (Intent prediction : predictions) {
            suggestions.append("💡 ").append(prediction.getResponseTemplate()).append("\n");
        }
        
        return suggestions.toString().trim();
    }
    
    /**
     * Generate payment inquiry response
     */
    private String generatePaymentInquiryResponse(User user) {
        return String.format(
            "Your current balance is $%.2f with an available credit of $%.2f. " +
            "Your payment status is %s and your next due date is %s.",
            user.getCurrentBalance(),
            user.getAvailableCredit(),
            user.getPaymentStatus().toString().toLowerCase(),
            user.getDueDate()
        );
    }
    
    /**
     * Generate statement response
     */
    private String generateStatementResponse(User user) {
        List<Transaction> transactions = mockDataService.getTransactionsByUserId(user.getUserId());
        return String.format(
            "You have %d recent transactions totaling $%.2f. " +
            "Would you like me to show you the details of your recent activity?",
            transactions.size(),
            transactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum()
        );
    }
    
    /**
     * Generate dispute response
     */
    private String generateDisputeResponse(User user) {
        return "I understand you want to dispute a transaction. " +
               "I can help you identify the transaction and guide you through the dispute process. " +
               "Which transaction would you like to dispute?";
    }
    
    /**
     * Generate feedback response
     */
    private String generateFeedbackResponse() {
        return "Thank you for wanting to provide feedback! " +
               "Your experience matters to us. How would you rate our service today?";
    }
}
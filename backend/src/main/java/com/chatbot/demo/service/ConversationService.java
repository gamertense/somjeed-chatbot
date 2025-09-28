package com.chatbot.demo.service;

import com.chatbot.demo.model.*;
import com.chatbot.demo.model.enums.ConversationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    
    // Duplicate transaction response templates
    private static final String DUPLICATE_TRANSACTION_BASE_RESPONSE = 
        "I understand you want to %s a duplicate transaction. " +
        "I've identified transactions that appear to be duplicates. " +
        "Would you like me to show you the details and help you %s?";
    
    private static final String DUPLICATE_TRANSACTION_GENERIC_RESPONSE = 
        "I understand you want to address a duplicate transaction. " +
        "I've identified transactions that appear to be duplicates. " +
        "Would you like me to show you the details and help you resolve this issue?";
    
    private static final String GENERAL_DISPUTE_RESPONSE = 
        "I understand you want to dispute a transaction. " +
        "I can help you identify the transaction and guide you through the dispute process. " +
        "Which transaction would you like to dispute?";
    
    // Greeting constants
    private static final String GREETING_MORNING = "Good morning";
    private static final String GREETING_AFTERNOON = "Good afternoon";
    private static final String GREETING_EVENING = "Good evening";
    
    // General response constants
    private static final String DEFAULT_HELP_MESSAGE = "How can I help you with your credit card today?";
    private static final String FEEDBACK_RESPONSE = 
        "Thank you for wanting to provide feedback! " +
        "Your experience matters to us. How would you rate our service today?";
    
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
        return processMessage(message, sessionId, null);
    }
    
    /**
     * Process incoming chat message and orchestrate conversation flow
     * 
     * @param message User message text
     * @param sessionId Session identifier
     * @param userId User identifier for new session creation (optional)
     * @return Chat response with appropriate content
     */
    public ChatMessage processMessage(String message, String sessionId, String userId) {
        // Get or create session context
        SessionContext sessionContext = sessionManager.getSessionContext(sessionId);
        String activeSessionId = sessionId;
        
        if (sessionContext == null) {
            // Determine which user to use for new session
            String targetUserId = userId != null ? userId : "user123"; // Default to user123 for backward compatibility
            User user = mockDataService.getUserById(targetUserId);
            
            if (user == null) {
                // Fallback to user123 if specified userId not found
                user = mockDataService.getUserById("user123");
            }
            
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
     * Generate contextual greeting with time, weather, and proactive prediction suggestions
     * 
     * @param user User context for personalization
     * @return Contextual greeting message with predictions
     */
    public String generateContextualGreeting(User user) {
        WeatherContext weather = weatherService.getCurrentWeather();
        String timeGreeting = getTimeBasedGreeting();
        String weatherText = weatherService.getGreetingWeatherText(weather.getCondition());
        
        // Build base greeting
        String baseGreeting = String.format("%s, %s", timeGreeting, weatherText);
        
        // Add proactive suggestions based on user context
        List<Intent> predictions = intentDetectionService.predictIntent(user);
        if (!predictions.isEmpty()) {
            String suggestions = generateProactiveSuggestions(predictions);
            return baseGreeting + "\n" + suggestions;
        }
        
        return baseGreeting;
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
            case TRANSACTION_DISPUTE -> generateDisputeResponse(intent, user);
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
            
            // Store the most likely prediction in session context for confirmation handling
            List<Intent> predictions = intentDetectionService.predictIntent(context.getUser());
            if (!predictions.isEmpty()) {
                context.setCurrentIntent(predictions.get(0)); // Store the first/most likely prediction
            }
            
            sessionManager.markGreetingSent(context.getSessionId());
            sessionManager.updateSessionState(context.getSessionId(), ConversationState.INTENT_PREDICTION);
            
            return new ChatMessage(
                "msg_" + System.currentTimeMillis(),
                context.getSessionId(),
                ChatMessage.MessageSender.BOT,
                greeting,
                java.time.LocalDateTime.now(),
                ChatMessage.MessageType.TEXT
            );
        } else {
            // Handle non-greeting intent
            return handleIntentHandlingState(message, context);
        }
    }
    
    /**
     * Handle intent prediction state - process confirmations or provide proactive suggestions
     */
    private ChatMessage handleIntentPredictionState(String message, SessionContext context) {
        // Check if user is confirming a predicted intent
        if (intentDetectionService.isConfirmationResponse(message, context.getCurrentIntent()) && context.getCurrentIntent() != null) {
            // For duplicate transaction context, store the specific action (cancel/report)
            if (context.getCurrentIntent().getIntentName() == Intent.IntentName.TRANSACTION_DISPUTE &&
                "duplicate_transaction".equals(context.getCurrentIntent().getParameters().get("suggestion"))) {
                if ("cancel".equalsIgnoreCase(message.trim())) {
                    context.getCurrentIntent().getParameters().put("action", "cancel");
                } else if ("report".equalsIgnoreCase(message.trim())) {
                    context.getCurrentIntent().getParameters().put("action", "report");
                }
            }
            // User confirmed the predicted intent, process it
            sessionManager.updateSessionState(context.getSessionId(), ConversationState.INTENT_HANDLING);
            return handleIntentHandlingState("", context); // Empty message since we're using stored intent
        }
        
        // Check if user is rejecting the prediction
        if (intentDetectionService.isNegativeResponse(message)) {
            context.setCurrentIntent(null); // Clear predicted intent
            return new ChatMessage(
                "msg_" + System.currentTimeMillis(),
                context.getSessionId(),
                ChatMessage.MessageSender.BOT,
                "No problem! How can I help you with your credit card today?",
                java.time.LocalDateTime.now(),
                ChatMessage.MessageType.TEXT
            );
        }
        
        // Try to detect a new intent from the message
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
     * Handle intent handling state - process detected or stored intent
     */
    private ChatMessage handleIntentHandlingState(String message, SessionContext context) {
        Intent intentToHandle;
        
        // If message is empty, use the stored intent (confirmation case)
        // Otherwise, detect intent from the message
        if (message == null || message.trim().isEmpty()) {
            intentToHandle = context.getCurrentIntent();
        } else {
            intentToHandle = intentDetectionService.detectIntent(message);
            context.setCurrentIntent(intentToHandle);
        }
        
        // If we still don't have an intent, use the stored one or fall back to unknown
        if (intentToHandle == null) {
            intentToHandle = context.getCurrentIntent();
        }
        
        String response = handleIntentResponse(intentToHandle, context.getUser());
        
        // Move to feedback state after handling intent
        sessionManager.updateSessionState(context.getSessionId(), ConversationState.FEEDBACK);
        
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
            return GREETING_MORNING;
        } else if (currentTime.isBefore(LocalTime.of(17, 0))) {
            return GREETING_AFTERNOON;
        } else {
            return GREETING_EVENING;
        }
    }
    
    /**
     * Generate proactive suggestions based on predictions
     */
    private String generateProactiveSuggestions(List<Intent> predictions) {
        if (predictions.isEmpty()) {
            return DEFAULT_HELP_MESSAGE;
        }
        
        StringBuilder suggestions = new StringBuilder();
        for (Intent prediction : predictions) {
            suggestions.append("💡 ").append(prediction.getResponseTemplate()).append("\n");
        }
        
        return suggestions.toString().trim();
    }
    
    /**
     * Generate payment inquiry response using enhanced MockDataService
     */
    private String generatePaymentInquiryResponse(User user) {
        PaymentSummary paymentSummary = mockDataService.getPaymentSummary(user.getUserId());
        
        if (paymentSummary == null) {
            return "I'm having trouble accessing your payment information right now. Please try again later.";
        }
        
        // Special handling for overdue payments to match the example format
        if (paymentSummary.getPaymentStatus() == User.PaymentStatus.OVERDUE) {
            String formattedDate = formatDateForDisplay(paymentSummary.getDueDate());
            return String.format(
                "Your current outstanding balance is %.0f THB, and your due date was %s.",
                paymentSummary.getOutstandingBalance(),
                formattedDate
            );
        } else if (MockDataService.SCENARIO_RECENT_PAYMENT.equals(mockDataService.getUserScenario(user.getUserId()))) {
            // Special handling for recent payment scenario - just show balance info
            return String.format(
                "Your outstanding balance is %.2f THB with an available credit of %.2f THB.",
                paymentSummary.getOutstandingBalance(),
                paymentSummary.getAvailableCredit()
            );
        } else {
            // General payment inquiry response with comprehensive information
            return String.format(
                "Your payment summary:\n" +
                "• Outstanding balance: %.2f THB\n" +
                "• Available credit: %.2f THB\n" +
                "• Payment status: %s\n" +
                "• Next due date: %s\n" +
                "• Last payment: %s",
                paymentSummary.getOutstandingBalance(),
                paymentSummary.getAvailableCredit(),
                paymentSummary.getPaymentStatus().toString().toLowerCase(),
                formatDateForDisplay(paymentSummary.getDueDate()),
                paymentSummary.getLastPaymentDate() != null ? formatDateForDisplay(paymentSummary.getLastPaymentDate()) : "No recent payment"
            );
        }
    }
    
    /**
     * Format date as "1 September 2025"
     */
    private String formatDateForDisplay(LocalDate date) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return date.getDayOfMonth() + " " + months[date.getMonthValue() - 1] + " " + date.getYear();
    }
    
    /**
     * Generate statement response using enhanced MockDataService
     */
    private String generateStatementResponse(User user) {
        // Get last 30 days of transactions
        LocalDate fromDate = LocalDate.now().minusDays(30);
        LocalDate toDate = LocalDate.now();
        
        List<Transaction> transactions = mockDataService.getTransactionHistory(user.getUserId(), fromDate, toDate);
        
        if (transactions.isEmpty()) {
            return "You don't have any transactions in the last 30 days. Your account shows no recent activity.";
        }
        
        double totalAmount = transactions.stream()
            .mapToDouble(t -> t.getAmount().doubleValue())
            .sum();
            
        String summary = String.format(
            "Here's your recent transaction summary (last 30 days):\n" +
            "• Total transactions: %d\n" +
            "• Total amount: %.2f THB\n" +
            "• Date range: %s to %s\n\n" +
            "Recent transactions:\n",
            transactions.size(),
            Math.abs(totalAmount), // Use absolute value for display
            formatDateForDisplay(fromDate),
            formatDateForDisplay(toDate)
        );
        
        // Add up to 3 most recent transactions
        StringBuilder recentTransactions = new StringBuilder();
        transactions.stream()
            .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate())) // Most recent first
            .limit(3)
            .forEach(txn -> {
                recentTransactions.append(String.format(
                    "• %s: %.2f THB (%s)\n",
                    formatDateForDisplay(txn.getTransactionDate().toLocalDate()),
                    Math.abs(txn.getAmount().doubleValue()),
                    txn.getMerchantName()
                ));
            });
        
        return summary + recentTransactions.toString() + 
               "\nWould you like me to show you more transaction details or help you with anything else?";
    }
    
    /**
     * Generate dispute response using enhanced MockDataService
     */
    private String generateDisputeResponse(Intent intent, User user) {
        // Check if this is a duplicate transaction scenario
        String scenario = mockDataService.getUserScenario(user.getUserId());
        if (MockDataService.SCENARIO_DUPLICATE_TRANSACTION.equals(scenario)) {
            // Check the action parameter to provide specific response
            String action = (String) intent.getParameters().get("action");
            if ("cancel".equals(action) || "report".equals(action)) {
                // Initiate dispute process for duplicate transaction
                DisputeCase disputeCase = mockDataService.initiateDisputeProcess(user.getUserId(), null);
                
                StringBuilder response = new StringBuilder();
                response.append(String.format(DUPLICATE_TRANSACTION_BASE_RESPONSE, action, action + " the duplicate transaction"));
                response.append("\n\n");
                response.append(String.join("\n• ", disputeCase.getNextSteps()));
                
                return response.toString();
            } else {
                // Generic response when no specific action is stored
                return DUPLICATE_TRANSACTION_GENERIC_RESPONSE;
            }
        } else {
            // General dispute process
            DisputeCase disputeCase = mockDataService.initiateDisputeProcess(user.getUserId(), null);
            
            StringBuilder response = new StringBuilder();
            response.append("I can help you dispute a transaction. ");
            response.append("Here's what happens next:\n\n");
            response.append("• " + String.join("\n• ", disputeCase.getNextSteps()));
            response.append("\n\nWhich specific transaction would you like to dispute? ");
            response.append("You can describe it or mention the amount and date.");
            
            return response.toString();
        }
    }
    
    /**
     * Generate feedback response using enhanced interaction
     */
    private String generateFeedbackResponse() {
        return "Thank you for wanting to provide feedback! " +
               "Your experience matters to us. Please tell me:\n" +
               "• How would you rate our service today? (1-5 stars)\n" +
               "• Any specific comments or suggestions?\n\n" +
               "I'll make sure your feedback reaches our service improvement team.";
    }
}
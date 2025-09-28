package com.chatbot.demo.service;

import com.chatbot.demo.model.Intent;
import com.chatbot.demo.model.Intent.IntentName;
import com.chatbot.demo.model.Transaction;
import com.chatbot.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Rule-based intent detection using keyword matching and pattern recognition.
 * Detects 5 core intents with confidence scoring and provides proactive predictions.
 */
@Service
public class IntentDetectionService {
    
    @Autowired
    private MockDataService mockDataService;
    
    // Intent keyword mappings for rule-based detection
    private static final Map<IntentName, List<String>> INTENT_KEYWORDS = Map.of(
        IntentName.GREETING, Arrays.asList("hi", "hello", "hey", "good morning", "good afternoon", "good evening", "greetings"),
        IntentName.PAYMENT_INQUIRY, Arrays.asList("payment", "balance", "overdue", "due date", "amount", "owe", "bill", "outstanding"),
        IntentName.E_STATEMENT, Arrays.asList("statement", "transactions", "summary", "history", "charges", "purchases", "spending"),
        IntentName.TRANSACTION_DISPUTE, Arrays.asList("dispute", "wrong charge", "cancel.*transaction", "refund", "incorrect", "unauthorized", "fraud", "dispute.*charge", "want.*dispute", "cancel", "report"),
        IntentName.FEEDBACK_COLLECTION, Arrays.asList("feedback", "rate", "experience", "service", "satisfied", "complaint", "suggestion")
    );
    
    // Intent response templates
    private static final Map<IntentName, String> RESPONSE_TEMPLATES = Map.of(
        IntentName.GREETING, "Hello! I'm here to help with your credit card questions.",
        IntentName.PAYMENT_INQUIRY, "I can help you check your payment information and outstanding balance.",
        IntentName.E_STATEMENT, "I can provide you with your transaction history and account summary.",
        IntentName.TRANSACTION_DISPUTE, "I understand you have concerns about a transaction. Let me help you with that.",
        IntentName.FEEDBACK_COLLECTION, "Your feedback is important to us. I'd like to hear about your experience."
    );
    
    /**
     * Detect intent from user message using rule-based keyword matching
     * 
     * @param message User message text
     * @return Intent with confidence score or UNKNOWN intent if no match
     */
    public Intent detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return createUnknownIntent();
        }
        
        String normalizedMessage = message.toLowerCase().trim();
        IntentName bestMatch = IntentName.UNKNOWN;
        float highestConfidence = 0.0f;
        List<String> matchedKeywords = new ArrayList<>();
        
        // Check dispute intent first for priority (to avoid conflicts with greeting keywords)
        for (IntentName intentName : new IntentName[]{IntentName.TRANSACTION_DISPUTE, IntentName.PAYMENT_INQUIRY, IntentName.E_STATEMENT, IntentName.FEEDBACK_COLLECTION, IntentName.GREETING}) {
            List<String> keywords = INTENT_KEYWORDS.get(intentName);
            
            List<String> foundKeywords = new ArrayList<>();
            float confidence = calculateConfidence(normalizedMessage, keywords, foundKeywords);
            
            if (confidence > highestConfidence) {
                highestConfidence = confidence;
                bestMatch = intentName;
                matchedKeywords = foundKeywords;
            }
        }
        
        // Return intent if confidence is above threshold
        if (highestConfidence >= 0.3f) {
            return new Intent(
                bestMatch,
                highestConfidence,
                new HashMap<>(),
                matchedKeywords,
                RESPONSE_TEMPLATES.get(bestMatch)
            );
        }
        
        return createUnknownIntent();
    }
    
    /**
     * Check if the message is a confirmation response (yes, sure, okay, etc.)
     * Also handles context-specific confirmations for duplicate transaction suggestions
     * 
     * @param message User message text
     * @param currentIntent Current intent context (optional)
     * @return true if message indicates confirmation
     */
    public boolean isConfirmationResponse(String message, Intent currentIntent) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String normalizedMessage = message.toLowerCase().trim();
        
        // Context-specific handling for duplicate transaction suggestions
        if (currentIntent != null && 
            currentIntent.getIntentName() == IntentName.TRANSACTION_DISPUTE &&
            "duplicate_transaction".equals(currentIntent.getParameters().get("suggestion"))) {
            // For duplicate transaction context, "cancel" and "report" are confirmations
            if (normalizedMessage.equals("cancel") || normalizedMessage.equals("report")) {
                return true;
            }
        }
        
        List<String> confirmationKeywords = Arrays.asList(
            "yes", "yeah", "yep", "yup", "sure", "okay", "ok", "alright", "right", 
            "correct", "absolutely", "definitely", "certainly", "of course", "please", "go ahead"
        );
        
        return confirmationKeywords.stream().anyMatch(keyword -> 
            normalizedMessage.equals(keyword) || normalizedMessage.matches("\\b" + keyword + "\\b.*")
        );
    }
    
    /**
     * Check if the message is a confirmation response (yes, sure, okay, etc.)
     * 
     * @param message User message text
     * @return true if message indicates confirmation
     */
    public boolean isConfirmationResponse(String message) {
        return isConfirmationResponse(message, null);
    }
    
    /**
     * Check if the message is a negative response (no, nope, cancel, etc.)
     * 
     * @param message User message text  
     * @return true if message indicates rejection
     */
    public boolean isNegativeResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String normalizedMessage = message.toLowerCase().trim();
        List<String> negativeKeywords = Arrays.asList(
            "no", "nope", "nah", "never", "cancel", "stop", "quit", "exit", "skip",
            "not interested", "not now", "maybe later", "not really"
        );
        
        return negativeKeywords.stream().anyMatch(keyword -> 
            normalizedMessage.equals(keyword) || normalizedMessage.matches("\\b" + keyword + "\\b.*")
        );
    }
    
    /**
     * Predict proactive intent suggestions based on user context
     * 
     * @param user User context for prediction
     * @return List of suggested intents based on user status
     */
    public List<Intent> predictIntent(User user) {
        List<Intent> predictions = new ArrayList<>();
        
        if (user == null) {
            return predictions;
        }
        
        // Get scenario type for enhanced prediction logic
        String scenarioType = mockDataService.getUserScenario(user.getUserId());
        
        // Scenario-based predictions for Story 1.3
        if (scenarioType != null) {
            switch (scenarioType) {
                case "OVERDUE_PAYMENT":
                    predictions.add(new Intent(
                        IntentName.PAYMENT_INQUIRY,
                        0.9f,
                        Map.of("suggestion", "overdue_payment"),
                        Arrays.asList("overdue", "payment"),
                        "Looks like your payment is overdue. Would you like to check your current outstanding balance?"
                    ));
                    break;
                    
                case "RECENT_PAYMENT":
                    predictions.add(new Intent(
                        IntentName.PAYMENT_INQUIRY,
                        0.8f,
                        Map.of("suggestion", "recent_payment"),
                        Arrays.asList("payment", "balance", "credit"),
                        "I see you received a payment confirmation today. Would you like to check your updated credit balance?"
                    ));
                    break;
                    
                case "DUPLICATE_TRANSACTION":
                    if (hasDuplicateTransactions(user.getUserId())) {
                        Map<String, Object> parameters = new HashMap<>();
                        parameters.put("suggestion", "duplicate_transaction");
                        predictions.add(new Intent(
                            IntentName.TRANSACTION_DISPUTE,
                            0.7f,
                            parameters,
                            Arrays.asList("duplicate", "cancel", "report", "transaction"),
                            "I notice you have similar transactions. Would you like to cancel or report it?"
                        ));
                    }
                    break;
            }
        }
        
        // Fallback to existing generic predictions for non-scenario users
        if (predictions.isEmpty()) {
            // Payment-related predictions
            if (user.getPaymentStatus() == User.PaymentStatus.OVERDUE) {
                predictions.add(new Intent(
                    IntentName.PAYMENT_INQUIRY,
                    0.8f,
                    Map.of("suggestion", "overdue_payment"),
                    Arrays.asList("overdue", "payment"),
                    "I notice you have an overdue payment. Would you like to check your outstanding balance?"
                ));
            }
            
            // Recent payment prediction
            if (user.getLastPaymentDate() != null && 
                user.getLastPaymentDate().isEqual(LocalDate.now())) {
                predictions.add(new Intent(
                    IntentName.PAYMENT_INQUIRY,
                    0.7f,
                    Map.of("suggestion", "recent_payment"),
                    Arrays.asList("payment", "balance"),
                    "I see you made a payment today. Would you like to see your updated credit balance?"
                ));
            }
            
            // Upcoming due date prediction
            if (user.getDueDate() != null && 
                user.getDueDate().isBefore(LocalDate.now().plusDays(3))) {
                predictions.add(new Intent(
                    IntentName.PAYMENT_INQUIRY,
                    0.6f,
                    Map.of("suggestion", "upcoming_due"),
                    Arrays.asList("due date", "payment"),
                    "Your payment is due soon. Would you like to review your balance?"
                ));
            }
            
            // High balance prediction - using more than 75% of credit limit
            if (user.getCurrentBalance() != null && user.getAvailableCredit() != null &&
                user.getCurrentBalance().doubleValue() >= (user.getAvailableCredit().doubleValue() * 0.75)) {
                predictions.add(new Intent(
                    IntentName.E_STATEMENT,
                    0.5f,
                    Map.of("suggestion", "high_usage"),
                    Arrays.asList("statement", "transactions"),
                    "You're using most of your credit limit. Would you like to review your recent transactions?"
                ));
            }
        }
        
        return predictions;
    }
    
    /**
     * Check if user has duplicate transactions (2+ transactions with same/similar amount within 48 hours)
     * 
     * @param userId User ID to check for duplicates
     * @return true if duplicate transactions found
     */
    private boolean hasDuplicateTransactions(String userId) {
        List<Transaction> transactions = mockDataService.getTransactionsByUserId(userId);
        
        if (transactions == null || transactions.size() < 2) {
            return false;
        }
        
        // Check for transactions with same amount within 48 hours
        for (int i = 0; i < transactions.size(); i++) {
            Transaction txn1 = transactions.get(i);
            
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction txn2 = transactions.get(j);
                
                // Check if amounts are the same or very similar (within $10)
                BigDecimal amount1 = txn1.getAmount().abs(); // Use absolute value to handle negative amounts
                BigDecimal amount2 = txn2.getAmount().abs();
                BigDecimal difference = amount1.subtract(amount2).abs();
                
                boolean similarAmounts = difference.compareTo(new BigDecimal("10.00")) <= 0;
                
                // Check if transactions are within 48 hours of each other
                LocalDateTime time1 = txn1.getTransactionDate();
                LocalDateTime time2 = txn2.getTransactionDate();
                long hoursDifference = Math.abs(ChronoUnit.HOURS.between(time1, time2));
                
                if (similarAmounts && hoursDifference <= 48) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Calculate confidence score based on keyword matches
     * 
     * @param message User message
     * @param keywords Keywords to match against (may include regex patterns)
     * @param foundKeywords Output list of matched keywords
     * @return Confidence score (0.0-1.0)
     */
    private float calculateConfidence(String message, List<String> keywords, List<String> foundKeywords) {
        float confidence = 0.0f;
        int matchedKeywords = 0;
        
        for (String keyword : keywords) {
            boolean matched = false;
            
            // Check if keyword contains regex patterns (indicated by .* or other special chars)
            if (keyword.contains(".*") || keyword.contains("\\b")) {
                // Use the keyword as-is regex pattern
                if (message.matches(".*" + keyword + ".*")) {
                    matched = true;
                }
            } else {
                // Use word boundary matching for literal keywords
                String regex = "\\b" + keyword.toLowerCase().replaceAll("\\s+", "\\\\s+") + "\\b";
                if (message.matches(".*" + regex + ".*")) {
                    matched = true;
                }
            }
            
            if (matched) {
                foundKeywords.add(keyword);
                matchedKeywords++;
                
                // Base score for each keyword match
                confidence += 0.3f;
                
                // Bonus for longer keywords (more specific)
                if (keyword.length() > 6) {
                    confidence += 0.1f;
                }
                
                // Bonus for phrase matches (contains space)
                if (keyword.contains(" ")) {
                    confidence += 0.2f;
                }
            }
        }
        
        // Normalize to 0.0-1.0 range, cap at 1.0
        return Math.min(1.0f, confidence);
    }    /**
     * Create unknown intent for unrecognized messages
     */
    private Intent createUnknownIntent() {
        return new Intent(
            IntentName.UNKNOWN,
            0.0f,
            new HashMap<>(),
            new ArrayList<>(),
            "I'm not sure how to help with that. Could you please rephrase your question about your credit card?"
        );
    }
}
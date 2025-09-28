package com.chatbot.demo.integration;

import com.chatbot.demo.model.ChatMessage;
import com.chatbot.demo.model.Intent;
import com.chatbot.demo.model.User;
import com.chatbot.demo.service.ConversationService;
import com.chatbot.demo.service.IntentDetectionService;
import com.chatbot.demo.service.MockDataService;
import com.chatbot.demo.service.SessionManager;
import com.chatbot.demo.model.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify the complete conversation flow for prediction confirmations
 */
@SpringBootTest
public class ConfirmationWorkflowIntegrationTest {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private MockDataService mockDataService;

    @Autowired
    private IntentDetectionService intentDetectionService;

    private String sessionId;
    private String userId;

    @BeforeEach
    void setUp() {
        sessionId = "test-session-" + System.currentTimeMillis();
        userId = "user_overdue";
    }

    @Test
    void testOverduePaymentConfirmationWorkflow() {
        // Step 1: Send initial greeting to get prediction
        ChatMessage greetingResponse = conversationService.processMessage("Hello", sessionId, userId);
        String greetingText = greetingResponse.getContent();
        
        // Verify greeting contains overdue payment prediction
        assertTrue(greetingText.contains("Looks like your payment is overdue"), 
            "Response should contain overdue payment prediction");
        assertTrue(greetingText.contains("Would you like to check your current outstanding balance?"), 
            "Response should ask for confirmation");

        // Step 2: User confirms with "Yes" - use the session ID from the greeting response
        ChatMessage confirmationResponse = conversationService.processMessage("Yes", greetingResponse.getSessionId(), userId);
        String confirmationText = confirmationResponse.getContent();
        
        // Verify the confirmation response provides specific payment information
        assertTrue(confirmationText.contains("120000 THB") || confirmationText.contains("120,000 THB"), 
            "Response should contain the specific outstanding balance");
        assertTrue(confirmationText.contains("1 September 2025"), 
            "Response should contain the due date in the expected format");
        assertTrue(confirmationText.contains("Your current outstanding balance"), 
            "Response should contain the expected message format");

        // Verify session context was properly managed
        SessionContext context = sessionManager.getSessionContext(greetingResponse.getSessionId());
        assertNotNull(context, "Session context should exist");
        assertNotNull(context.getCurrentIntent(), "Current intent should be stored");
    }

    @Test
    void testRecentPaymentConfirmationWorkflow() {
        // Use user with recent payment
        userId = "user_recent_payment";
        
        // Step 1: Send initial greeting to get prediction
        ChatMessage greetingResponse = conversationService.processMessage("Hello", sessionId, userId);
        String greetingText = greetingResponse.getContent();
        
        // Verify greeting contains recent payment prediction
        assertTrue(greetingText.contains("I see you received a payment confirmation today"), 
            "Response should contain recent payment prediction");

        // Step 2: User confirms with "Sure" - use the session ID from the greeting response
        ChatMessage confirmationResponse = conversationService.processMessage("Sure", greetingResponse.getSessionId(), userId);
        String confirmationText = confirmationResponse.getContent();
        
        // Verify the confirmation response provides confirmation details
        assertTrue(confirmationText.contains("payment confirmation") || confirmationText.contains("balance"), 
            "Response should contain payment confirmation details");
    }

    @Test
    void testDuplicateTransactionConfirmationWorkflow() {
        // Use user with duplicate transaction scenario
        userId = "user_duplicate_txn";
        
        // Step 1: Send initial greeting to get prediction
        ChatMessage greetingResponse = conversationService.processMessage("Hello", sessionId, userId);
        String greetingText = greetingResponse.getContent();
        
        // Verify greeting contains duplicate transaction prediction if duplicates exist
        User user = mockDataService.getUserById(userId);
        if (user != null && !mockDataService.getTransactionsByUserId(userId).isEmpty()) {
            // Step 2: User confirms with "okay" - use the session ID from the greeting response
            ChatMessage confirmationResponse = conversationService.processMessage("okay", greetingResponse.getSessionId(), userId);
            String confirmationText = confirmationResponse.getContent();
            
            // Verify the response addresses the duplicate transaction concern
            assertNotNull(confirmationText, "Response should not be null");
            assertFalse(confirmationText.trim().isEmpty(), "Response should not be empty");
        }
    }

    @Test
    void testNegativeResponse() {
        // Step 1: Send initial greeting to get prediction
        ChatMessage greetingResponse = conversationService.processMessage("Hello", sessionId, userId);
        String greetingText = greetingResponse.getContent();
        
        // Step 2: User rejects with "No" - use the session ID from the greeting response
        ChatMessage rejectionResponse = conversationService.processMessage("No", greetingResponse.getSessionId(), userId);
        String rejectionText = rejectionResponse.getContent();
        
        // Verify the rejection is handled appropriately
        assertTrue(rejectionText.contains("No problem") || 
                   rejectionText.contains("understand") ||
                   rejectionText.contains("anything else") ||
                   rejectionText.contains("help you with"), 
            "Response should acknowledge the rejection politely");
    }

    @Test
    void testConfirmationDetection() {
        // Test various confirmation phrases
        String[] confirmations = {"yes", "Yeah", "SURE", "okay", "yup", "definitely"};
        
        for (String confirmation : confirmations) {
            assertTrue(intentDetectionService.isConfirmationResponse(confirmation),
                "Should detect '" + confirmation + "' as confirmation");
        }
    }

    @Test
    void testNegativeDetection() {
        // Test various negative phrases
        String[] negatives = {"no", "Nope", "NEVER", "cancel", "skip", "not interested"};
        
        for (String negative : negatives) {
            assertTrue(intentDetectionService.isNegativeResponse(negative),
                "Should detect '" + negative + "' as negative response");
        }
    }
}
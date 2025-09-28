package com.chatbot.demo.integration;

import com.chatbot.demo.model.ChatMessage;
import com.chatbot.demo.model.SessionContext;
import com.chatbot.demo.service.ConversationService;
import com.chatbot.demo.service.SessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Simple test to verify actual response content for debugging
 */
@SpringBootTest
public class ResponseDebugTest {

    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private SessionManager sessionManager;

    @Test
    void debugOverdueUserResponse() {
        String sessionId = "debug-session-overdue";
        String userId = "user_overdue";
        
        // Step 1: Send initial greeting
        ChatMessage greetingResponse = conversationService.processMessage("Hello", sessionId, userId);
        System.out.println("=== GREETING RESPONSE ===");
        System.out.println(greetingResponse.getContent());
        System.out.println("Session ID: " + greetingResponse.getSessionId());
        
        SessionContext context1 = sessionManager.getSessionContext(greetingResponse.getSessionId());
        System.out.println("State after greeting: " + context1.getConversationState());
        System.out.println("Current intent: " + (context1.getCurrentIntent() != null ? context1.getCurrentIntent().getIntentName() : "null"));
        System.out.println("========================");

        // Step 2: Send "Yes" confirmation
        ChatMessage confirmationResponse = conversationService.processMessage("Yes", greetingResponse.getSessionId(), userId);
        System.out.println("=== CONFIRMATION RESPONSE ===");
        System.out.println(confirmationResponse.getContent());
        System.out.println("Session ID: " + confirmationResponse.getSessionId());
        
        SessionContext context2 = sessionManager.getSessionContext(confirmationResponse.getSessionId());
        System.out.println("State after confirmation: " + context2.getConversationState());
        System.out.println("Current intent: " + (context2.getCurrentIntent() != null ? context2.getCurrentIntent().getIntentName() : "null"));
        System.out.println("==============================");
    }

    @Test
    void debugRecentPaymentUserResponse() {
        String sessionId = "debug-session-recent";
        String userId = "user_recent_payment";
        
        // Step 1: Send initial greeting
        ChatMessage greetingResponse = conversationService.processMessage("Hello", sessionId, userId);
        System.out.println("=== RECENT PAYMENT GREETING RESPONSE ===");
        System.out.println(greetingResponse.getContent());
        System.out.println("=========================================");
    }
}
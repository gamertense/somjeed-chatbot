package com.chatbot.demo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for Intent model
 */
public class IntentTest {
    
    @Test
    public void testIntentCreation() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", "user123");
        
        // When
        Intent intent = new Intent(
            Intent.IntentName.PAYMENT_INQUIRY,
            0.95f,
            parameters,
            Arrays.asList("balance", "payment", "due"),
            "Your current balance is ${currentBalance}"
        );
        
        // Then
        assertEquals(Intent.IntentName.PAYMENT_INQUIRY, intent.getIntentName());
        assertEquals(0.95f, intent.getConfidence());
        assertEquals(parameters, intent.getParameters());
        assertEquals(3, intent.getTriggerKeywords().size());
        assertTrue(intent.getTriggerKeywords().contains("balance"));
    }
    
    @Test
    public void testIntentNameEnum() {
        // Test all enum values exist
        assertNotNull(Intent.IntentName.GREETING);
        assertNotNull(Intent.IntentName.PAYMENT_INQUIRY);
        assertNotNull(Intent.IntentName.E_STATEMENT);
        assertNotNull(Intent.IntentName.TRANSACTION_DISPUTE);
        assertNotNull(Intent.IntentName.FEEDBACK_COLLECTION);
        assertNotNull(Intent.IntentName.UNKNOWN);
    }
}
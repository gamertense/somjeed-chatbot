package com.chatbot.demo.service;

import com.chatbot.demo.model.Intent;
import com.chatbot.demo.model.Intent.IntentName;
import com.chatbot.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntentDetectionServiceTest {
    
    private IntentDetectionService intentDetectionService;
    
    @Mock
    private MockDataService mockDataService;
    
    @BeforeEach
    void setUp() {
        intentDetectionService = new IntentDetectionService();
        // Inject the mock using reflection for testing
        ReflectionTestUtils.setField(intentDetectionService, "mockDataService", mockDataService);
    }
    
    @Test
    void detectIntent_ShouldDetectGreetingIntent() {
        // Given
        String[] greetingMessages = {"hello", "hi there", "good morning", "hey", "greetings"};
        
        for (String message : greetingMessages) {
            // When
            Intent intent = intentDetectionService.detectIntent(message);
            
            // Then
            assertEquals(IntentName.GREETING, intent.getIntentName());
            assertTrue(intent.getConfidence() >= 0.3f);
            assertFalse(intent.getTriggerKeywords().isEmpty());
        }
    }
    
    @Test
    void detectIntent_ShouldDetectPaymentInquiryIntent() {
        // Given
        String[] paymentMessages = {
            "what's my balance?",
            "payment due date",
            "how much do I owe?",
            "outstanding amount",
            "overdue payment"
        };
        
        for (String message : paymentMessages) {
            // When
            Intent intent = intentDetectionService.detectIntent(message);
            
            // Then
            assertEquals(IntentName.PAYMENT_INQUIRY, intent.getIntentName());
            assertTrue(intent.getConfidence() >= 0.3f);
            assertFalse(intent.getTriggerKeywords().isEmpty());
        }
    }
    
    @Test
    void detectIntent_ShouldDetectEStatementIntent() {
        // Given
        String[] statementMessages = {
            "show me my statement",
            "recent transactions",
            "transaction history",
            "spending summary",
            "charges on my account"
        };
        
        for (String message : statementMessages) {
            // When
            Intent intent = intentDetectionService.detectIntent(message);
            
            // Then
            assertEquals(IntentName.E_STATEMENT, intent.getIntentName());
            assertTrue(intent.getConfidence() >= 0.3f);
            assertFalse(intent.getTriggerKeywords().isEmpty());
        }
    }
    
    @Test
    void detectIntent_ShouldDetectTransactionDisputeIntent() {
        // Given
        String[] disputeMessages = {
            "I want to dispute a charge",
            "wrong charge on my card",
            "cancel this transaction",
            "unauthorized purchase",
            "fraud on my account"
        };
        
        for (String message : disputeMessages) {
            // When
            Intent intent = intentDetectionService.detectIntent(message);
            
            // Then
            assertEquals(IntentName.TRANSACTION_DISPUTE, intent.getIntentName());
            assertTrue(intent.getConfidence() >= 0.3f);
            assertFalse(intent.getTriggerKeywords().isEmpty());
        }
    }
    
    @Test
    void detectIntent_ShouldDetectFeedbackCollectionIntent() {
        // Given
        String[] feedbackMessages = {
            "I want to give feedback",
            "rate your service",
            "my experience was good",
            "satisfied with help",
            "complaint about service"
        };
        
        for (String message : feedbackMessages) {
            // When
            Intent intent = intentDetectionService.detectIntent(message);
            
            // Then
            assertEquals(IntentName.FEEDBACK_COLLECTION, intent.getIntentName());
            assertTrue(intent.getConfidence() >= 0.3f);
            assertFalse(intent.getTriggerKeywords().isEmpty());
        }
    }
    
    @Test
    void detectIntent_ShouldReturnUnknownForUnrecognizedMessage() {
        // Given
        String unrecognizedMessage = "the weather is nice today";
        
        // When
        Intent intent = intentDetectionService.detectIntent(unrecognizedMessage);
        
        // Then
        assertEquals(IntentName.UNKNOWN, intent.getIntentName());
        assertEquals(0.0f, intent.getConfidence());
        assertTrue(intent.getTriggerKeywords().isEmpty());
    }
    
    @Test
    void detectIntent_ShouldReturnUnknownForEmptyMessage() {
        // When
        Intent intent1 = intentDetectionService.detectIntent("");
        Intent intent2 = intentDetectionService.detectIntent(null);
        Intent intent3 = intentDetectionService.detectIntent("   ");
        
        // Then
        assertEquals(IntentName.UNKNOWN, intent1.getIntentName());
        assertEquals(IntentName.UNKNOWN, intent2.getIntentName());
        assertEquals(IntentName.UNKNOWN, intent3.getIntentName());
    }
    
    @Test
    void detectIntent_ShouldAssignHigherConfidenceForExactPhraseMatch() {
        // Given
        String exactPhrase = "good morning";
        String partialMatch = "morning good";
        
        // When
        Intent exactIntent = intentDetectionService.detectIntent(exactPhrase);
        Intent partialIntent = intentDetectionService.detectIntent(partialMatch);
        
        // Then
        assertTrue(exactIntent.getConfidence() >= partialIntent.getConfidence());
    }
    
    @Test
    void predictIntent_ShouldPredictPaymentInquiryForOverdueUser() {
        // Given
        User overdueUser = new User(
            "user456",
            "****-****-****-5678",
            new BigDecimal("2850.75"),
            new BigDecimal("8000.00"),
            LocalDate.of(2025, 9, 20),
            LocalDate.of(2025, 8, 15),
            User.PaymentStatus.OVERDUE
        );
        
        // When
        List<Intent> predictions = intentDetectionService.predictIntent(overdueUser);
        
        // Then
        assertFalse(predictions.isEmpty());
        assertTrue(predictions.stream().anyMatch(intent -> 
            intent.getIntentName() == IntentName.PAYMENT_INQUIRY &&
            intent.getResponseTemplate().contains("overdue")));
    }
    
    @Test
    void predictIntent_ShouldPredictPaymentInquiryForRecentPayment() {
        // Given
        User recentPaymentUser = new User(
            "user123",
            "****-****-****-1234",
            new BigDecimal("1250.00"),
            new BigDecimal("5000.00"),
            LocalDate.of(2025, 10, 15),
            LocalDate.now(), // Payment today
            User.PaymentStatus.CURRENT
        );
        
        // When
        List<Intent> predictions = intentDetectionService.predictIntent(recentPaymentUser);
        
        // Then
        assertFalse(predictions.isEmpty());
        assertTrue(predictions.stream().anyMatch(intent -> 
            intent.getIntentName() == IntentName.PAYMENT_INQUIRY &&
            intent.getResponseTemplate().contains("payment today")));
    }
    
    @Test
    void predictIntent_ShouldPredictForUpcomingDueDate() {
        // Given
        User upcomingDueUser = new User(
            "user789",
            "****-****-****-9012",
            new BigDecimal("456.30"),
            new BigDecimal("3000.00"),
            LocalDate.now().plusDays(2), // Due in 2 days
            LocalDate.of(2025, 9, 15),
            User.PaymentStatus.UPCOMING
        );
        
        // When
        List<Intent> predictions = intentDetectionService.predictIntent(upcomingDueUser);
        
        // Then
        assertFalse(predictions.isEmpty());
        assertTrue(predictions.stream().anyMatch(intent -> 
            intent.getIntentName() == IntentName.PAYMENT_INQUIRY &&
            intent.getResponseTemplate().contains("due soon")));
    }
    
    @Test
    void predictIntent_ShouldPredictStatementForHighBalance() {
        // Given
        User highBalanceUser = new User(
            "user999",
            "****-****-****-9999",
            new BigDecimal("4000.00"), // 80% of 5000 credit limit (> 75% threshold)
            new BigDecimal("5000.00"),
            LocalDate.of(2025, 10, 15),
            LocalDate.of(2025, 9, 15),
            User.PaymentStatus.CURRENT
        );
        
        // When
        List<Intent> predictions = intentDetectionService.predictIntent(highBalanceUser);
        
        // Then
        assertFalse(predictions.isEmpty());
        assertTrue(predictions.stream().anyMatch(intent -> 
            intent.getIntentName() == IntentName.E_STATEMENT &&
            intent.getResponseTemplate().contains("credit limit")));
    }
    
    @Test
    void predictIntent_ShouldReturnEmptyForNullUser() {
        // When
        List<Intent> predictions = intentDetectionService.predictIntent(null);
        
        // Then
        assertTrue(predictions.isEmpty());
    }
    
    @Test
    void isConfirmationResponse_ShouldDetectBasicConfirmations() {
        // Given
        String[] confirmationMessages = {"yes", "sure", "okay", "ok"};
        
        for (String message : confirmationMessages) {
            // When & Then
            assertTrue(intentDetectionService.isConfirmationResponse(message));
        }
    }
    
    @Test
    void isNegativeResponse_ShouldDetectBasicNegativeResponses() {
        // Given
        String[] negativeMessages = {"no", "nope", "cancel"};
        
        for (String message : negativeMessages) {
            // When & Then
            assertTrue(intentDetectionService.isNegativeResponse(message));
        }
    }

    // Helper methods for test data
    
    private User createScenarioUser(String userId, User.PaymentStatus status) {
        return new User(
            userId,
            "****-****-****-1234",
            new BigDecimal("5000.00"),
            new BigDecimal("10000.00"),
            LocalDate.of(2025, 10, 15),
            LocalDate.of(2025, 9, 15),
            status
        );
    }
    
    private List<com.chatbot.demo.model.Transaction> createDuplicateTransactions() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return List.of(
            new com.chatbot.demo.model.Transaction("txn1", "user_duplicate_txn", new BigDecimal("2890.00"), 
                "Store A", now.minusHours(36), 
                com.chatbot.demo.model.Transaction.TransactionCategory.SHOPPING, 
                com.chatbot.demo.model.Transaction.TransactionStatus.COMPLETED),
            new com.chatbot.demo.model.Transaction("txn2", "user_duplicate_txn", new BigDecimal("2890.00"), 
                "Store B", now.minusHours(12), 
                com.chatbot.demo.model.Transaction.TransactionCategory.SHOPPING, 
                com.chatbot.demo.model.Transaction.TransactionStatus.COMPLETED)
        );
    }
}
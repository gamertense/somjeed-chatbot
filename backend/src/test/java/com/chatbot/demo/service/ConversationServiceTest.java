package com.chatbot.demo.service;

import com.chatbot.demo.model.*;
import com.chatbot.demo.model.enums.ConversationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {
    
    @Mock
    private IntentDetectionService intentDetectionService;
    
    @Mock
    private MockDataService mockDataService;
    
    @Mock
    private WeatherService weatherService;
    
    @Mock
    private SessionManager sessionManager;
    
    @InjectMocks
    private ConversationService conversationService;
    
    private User testUser;
    private SessionContext testSessionContext;
    private WeatherContext testWeatherContext;
    
    @BeforeEach
    void setUp() {
        testUser = new User(
            "user123",
            "****-****-****-1234",
            new BigDecimal("1250.00"),
            new BigDecimal("5000.00"),
            LocalDate.of(2025, 10, 15),
            LocalDate.of(2025, 9, 15),
            User.PaymentStatus.CURRENT
        );
        
        testSessionContext = new SessionContext("session123", testUser);
        
        testWeatherContext = new WeatherContext(
            WeatherContext.WeatherCondition.SUNNY,
            24,
            "Pleasant sunny day",
            java.time.LocalDateTime.now()
        );
    }
    
    @Test
    void processMessage_ShouldCreateNewSessionIfNotExists() {
        // Given
        String message = "hello";
        String sessionId = "new-session";
        String createdSessionId = "created-session";
        
        when(sessionManager.getSessionContext(sessionId)).thenReturn(null);
        when(mockDataService.getUserById("user123")).thenReturn(testUser);
        when(sessionManager.createSession(testUser)).thenReturn(createdSessionId);
        when(sessionManager.getSessionContext(createdSessionId)).thenReturn(testSessionContext);
        when(intentDetectionService.detectIntent(message))
            .thenReturn(new Intent(Intent.IntentName.GREETING, 0.8f, null, Arrays.asList("hello"), "Hello response"));
        when(weatherService.getCurrentWeather()).thenReturn(testWeatherContext);
        when(weatherService.getGreetingWeatherText(any())).thenReturn("on a sunshine day!");
        when(intentDetectionService.predictIntent(any())).thenReturn(Arrays.asList());
        
        // When
        ChatMessage result = conversationService.processMessage(message, sessionId);
        
        // Then
        assertNotNull(result);
        verify(sessionManager).createSession(testUser);
        verify(sessionManager).incrementMessageCount(createdSessionId);
    }
    
    @Test
    void processMessage_ShouldUseExistingSession() {
        // Given
        String message = "hello";
        String sessionId = "existing-session";
        
        when(sessionManager.getSessionContext(sessionId)).thenReturn(testSessionContext);
        when(intentDetectionService.detectIntent(message))
            .thenReturn(new Intent(Intent.IntentName.GREETING, 0.8f, null, Arrays.asList("hello"), "Hello response"));
        when(weatherService.getCurrentWeather()).thenReturn(testWeatherContext);
        when(weatherService.getGreetingWeatherText(any())).thenReturn("on a sunshine day!");
        when(intentDetectionService.predictIntent(any())).thenReturn(Arrays.asList());
        
        // When
        ChatMessage result = conversationService.processMessage(message, sessionId);
        
        // Then
        assertNotNull(result);
        verify(sessionManager, never()).createSession(any());
        verify(sessionManager).incrementMessageCount(sessionId);
    }
    
    @Test
    void generateContextualGreeting_ShouldCombineTimeAndWeather() {
        // Given
        when(weatherService.getCurrentWeather()).thenReturn(testWeatherContext);
        when(weatherService.getGreetingWeatherText(testWeatherContext.getCondition()))
            .thenReturn("on a sunshine day!");
        
        // When
        String greeting = conversationService.generateContextualGreeting(testUser);
        
        // Then
        assertNotNull(greeting);
        assertTrue(greeting.contains("Good"));
        assertTrue(greeting.contains("sunshine day"));
        verify(weatherService).getCurrentWeather();
        verify(weatherService).getGreetingWeatherText(testWeatherContext.getCondition());
    }
    
    @Test
    void handleIntentResponse_ShouldHandleGreetingIntent() {
        // Given
        Intent greetingIntent = new Intent(Intent.IntentName.GREETING, 0.8f, null, null, null);
        when(weatherService.getCurrentWeather()).thenReturn(testWeatherContext);
        when(weatherService.getGreetingWeatherText(any())).thenReturn("on a sunshine day!");
        
        // When
        String response = conversationService.handleIntentResponse(greetingIntent, testUser);
        
        // Then
        assertNotNull(response);
        assertTrue(response.contains("Good"));
    }
    
    @Test
    void handleIntentResponse_ShouldHandlePaymentInquiryIntent() {
        // Given
        Intent paymentIntent = new Intent(Intent.IntentName.PAYMENT_INQUIRY, 0.9f, null, null, null);
        PaymentSummary paymentSummary = new PaymentSummary(
            "user123",
            new BigDecimal("1250.00"),
            new BigDecimal("5000.00"),
            LocalDate.of(2025, 10, 15),
            User.PaymentStatus.CURRENT,
            LocalDate.of(2025, 9, 15)
        );
        when(mockDataService.getPaymentSummary(testUser.getUserId())).thenReturn(paymentSummary);
        
        // When
        String response = conversationService.handleIntentResponse(paymentIntent, testUser);
        
        // Then
        assertNotNull(response);
        assertTrue(response.contains("balance") || response.contains("payment"));
    }
    
    @Test
    void handleIntentResponse_ShouldHandleEStatementIntent() {
        // Given
        Intent statementIntent = new Intent(Intent.IntentName.E_STATEMENT, 0.7f, null, null, null);
        List<Transaction> transactions = Arrays.asList(
            new Transaction("txn1", "user123", new BigDecimal("100.00"), "Store", 
                java.time.LocalDateTime.now(), Transaction.TransactionCategory.SHOPPING, 
                Transaction.TransactionStatus.COMPLETED)
        );
        when(mockDataService.getTransactionHistory(eq(testUser.getUserId()), any(), any()))
            .thenReturn(transactions);
        
        // When
        String response = conversationService.handleIntentResponse(statementIntent, testUser);
        
        // Then
        assertNotNull(response);
        assertTrue(response.contains("transaction"));
    }
    
    @Test
    void handleIntentResponse_ShouldHandleTransactionDisputeIntent() {
        // Given
        Intent disputeIntent = new Intent(Intent.IntentName.TRANSACTION_DISPUTE, 0.8f, null, null, null);
        DisputeCase disputeCase = new DisputeCase(
            "DISP-123",
            "CASE123",
            "user123",
            null,
            java.time.LocalDateTime.now(),
            DisputeCase.DisputeStatus.INITIATED,
            Arrays.asList("Your dispute case has been created", "We will review within 5-10 business days")
        );
        when(mockDataService.initiateDisputeProcess(testUser.getUserId(), null))
            .thenReturn(disputeCase);
        
        // When
        String response = conversationService.handleIntentResponse(disputeIntent, testUser);
        
        // Then
        assertNotNull(response);
        assertTrue(response.contains("dispute"));
    }
    
    @Test
    void handleIntentResponse_ShouldHandleFeedbackCollectionIntent() {
        // Given
        Intent feedbackIntent = new Intent(Intent.IntentName.FEEDBACK_COLLECTION, 0.6f, null, null, null);
        
        // When
        String response = conversationService.handleIntentResponse(feedbackIntent, testUser);
        
        // Then
        assertNotNull(response);
        assertTrue(response.contains("feedback"));
    }
    
    @Test
    void handleIntentResponse_ShouldHandleUnknownIntent() {
        // Given
        Intent unknownIntent = new Intent(Intent.IntentName.UNKNOWN, 0.0f, null, null, null);
        
        // When
        String response = conversationService.handleIntentResponse(unknownIntent, testUser);
        
        // Then
        assertNotNull(response);
        assertTrue(response.contains("not sure"));
    }
    
    @Test
    void processMessage_ShouldHandleGreetingStateCorrectly() {
        // Given
        String message = "hello";
        String sessionId = "session123";
        testSessionContext.setConversationState(ConversationState.GREETING);
        
        when(sessionManager.getSessionContext(sessionId)).thenReturn(testSessionContext);
        when(intentDetectionService.detectIntent(message))
            .thenReturn(new Intent(Intent.IntentName.GREETING, 0.8f, null, Arrays.asList("hello"), "Hello response"));
        when(weatherService.getCurrentWeather()).thenReturn(testWeatherContext);
        when(weatherService.getGreetingWeatherText(any())).thenReturn("on a sunshine day!");
        when(intentDetectionService.predictIntent(testUser)).thenReturn(Arrays.asList());
        
        // When
        ChatMessage result = conversationService.processMessage(message, sessionId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getContent().contains("Good"));
        verify(sessionManager).markGreetingSent(sessionId);
        verify(sessionManager).updateSessionState(sessionId, ConversationState.INTENT_PREDICTION);
    }
    
    @Test
    void processMessage_ShouldHandleIntentHandlingState() {
        // Given
        String message = "what's my balance";
        String sessionId = "session123";
        testSessionContext.setConversationState(ConversationState.INTENT_HANDLING);
        
        PaymentSummary paymentSummary = new PaymentSummary(
            "user123",
            new BigDecimal("1250.00"),
            new BigDecimal("5000.00"),
            LocalDate.of(2025, 10, 15),
            User.PaymentStatus.CURRENT,
            LocalDate.of(2025, 9, 15)
        );
        
        when(sessionManager.getSessionContext(sessionId)).thenReturn(testSessionContext);
        when(intentDetectionService.detectIntent(message))
            .thenReturn(new Intent(Intent.IntentName.PAYMENT_INQUIRY, 0.9f, null, Arrays.asList("balance"), "Payment response"));
        when(mockDataService.getPaymentSummary(testUser.getUserId())).thenReturn(paymentSummary);
        
        // When
        ChatMessage result = conversationService.processMessage(message, sessionId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getContent().contains("balance") || result.getContent().contains("payment"));
        verify(sessionManager).updateSessionState(sessionId, ConversationState.FEEDBACK);
    }
    
    @Test
    void processMessage_ShouldHandleFeedbackState() {
        // Given
        String message = "thank you";
        String sessionId = "session123";
        testSessionContext.setConversationState(ConversationState.FEEDBACK);
        
        when(sessionManager.getSessionContext(sessionId)).thenReturn(testSessionContext);
        when(intentDetectionService.detectIntent(message))
            .thenReturn(new Intent(Intent.IntentName.UNKNOWN, 0.0f, null, Arrays.asList(), "Unknown"));
        
        // When
        ChatMessage result = conversationService.processMessage(message, sessionId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getContent().contains("Thank you"));
    }
    
    // Story 1.3 Integration Tests
    
    @Test
    void generateContextualGreeting_ShouldIncludePredictionsForScenarioUsers() {
        // Given
        User overdueUser = new User(
            "user_overdue",
            "****-****-****-4321",
            new BigDecimal("120000.00"),
            new BigDecimal("150000.00"),
            LocalDate.of(2025, 9, 1),
            LocalDate.of(2025, 8, 1),
            User.PaymentStatus.OVERDUE
        );
        
        Intent prediction = new Intent(
            Intent.IntentName.PAYMENT_INQUIRY,
            0.9f,
            java.util.Map.of("suggestion", "overdue_payment"),
            List.of("overdue", "payment"),
            "Looks like your payment is overdue. Would you like to check your current outstanding balance?"
        );
        
        when(weatherService.getCurrentWeather()).thenReturn(testWeatherContext);
        when(weatherService.getGreetingWeatherText(any())).thenReturn("on a sunshine day");
        when(intentDetectionService.predictIntent(overdueUser)).thenReturn(List.of(prediction));
        
        // When
        String greeting = conversationService.generateContextualGreeting(overdueUser);
        
        // Then
        assertTrue(greeting.contains("Good"));
        assertTrue(greeting.contains("sunshine day"));
        assertTrue(greeting.contains("💡 Looks like your payment is overdue"));
    }
    
    @Test
    void processMessage_GreetingState_ShouldIncludeScenarioPredictions() {
        // Given
        String message = "Hello";
        String sessionId = "session_overdue";
        
        User overdueUser = new User(
            "user_overdue",
            "****-****-****-4321",
            new BigDecimal("120000.00"),
            new BigDecimal("150000.00"),
            LocalDate.of(2025, 9, 1),
            LocalDate.of(2025, 8, 1),
            User.PaymentStatus.OVERDUE
        );
        
        SessionContext overdueContext = new SessionContext(
            sessionId,
            ConversationState.GREETING,
            overdueUser,
            null,
            java.time.LocalDateTime.now(),
            0,
            false
        );
        
        Intent greetingIntent = new Intent(Intent.IntentName.GREETING, 0.9f, null, List.of("hello"), "Hello");
        Intent prediction = new Intent(
            Intent.IntentName.PAYMENT_INQUIRY,
            0.9f,
            java.util.Map.of("suggestion", "overdue_payment"),
            List.of("overdue", "payment"),
            "Looks like your payment is overdue. Would you like to check your current outstanding balance?"
        );
        
        when(sessionManager.getSessionContext(sessionId)).thenReturn(overdueContext);
        when(intentDetectionService.detectIntent(message)).thenReturn(greetingIntent);
        when(weatherService.getCurrentWeather()).thenReturn(testWeatherContext);
        when(weatherService.getGreetingWeatherText(any())).thenReturn("on a sunshine day");
        when(intentDetectionService.predictIntent(overdueUser)).thenReturn(List.of(prediction));
        
        // When
        ChatMessage result = conversationService.processMessage(message, sessionId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getContent().contains("Good"));
        assertTrue(result.getContent().contains("sunshine day"));
        assertTrue(result.getContent().contains("💡 Looks like your payment is overdue"));
        verify(sessionManager).markGreetingSent(sessionId);
        verify(sessionManager).updateSessionState(sessionId, ConversationState.INTENT_PREDICTION);
    }
    
    @Test
    void processMessage_EndToEndScenarioFlow_OverduePayment() {
        // Given - Complete flow test: greeting → prediction response → intent handling
        String sessionId = "session_e2e_overdue";
        
        User overdueUser = new User(
            "user_overdue",
            "****-****-****-4321",
            new BigDecimal("120000.00"),
            new BigDecimal("150000.00"),
            LocalDate.of(2025, 9, 1),
            LocalDate.of(2025, 8, 1),
            User.PaymentStatus.OVERDUE
        );
        
        SessionContext context = new SessionContext(
            sessionId,
            ConversationState.GREETING,
            overdueUser,
            null,
            java.time.LocalDateTime.now(),
            0,
            false
        );
        
        Intent greetingIntent = new Intent(Intent.IntentName.GREETING, 0.9f, null, List.of("hello"), "Hello");
        Intent prediction = new Intent(
            Intent.IntentName.PAYMENT_INQUIRY,
            0.9f,
            java.util.Map.of("suggestion", "overdue_payment"),
            List.of("overdue", "payment"),
            "Looks like your payment is overdue. Would you like to check your current outstanding balance?"
        );
        
        when(sessionManager.getSessionContext(sessionId)).thenReturn(context);
        doNothing().when(sessionManager).incrementMessageCount(sessionId);
        when(intentDetectionService.detectIntent("Hello")).thenReturn(greetingIntent);
        when(weatherService.getCurrentWeather()).thenReturn(testWeatherContext);
        when(weatherService.getGreetingWeatherText(any())).thenReturn("on a sunshine day");
        when(intentDetectionService.predictIntent(overdueUser)).thenReturn(List.of(prediction));
        
        // When - User says hello
        ChatMessage greetingResponse = conversationService.processMessage("Hello", sessionId);
        
        // Then - Should get greeting with prediction
        assertNotNull(greetingResponse);
        assertTrue(greetingResponse.getContent().contains("Good"));
        assertTrue(greetingResponse.getContent().contains("💡 Looks like your payment is overdue"));
        
        verify(sessionManager).markGreetingSent(sessionId);
        verify(sessionManager).updateSessionState(sessionId, ConversationState.INTENT_PREDICTION);
    }
}
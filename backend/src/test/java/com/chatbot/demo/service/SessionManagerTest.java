package com.chatbot.demo.service;

import com.chatbot.demo.model.SessionContext;
import com.chatbot.demo.model.User;
import com.chatbot.demo.model.enums.ConversationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SessionManagerTest {
    
    private SessionManager sessionManager;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        testUser = new User(
            "user123",
            "****-****-****-1234",
            new BigDecimal("1250.00"),
            new BigDecimal("5000.00"),
            LocalDate.of(2025, 10, 15),
            LocalDate.of(2025, 9, 15),
            User.PaymentStatus.CURRENT
        );
    }
    
    @Test
    void createSession_ShouldReturnValidSessionId() {
        // When
        String sessionId = sessionManager.createSession(testUser);
        
        // Then
        assertNotNull(sessionId);
        assertFalse(sessionId.trim().isEmpty());
        assertTrue(sessionManager.isValidSession(sessionId));
    }
    
    @Test
    void getSessionContext_ShouldReturnCorrectContext() {
        // Given
        String sessionId = sessionManager.createSession(testUser);
        
        // When
        SessionContext context = sessionManager.getSessionContext(sessionId);
        
        // Then
        assertNotNull(context);
        assertEquals(sessionId, context.getSessionId());
        assertEquals(testUser, context.getUser());
        assertEquals(ConversationState.GREETING, context.getConversationState());
        assertEquals(0, context.getMessageCount());
        assertFalse(context.isHasReceivedGreeting());
    }
    
    @Test
    void getSessionContext_ShouldReturnNullForInvalidSessionId() {
        // When
        SessionContext context = sessionManager.getSessionContext("invalid-session-id");
        
        // Then
        assertNull(context);
    }
    
    @Test
    void updateSessionState_ShouldChangeConversationState() {
        // Given
        String sessionId = sessionManager.createSession(testUser);
        
        // When
        SessionContext updatedContext = sessionManager.updateSessionState(sessionId, ConversationState.INTENT_HANDLING);
        
        // Then
        assertNotNull(updatedContext);
        assertEquals(ConversationState.INTENT_HANDLING, updatedContext.getConversationState());
    }
    
    @Test
    void incrementMessageCount_ShouldIncrementCorrectly() {
        // Given
        String sessionId = sessionManager.createSession(testUser);
        
        // When
        sessionManager.incrementMessageCount(sessionId);
        sessionManager.incrementMessageCount(sessionId);
        
        // Then
        SessionContext context = sessionManager.getSessionContext(sessionId);
        assertEquals(2, context.getMessageCount());
    }
    
    @Test
    void markGreetingSent_ShouldSetGreetingFlag() {
        // Given
        String sessionId = sessionManager.createSession(testUser);
        
        // When
        sessionManager.markGreetingSent(sessionId);
        
        // Then
        SessionContext context = sessionManager.getSessionContext(sessionId);
        assertTrue(context.isHasReceivedGreeting());
    }
    
    @Test
    void removeSession_ShouldRemoveSessionFromStorage() {
        // Given
        String sessionId = sessionManager.createSession(testUser);
        assertTrue(sessionManager.isValidSession(sessionId));
        
        // When
        sessionManager.removeSession(sessionId);
        
        // Then
        assertFalse(sessionManager.isValidSession(sessionId));
        assertNull(sessionManager.getSessionContext(sessionId));
    }
    
    @Test
    void getActiveSessionCount_ShouldReturnCorrectCount() {
        // Given
        assertEquals(0, sessionManager.getActiveSessionCount());
        
        // When
        String sessionId1 = sessionManager.createSession(testUser);
        String sessionId2 = sessionManager.createSession(testUser);
        
        // Then
        assertEquals(2, sessionManager.getActiveSessionCount());
        
        // When
        sessionManager.removeSession(sessionId1);
        
        // Then
        assertEquals(1, sessionManager.getActiveSessionCount());
    }
    
    @Test
    void sessionManager_ShouldBeThreadSafe() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numberOfThreads = 10;
        int sessionsPerThread = 10;
        
        // When - Create sessions concurrently
        CompletableFuture<Void>[] futures = IntStream.range(0, numberOfThreads)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                for (int j = 0; j < sessionsPerThread; j++) {
                    String sessionId = sessionManager.createSession(testUser);
                    sessionManager.incrementMessageCount(sessionId);
                    sessionManager.updateSessionState(sessionId, ConversationState.INTENT_HANDLING);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Then
        assertEquals(numberOfThreads * sessionsPerThread, sessionManager.getActiveSessionCount());
    }
    
    @Test
    void getSessionContext_ShouldUpdateLastActivity() {
        // Given
        String sessionId = sessionManager.createSession(testUser);
        SessionContext originalContext = sessionManager.getSessionContext(sessionId);
        LocalDateTime originalActivity = originalContext.getLastActivity();
        
        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        SessionContext updatedContext = sessionManager.getSessionContext(sessionId);
        
        // Then
        assertTrue(updatedContext.getLastActivity().isAfter(originalActivity));
    }
    
    @Test
    void cleanupInactiveSessions_ShouldNotRemoveActiveSessions() {
        // Given
        String sessionId = sessionManager.createSession(testUser);
        int initialCount = sessionManager.getActiveSessionCount();
        
        // When
        sessionManager.cleanupInactiveSessions();
        
        // Then
        assertEquals(initialCount, sessionManager.getActiveSessionCount());
        assertTrue(sessionManager.isValidSession(sessionId));
    }
}
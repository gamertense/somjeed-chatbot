package com.chatbot.demo.service;

import com.chatbot.demo.model.FeedbackResponse;
import com.chatbot.demo.model.FeedbackResponse.FeedbackRating;
import com.chatbot.demo.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private SessionManager sessionManager;

    @InjectMocks
    private FeedbackService feedbackService;

    private static final String TEST_SESSION_ID = "test-session-123";
    private static final String TEST_USER_ID = "user123";
    private static final FeedbackRating TEST_RATING = FeedbackRating.HAPPY;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(feedbackRepository, sessionManager);
    }

    @Test
    void submitFeedback_ValidSession_ShouldSubmitSuccessfully() {
        // Arrange
        when(sessionManager.sessionExists(TEST_SESSION_ID)).thenReturn(true);
        when(feedbackRepository.existsBySessionId(TEST_SESSION_ID)).thenReturn(false);
        
        FeedbackResponse expectedResponse = createMockFeedbackResponse();
        when(feedbackRepository.save(any(FeedbackResponse.class))).thenReturn(expectedResponse);

        // Act
        FeedbackResponse result = feedbackService.submitFeedback(TEST_SESSION_ID, TEST_USER_ID, TEST_RATING);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_SESSION_ID, result.getSessionId());
        assertEquals(TEST_RATING, result.getRating());
        assertTrue(result.getAnonymous());
        assertNotNull(result.getTimestamp());

        // Verify interactions
        verify(sessionManager).sessionExists(TEST_SESSION_ID);
        verify(feedbackRepository).existsBySessionId(TEST_SESSION_ID);
        verify(feedbackRepository).save(any(FeedbackResponse.class));
        verify(sessionManager).completeSession(TEST_SESSION_ID);
    }

    @Test
    void submitFeedback_InvalidSession_ShouldThrowException() {
        // Arrange
        when(sessionManager.sessionExists(TEST_SESSION_ID)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> feedbackService.submitFeedback(TEST_SESSION_ID, TEST_USER_ID, TEST_RATING)
        );

        assertEquals("Session not found: " + TEST_SESSION_ID, exception.getMessage());
        
        // Verify no feedback was saved
        verify(feedbackRepository, never()).save(any(FeedbackResponse.class));
        verify(sessionManager, never()).completeSession(any());
    }

    @Test
    void submitFeedback_FeedbackAlreadyExists_ShouldReturnExisting() {
        // Arrange
        when(sessionManager.sessionExists(TEST_SESSION_ID)).thenReturn(true);
        when(feedbackRepository.existsBySessionId(TEST_SESSION_ID)).thenReturn(true);
        
        FeedbackResponse existingResponse = createMockFeedbackResponse();
        when(feedbackRepository.findBySessionId(TEST_SESSION_ID))
            .thenReturn(Collections.singletonList(existingResponse));

        // Act
        FeedbackResponse result = feedbackService.submitFeedback(TEST_SESSION_ID, TEST_USER_ID, TEST_RATING);

        // Assert
        assertEquals(existingResponse, result);
        
        // Verify no new feedback was saved
        verify(feedbackRepository, never()).save(any(FeedbackResponse.class));
        verify(sessionManager, never()).completeSession(any());
    }

    @Test
    void submitFeedback_SessionCompletionFails_ShouldStillSubmitFeedback() {
        // Arrange
        when(sessionManager.sessionExists(TEST_SESSION_ID)).thenReturn(true);
        when(feedbackRepository.existsBySessionId(TEST_SESSION_ID)).thenReturn(false);
        
        FeedbackResponse expectedResponse = createMockFeedbackResponse();
        when(feedbackRepository.save(any(FeedbackResponse.class))).thenReturn(expectedResponse);
        
        // Simulate session completion failure
        doThrow(new RuntimeException("Session completion error"))
            .when(sessionManager).completeSession(TEST_SESSION_ID);

        // Act
        FeedbackResponse result = feedbackService.submitFeedback(TEST_SESSION_ID, TEST_USER_ID, TEST_RATING);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        
        // Verify feedback was still saved despite session completion failure
        verify(feedbackRepository).save(any(FeedbackResponse.class));
    }

    @Test
    void getFeedbackBySession_ShouldReturnFeedbackList() {
        // Arrange
        List<FeedbackResponse> expectedFeedback = Arrays.asList(
            createMockFeedbackResponse(),
            createMockFeedbackResponse()
        );
        when(feedbackRepository.findBySessionId(TEST_SESSION_ID)).thenReturn(expectedFeedback);

        // Act
        List<FeedbackResponse> result = feedbackService.getFeedbackBySession(TEST_SESSION_ID);

        // Assert
        assertEquals(expectedFeedback, result);
        verify(feedbackRepository).findBySessionId(TEST_SESSION_ID);
    }

    @Test
    void getFeedbackStatistics_ShouldCalculateCorrectly() {
        // Arrange
        List<FeedbackResponse> mockFeedback = Arrays.asList(
            createMockFeedbackResponse(FeedbackRating.HAPPY),
            createMockFeedbackResponse(FeedbackRating.HAPPY),
            createMockFeedbackResponse(FeedbackRating.NEUTRAL),
            createMockFeedbackResponse(FeedbackRating.SAD)
        );
        when(feedbackRepository.findAll()).thenReturn(mockFeedback);

        // Act
        FeedbackService.FeedbackStatistics stats = feedbackService.getFeedbackStatistics();

        // Assert
        assertEquals(4L, stats.totalFeedback());
        assertEquals(2L, stats.happyCount());
        assertEquals(1L, stats.neutralCount());
        assertEquals(1L, stats.sadCount());
        assertEquals(50.0, stats.getHappyPercentage(), 0.01);
        assertEquals(25.0, stats.getNeutralPercentage(), 0.01);
        assertEquals(25.0, stats.getSadPercentage(), 0.01);
    }

    @Test
    void getFeedbackStatistics_EmptyFeedback_ShouldReturnZeros() {
        // Arrange
        when(feedbackRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        FeedbackService.FeedbackStatistics stats = feedbackService.getFeedbackStatistics();

        // Assert
        assertEquals(0L, stats.totalFeedback());
        assertEquals(0L, stats.happyCount());
        assertEquals(0L, stats.neutralCount());
        assertEquals(0L, stats.sadCount());
        assertEquals(0.0, stats.getHappyPercentage());
        assertEquals(0.0, stats.getNeutralPercentage());
        assertEquals(0.0, stats.getSadPercentage());
    }

    @Test
    void hasFeedback_ShouldReturnCorrectStatus() {
        // Test when feedback exists
        when(feedbackRepository.existsBySessionId(TEST_SESSION_ID)).thenReturn(true);
        assertTrue(feedbackService.hasFeedback(TEST_SESSION_ID));

        // Test when feedback doesn't exist
        when(feedbackRepository.existsBySessionId(TEST_SESSION_ID)).thenReturn(false);
        assertFalse(feedbackService.hasFeedback(TEST_SESSION_ID));
    }

    private FeedbackResponse createMockFeedbackResponse() {
        return createMockFeedbackResponse(TEST_RATING);
    }

    private FeedbackResponse createMockFeedbackResponse(FeedbackRating rating) {
        FeedbackResponse response = new FeedbackResponse();
        response.setFeedbackId("feedback-123");
        response.setSessionId(TEST_SESSION_ID);
        response.setRating(rating);
        response.setAnonymous(true);
        response.setTimestamp(java.time.LocalDateTime.now());
        return response;
    }
}
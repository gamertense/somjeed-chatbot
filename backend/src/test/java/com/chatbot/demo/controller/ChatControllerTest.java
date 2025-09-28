package com.chatbot.demo.controller;

import com.chatbot.demo.dto.ChatRequest;
import com.chatbot.demo.model.ChatMessage;
import com.chatbot.demo.model.User;
import com.chatbot.demo.model.WeatherContext;
import com.chatbot.demo.service.ConversationService;
import com.chatbot.demo.service.FeedbackService;
import com.chatbot.demo.service.MockDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for ChatController
 */
@WebMvcTest(ChatController.class)
public class ChatControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ConversationService conversationService;
    
    @MockBean
    private MockDataService mockDataService;
    
    @MockBean
    private FeedbackService feedbackService;
    
    @Test
    public void testChatEndpointWithValidGreeting() throws Exception {
        // Given
        ChatRequest request = new ChatRequest("Hello", null, "user123");
        
        // Mock the conversation service response
        ChatMessage mockChatMessage = new ChatMessage(
            "msg123",
            "session123",
            ChatMessage.MessageSender.BOT,
            "Good morning! Welcome to SOMJEED Credit Card services. How can I assist you today?",
            LocalDateTime.now(),
            ChatMessage.MessageType.TEXT
        );
        
        when(conversationService.processMessage(eq("Hello"), anyString(), eq("user123")))
            .thenReturn(mockChatMessage);
        
        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value("session123"))
            .andExpect(jsonPath("$.botMessage").value("Good morning! Welcome to SOMJEED Credit Card services. How can I assist you today?"))
            .andExpect(jsonPath("$.messageType").value("GREETING"))
            .andExpect(jsonPath("$.isSessionComplete").value(false))
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
    
    @Test
    public void testChatEndpointWithInvalidRequest() throws Exception {
        // Given - Empty message should fail validation
        ChatRequest request = new ChatRequest("", null, "user123");
        
        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
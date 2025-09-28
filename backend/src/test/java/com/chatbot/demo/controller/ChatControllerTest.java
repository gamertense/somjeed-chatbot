package com.chatbot.demo.controller;

import com.chatbot.demo.dto.ChatRequest;
import com.chatbot.demo.service.MockDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
    private MockDataService mockDataService;
    
    @Test
    public void testChatEndpointWithValidGreeting() throws Exception {
        // Given
        ChatRequest request = new ChatRequest("Hello", null, "user123");
        
        // When & Then
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").isNotEmpty())
            .andExpect(jsonPath("$.botMessage").isNotEmpty())
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
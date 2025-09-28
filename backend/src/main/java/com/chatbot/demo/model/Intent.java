package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents detected or predicted user intents with confidence scoring.
 * 
 * TypeScript Interface:
 * interface Intent {
 *   intentName: "GREETING" | "PAYMENT_INQUIRY" | "E_STATEMENT" | "TRANSACTION_DISPUTE" | "FEEDBACK_COLLECTION" | "UNKNOWN";
 *   confidence: number; // 0.0 to 1.0
 *   parameters: Record<string, any>;
 *   triggerKeywords: string[];
 *   responseTemplate: string;
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Intent {
    
    /**
     * Intent identifier
     */
    private IntentName intentName;
    
    /**
     * Confidence score (0.0-1.0)
     */
    private Float confidence;
    
    /**
     * Intent-specific parameters
     */
    private Map<String, Object> parameters;
    
    /**
     * Keywords that triggered this intent
     */
    private List<String> triggerKeywords;
    
    /**
     * Template for bot response
     */
    private String responseTemplate;
    
    /**
     * Intent name enum
     */
    public enum IntentName {
        GREETING, PAYMENT_INQUIRY, E_STATEMENT, TRANSACTION_DISPUTE, FEEDBACK_COLLECTION, UNKNOWN
    }
}
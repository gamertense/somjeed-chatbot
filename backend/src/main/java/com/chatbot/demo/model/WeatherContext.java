package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mock weather data for contextual greetings.
 * 
 * TypeScript Interface:
 * interface WeatherContext {
 *   condition: "SUNNY" | "CLOUDY" | "RAINY" | "STORMY";
 *   temperature: number;
 *   description: string;
 *   timestamp: string; // ISO datetime format
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherContext {
    
    /**
     * Current weather condition
     */
    private WeatherCondition condition;
    
    /**
     * Temperature in Celsius
     */
    private Integer temperature;
    
    /**
     * Human-readable weather description
     */
    private String description;
    
    /**
     * Weather data timestamp
     */
    private LocalDateTime timestamp;
    
    /**
     * Weather condition enum
     */
    public enum WeatherCondition {
        SUNNY, CLOUDY, RAINY, STORMY
    }
}
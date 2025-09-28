package com.chatbot.demo.service;

import com.chatbot.demo.model.WeatherContext;
import com.chatbot.demo.model.WeatherContext.WeatherCondition;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Mock weather service for generating contextual greetings.
 * Provides randomized but realistic weather conditions for greeting messages.
 */
@Service
public class WeatherService {
    
    private final Random random = new Random();
    
    /**
     * Get current mock weather data for contextual greetings
     * 
     * @return WeatherContext with randomized weather condition and temperature
     */
    public WeatherContext getCurrentWeather() {
        WeatherCondition condition = getRandomWeatherCondition();
        Integer temperature = generateTemperatureForCondition(condition);
        String description = getWeatherDescription(condition);
        
        return new WeatherContext(condition, temperature, description, LocalDateTime.now());
    }
    
    /**
     * Generate weather description for greetings based on condition.
     * Maps to PRD specifications for weather-based greeting messages.
     * 
     * @param condition The weather condition
     * @return Greeting-appropriate weather description
     */
    public String getGreetingWeatherText(WeatherCondition condition) {
        switch (condition) {
            case SUNNY:
                return "on a sunshine day!";
            case CLOUDY:
                return "a bit cloudy but I'm here to help!";
            case RAINY:
                return "stay dry out there!";
            case STORMY:
                return "let me help make your stormy day better.";
            default:
                return "hope you're having a great day!";
        }
    }
    
    /**
     * Get random weather condition for mock data
     */
    private WeatherCondition getRandomWeatherCondition() {
        WeatherCondition[] conditions = WeatherCondition.values();
        return conditions[random.nextInt(conditions.length)];
    }
    
    /**
     * Generate realistic temperature based on weather condition
     */
    private Integer generateTemperatureForCondition(WeatherCondition condition) {
        switch (condition) {
            case SUNNY:
                return 22 + random.nextInt(8); // 22-29°C
            case CLOUDY:
                return 18 + random.nextInt(6); // 18-23°C
            case RAINY:
                return 15 + random.nextInt(5); // 15-19°C
            case STORMY:
                return 12 + random.nextInt(8); // 12-19°C
            default:
                return 20; // Default temperature
        }
    }
    
    /**
     * Get descriptive weather text for general use
     */
    private String getWeatherDescription(WeatherCondition condition) {
        switch (condition) {
            case SUNNY:
                return "Clear skies and sunshine";
            case CLOUDY:
                return "Overcast with some clouds";
            case RAINY:
                return "Light rain showers";
            case STORMY:
                return "Thunderstorms with heavy rain";
            default:
                return "Pleasant weather";
        }
    }
}
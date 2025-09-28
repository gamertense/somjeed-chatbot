package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents credit card customer data for intent prediction and personalized responses.
 * 
 * TypeScript Interface:
 * interface User {
 *   userId: string;
 *   cardNumber: string; // e.g., "****-****-****-1234"
 *   currentBalance: number;
 *   availableCredit: number;
 *   dueDate: string; // ISO date format
 *   lastPaymentDate: string; // ISO date format
 *   paymentStatus: "CURRENT" | "OVERDUE" | "UPCOMING";
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * Unique identifier for user session
     */
    private String userId;
    
    /**
     * Masked credit card number (last 4 digits)
     */
    private String cardNumber;
    
    /**
     * Outstanding balance amount
     */
    private BigDecimal currentBalance;
    
    /**
     * Available credit limit
     */
    private BigDecimal availableCredit;
    
    /**
     * Payment due date
     */
    private LocalDate dueDate;
    
    /**
     * Most recent payment date
     */
    private LocalDate lastPaymentDate;
    
    /**
     * Current payment status
     */
    private PaymentStatus paymentStatus;
    
    /**
     * Payment status enum
     */
    public enum PaymentStatus {
        CURRENT, OVERDUE, UPCOMING
    }
}
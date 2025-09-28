package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents credit card transactions for dispute handling and transaction inquiries.
 * 
 * TypeScript Interface:
 * interface Transaction {
 *   transactionId: string;
 *   userId: string;
 *   amount: number;
 *   merchantName: string;
 *   transactionDate: string; // ISO datetime format
 *   category: "DINING" | "SHOPPING" | "TRAVEL" | "UTILITIES" | "OTHER";
 *   status: "PENDING" | "COMPLETED" | "DISPUTED";
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    /**
     * Unique transaction identifier
     */
    private String transactionId;
    
    /**
     * Reference to user
     */
    private String userId;
    
    /**
     * Transaction amount
     */
    private BigDecimal amount;
    
    /**
     * Merchant or vendor name
     */
    private String merchantName;
    
    /**
     * When transaction occurred
     */
    private LocalDateTime transactionDate;
    
    /**
     * Transaction category
     */
    private TransactionCategory category;
    
    /**
     * Transaction status
     */
    private TransactionStatus status;
    
    /**
     * Transaction category enum
     */
    public enum TransactionCategory {
        DINING, SHOPPING, TRAVEL, UTILITIES, OTHER
    }
    
    /**
     * Transaction status enum
     */
    public enum TransactionStatus {
        PENDING, COMPLETED, DISPUTED
    }
}
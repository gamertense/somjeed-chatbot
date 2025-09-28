package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payment summary data for PAYMENT_INQUIRY intent responses.
 * Contains user payment status and balance information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummary {
    
    /**
     * User identifier
     */
    private String userId;
    
    /**
     * Current outstanding balance
     */
    private BigDecimal outstandingBalance;
    
    /**
     * Available credit limit
     */
    private BigDecimal availableCredit;
    
    /**
     * Payment due date
     */
    private LocalDate dueDate;
    
    /**
     * Current payment status
     */
    private User.PaymentStatus paymentStatus;
    
    /**
     * Last payment date
     */
    private LocalDate lastPaymentDate;
}
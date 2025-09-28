package com.chatbot.demo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unit tests for User model
 */
public class UserTest {
    
    @Test
    public void testUserCreation() {
        // Given
        String userId = "user123";
        String cardNumber = "****-****-****-1234";
        BigDecimal balance = new BigDecimal("1250.00");
        BigDecimal credit = new BigDecimal("5000.00");
        LocalDate dueDate = LocalDate.of(2025, 10, 15);
        LocalDate lastPayment = LocalDate.of(2025, 9, 15);
        
        // When
        User user = new User(userId, cardNumber, balance, credit, dueDate, lastPayment, User.PaymentStatus.CURRENT);
        
        // Then
        assertEquals(userId, user.getUserId());
        assertEquals(cardNumber, user.getCardNumber());
        assertEquals(balance, user.getCurrentBalance());
        assertEquals(credit, user.getAvailableCredit());
        assertEquals(dueDate, user.getDueDate());
        assertEquals(lastPayment, user.getLastPaymentDate());
        assertEquals(User.PaymentStatus.CURRENT, user.getPaymentStatus());
    }
    
    @Test
    public void testPaymentStatusEnum() {
        // Test all enum values exist
        assertNotNull(User.PaymentStatus.CURRENT);
        assertNotNull(User.PaymentStatus.OVERDUE);
        assertNotNull(User.PaymentStatus.UPCOMING);
    }
}
package com.chatbot.demo.service;

import com.chatbot.demo.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Unit tests for MockDataService
 */
public class MockDataServiceTest {
    
    private MockDataService mockDataService;
    
    @BeforeEach
    public void setUp() {
        mockDataService = new MockDataService();
    }
    
    @Test
    public void testGetUserById() {
        // When
        User user123 = mockDataService.getUserById("user123");
        User nonExistentUser = mockDataService.getUserById("nonexistent");
        
        // Then
        assertNotNull(user123);
        assertEquals("user123", user123.getUserId());
        assertEquals("****-****-****-1234", user123.getCardNumber());
        assertEquals(new BigDecimal("1250.00"), user123.getCurrentBalance());
        assertEquals(User.PaymentStatus.CURRENT, user123.getPaymentStatus());
        
        assertNull(nonExistentUser);
    }
    
    @Test
    public void testGetAllUsers() {
        // When
        var allUsers = mockDataService.getAllUsers();
        
        // Then
        assertEquals(6, allUsers.size()); // Updated to include 3 scenario users
        assertTrue(allUsers.stream().anyMatch(u -> "user123".equals(u.getUserId())));
        assertTrue(allUsers.stream().anyMatch(u -> "user456".equals(u.getUserId())));
        assertTrue(allUsers.stream().anyMatch(u -> "user789".equals(u.getUserId())));
        // Story 1.3 scenario users
        assertTrue(allUsers.stream().anyMatch(u -> "user_overdue".equals(u.getUserId())));
        assertTrue(allUsers.stream().anyMatch(u -> "user_recent_payment".equals(u.getUserId())));
        assertTrue(allUsers.stream().anyMatch(u -> "user_duplicate_txn".equals(u.getUserId())));
    }
    
    @Test
    public void testGetTransactionsByUserId() {
        // When
        List<Transaction> user123Transactions = mockDataService.getTransactionsByUserId("user123");
        List<Transaction> emptyTransactions = mockDataService.getTransactionsByUserId("nonexistent");
        
        // Then
        assertEquals(3, user123Transactions.size());
        assertTrue(user123Transactions.stream().anyMatch(t -> "Starbucks Coffee".equals(t.getMerchantName())));
        assertTrue(user123Transactions.stream().anyMatch(t -> "Amazon Shopping".equals(t.getMerchantName())));
        
        assertTrue(emptyTransactions.isEmpty());
    }
    
    @Test
    public void testGetCurrentWeatherContext() {
        // When
        WeatherContext weather = mockDataService.getCurrentWeatherContext();
        
        // Then
        assertNotNull(weather);
        assertEquals(WeatherContext.WeatherCondition.SUNNY, weather.getCondition());
        assertEquals(24, weather.getTemperature());
        assertNotNull(weather.getDescription());
        assertNotNull(weather.getTimestamp());
    }
    
    @Test
    public void testGetTransactionsByUserIdAndStatus() {
        // When
        List<Transaction> completedTransactions = mockDataService.getTransactionsByUserIdAndStatus("user123", Transaction.TransactionStatus.COMPLETED);
        List<Transaction> pendingTransactions = mockDataService.getTransactionsByUserIdAndStatus("user123", Transaction.TransactionStatus.PENDING);
        
        // Then
        assertEquals(2, completedTransactions.size());
        assertEquals(1, pendingTransactions.size());
        assertEquals("Uber Ride", pendingTransactions.get(0).getMerchantName());
    }
    
    @Test
    void hasOverduePayment_ShouldReturnTrueForOverdueUser() {
        assertTrue(mockDataService.hasOverduePayment("user456"));
    }
    
    // Story 1.4: Intent-specific data method tests
    
    @Test
    void getPaymentSummary_ShouldReturnValidSummaryForExistingUser() {
        PaymentSummary summary = mockDataService.getPaymentSummary("user123");
        
        assertNotNull(summary);
        assertEquals("user123", summary.getUserId());
        assertEquals(new BigDecimal("1250.00"), summary.getOutstandingBalance());
        assertEquals(new BigDecimal("5000.00"), summary.getAvailableCredit());
        assertNotNull(summary.getDueDate());
        assertNotNull(summary.getPaymentStatus());
    }
    
    @Test
    void getPaymentSummary_ShouldReturnNullForNonExistentUser() {
        PaymentSummary summary = mockDataService.getPaymentSummary("nonexistent");
        
        assertNull(summary);
    }
    
    @Test
    void getTransactionHistory_ShouldReturnAllTransactionsWhenNoDatesProvided() {
        List<Transaction> transactions = mockDataService.getTransactionHistory("user123", null, null);
        
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertEquals(3, transactions.size()); // user123 has 3 transactions
    }
    
    @Test
    void getTransactionHistory_ShouldFilterTransactionsByDateRange() {
        LocalDate fromDate = LocalDate.of(2025, 9, 26);
        LocalDate toDate = LocalDate.of(2025, 9, 28);
        
        List<Transaction> transactions = mockDataService.getTransactionHistory("user123", fromDate, toDate);
        
        assertNotNull(transactions);
        // Should contain transactions within the date range
        transactions.forEach(txn -> {
            LocalDate txnDate = txn.getTransactionDate().toLocalDate();
            assertTrue(txnDate.equals(fromDate) || txnDate.equals(toDate) || 
                      (txnDate.isAfter(fromDate) && txnDate.isBefore(toDate)));
        });
    }
    
    @Test
    void initiateDisputeProcess_ShouldCreateDisputeCaseWithGeneralProcess() {
        DisputeCase disputeCase = mockDataService.initiateDisputeProcess("user123", null);
        
        assertNotNull(disputeCase);
        assertNotNull(disputeCase.getDisputeId());
        assertNotNull(disputeCase.getCaseReference());
        assertEquals("user123", disputeCase.getUserId());
        assertEquals(DisputeCase.DisputeStatus.INITIATED, disputeCase.getStatus());
        assertNotNull(disputeCase.getNextSteps());
        assertFalse(disputeCase.getNextSteps().isEmpty());
        assertTrue(disputeCase.getCaseReference().startsWith("CASE"));
    }
    
    @Test
    void initiateDisputeProcess_ShouldCreateDisputeCaseWithSpecificTransaction() {
        DisputeCase disputeCase = mockDataService.initiateDisputeProcess("user123", "txn001");
        
        assertNotNull(disputeCase);
        assertEquals("user123", disputeCase.getUserId());
        // Should attempt to find the specific transaction (may be null if not found)
        assertNotNull(disputeCase.getNextSteps());
    }
    

    
    @Test
    public void testGetAllTransactions() {
        // When
        List<Transaction> allTransactions = mockDataService.getAllTransactions();
        
        // Then
        assertEquals(17, allTransactions.size()); // 3 + 3 + 2 + 3 + 3 + 3 transactions
        assertTrue(allTransactions.stream().anyMatch(t -> "user123".equals(t.getUserId())));
        assertTrue(allTransactions.stream().anyMatch(t -> "user456".equals(t.getUserId())));
        assertTrue(allTransactions.stream().anyMatch(t -> "user789".equals(t.getUserId())));
        // Story 1.3 scenario users
        assertTrue(allTransactions.stream().anyMatch(t -> "user_overdue".equals(t.getUserId())));
        assertTrue(allTransactions.stream().anyMatch(t -> "user_recent_payment".equals(t.getUserId())));
        assertTrue(allTransactions.stream().anyMatch(t -> "user_duplicate_txn".equals(t.getUserId())));
    }
    
    // Story 1.3 Tests
    
    @Test
    public void testGetUserScenario() {
        // When & Then
        assertEquals("OVERDUE_PAYMENT", mockDataService.getUserScenario("user_overdue"));
        assertEquals("RECENT_PAYMENT", mockDataService.getUserScenario("user_recent_payment"));
        assertEquals("DUPLICATE_TRANSACTION", mockDataService.getUserScenario("user_duplicate_txn"));
        
        // Regular users should not have scenario types
        assertNull(mockDataService.getUserScenario("user123"));
        assertNull(mockDataService.getUserScenario("user456"));
        assertNull(mockDataService.getUserScenario("user789"));
        assertNull(mockDataService.getUserScenario("nonexistent"));
    }
    
    @Test
    public void testScenarioUserData_OverduePayment() {
        // When
        User overdueUser = mockDataService.getUserById("user_overdue");
        
        // Then
        assertNotNull(overdueUser);
        assertEquals("user_overdue", overdueUser.getUserId());
        assertEquals(new BigDecimal("120000.00"), overdueUser.getCurrentBalance());
        assertEquals(User.PaymentStatus.OVERDUE, overdueUser.getPaymentStatus());
        assertNotNull(overdueUser.getDueDate());
    }
    
    @Test
    public void testScenarioUserData_RecentPayment() {
        // When
        User recentPaymentUser = mockDataService.getUserById("user_recent_payment");
        
        // Then
        assertNotNull(recentPaymentUser);
        assertEquals("user_recent_payment", recentPaymentUser.getUserId());
        assertEquals(User.PaymentStatus.CURRENT, recentPaymentUser.getPaymentStatus());
        // Payment received today
        assertEquals(java.time.LocalDate.now(), recentPaymentUser.getLastPaymentDate());
    }
    
    @Test
    public void testScenarioUserData_DuplicateTransaction() {
        // When
        User duplicateUser = mockDataService.getUserById("user_duplicate_txn");
        List<Transaction> transactions = mockDataService.getTransactionsByUserId("user_duplicate_txn");
        
        // Then
        assertNotNull(duplicateUser);
        assertEquals("user_duplicate_txn", duplicateUser.getUserId());
        assertEquals(User.PaymentStatus.CURRENT, duplicateUser.getPaymentStatus());
        
        // Should have at least 2 transactions with same amount for duplicate detection
        assertEquals(3, transactions.size());
        assertTrue(transactions.stream().anyMatch(t -> t.getAmount().equals(new BigDecimal("2890.00"))));
    }
}
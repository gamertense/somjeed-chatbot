package com.chatbot.demo.service;

import com.chatbot.demo.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock data service providing sample data for users, transactions, and weather context.
 * This service provides mock data as specified in the architecture requirements.
 */
@Service
public class MockDataService {
    
    private final Map<String, User> users;
    private final Map<String, List<Transaction>> userTransactions;
    private final WeatherContext currentWeatherContext;
    
    public MockDataService() {
        this.users = new HashMap<>();
        this.userTransactions = new HashMap<>();
        this.currentWeatherContext = createMockWeatherContext();
        
        initializeMockUsers();
        initializeMockTransactions();
    }
    
    /**
     * Initialize mock user data with different payment statuses
     */
    private void initializeMockUsers() {
        // User with current payment status
        users.put("user123", new User(
            "user123",
            "****-****-****-1234",
            new BigDecimal("1250.00"),
            new BigDecimal("5000.00"),
            LocalDate.of(2025, 10, 15),
            LocalDate.of(2025, 9, 15),
            User.PaymentStatus.CURRENT
        ));
        
        // User with overdue payment
        users.put("user456", new User(
            "user456",
            "****-****-****-5678",
            new BigDecimal("2850.75"),
            new BigDecimal("8000.00"),
            LocalDate.of(2025, 9, 20),
            LocalDate.of(2025, 8, 15),
            User.PaymentStatus.OVERDUE
        ));
        
        // User with upcoming payment
        users.put("user789", new User(
            "user789",
            "****-****-****-9012",
            new BigDecimal("456.30"),
            new BigDecimal("3000.00"),
            LocalDate.of(2025, 10, 5),
            LocalDate.of(2025, 9, 25),
            User.PaymentStatus.UPCOMING
        ));
    }
    
    /**
     * Initialize mock transaction data with different categories and statuses
     */
    private void initializeMockTransactions() {
        // Transactions for user123
        List<Transaction> user123Transactions = Arrays.asList(
            new Transaction("txn001", "user123", new BigDecimal("85.50"), 
                "Starbucks Coffee", LocalDateTime.of(2025, 9, 27, 8, 30), 
                Transaction.TransactionCategory.DINING, Transaction.TransactionStatus.COMPLETED),
            new Transaction("txn002", "user123", new BigDecimal("1200.00"), 
                "Amazon Shopping", LocalDateTime.of(2025, 9, 25, 14, 20), 
                Transaction.TransactionCategory.SHOPPING, Transaction.TransactionStatus.COMPLETED),
            new Transaction("txn003", "user123", new BigDecimal("45.00"), 
                "Uber Ride", LocalDateTime.of(2025, 9, 26, 18, 15), 
                Transaction.TransactionCategory.TRAVEL, Transaction.TransactionStatus.PENDING)
        );
        userTransactions.put("user123", user123Transactions);
        
        // Transactions for user456
        List<Transaction> user456Transactions = Arrays.asList(
            new Transaction("txn004", "user456", new BigDecimal("2500.00"), 
                "Hotel Booking", LocalDateTime.of(2025, 9, 20, 10, 0), 
                Transaction.TransactionCategory.TRAVEL, Transaction.TransactionStatus.DISPUTED),
            new Transaction("txn005", "user456", new BigDecimal("125.80"), 
                "Electric Bill", LocalDateTime.of(2025, 9, 22, 16, 45), 
                Transaction.TransactionCategory.UTILITIES, Transaction.TransactionStatus.COMPLETED),
            new Transaction("txn006", "user456", new BigDecimal("67.25"), 
                "Restaurant Dinner", LocalDateTime.of(2025, 9, 24, 19, 30), 
                Transaction.TransactionCategory.DINING, Transaction.TransactionStatus.COMPLETED)
        );
        userTransactions.put("user456", user456Transactions);
        
        // Transactions for user789
        List<Transaction> user789Transactions = Arrays.asList(
            new Transaction("txn007", "user789", new BigDecimal("156.00"), 
                "Grocery Store", LocalDateTime.of(2025, 9, 28, 11, 15), 
                Transaction.TransactionCategory.OTHER, Transaction.TransactionStatus.COMPLETED),
            new Transaction("txn008", "user789", new BigDecimal("89.99"), 
                "Online Subscription", LocalDateTime.of(2025, 9, 26, 9, 0), 
                Transaction.TransactionCategory.OTHER, Transaction.TransactionStatus.COMPLETED)
        );
        userTransactions.put("user789", user789Transactions);
    }
    
    /**
     * Create mock weather context data
     */
    private WeatherContext createMockWeatherContext() {
        return new WeatherContext(
            WeatherContext.WeatherCondition.SUNNY,
            24,
            "Pleasant sunny day with light breeze",
            LocalDateTime.now()
        );
    }
    
    /**
     * Retrieve user by ID
     * @param userId User identifier
     * @return User object or null if not found
     */
    public User getUserById(String userId) {
        return users.get(userId);
    }
    
    /**
     * Retrieve all mock users
     * @return Collection of all users
     */
    public Collection<User> getAllUsers() {
        return users.values();
    }
    
    /**
     * Retrieve transactions for a specific user
     * @param userId User identifier
     * @return List of transactions for the user
     */
    public List<Transaction> getTransactionsByUserId(String userId) {
        return userTransactions.getOrDefault(userId, new ArrayList<>());
    }
    
    /**
     * Retrieve all transactions across all users
     * @return List of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return userTransactions.values().stream()
            .flatMap(List::stream)
            .toList();
    }
    
    /**
     * Get current weather context
     * @return Current weather context data
     */
    public WeatherContext getCurrentWeatherContext() {
        return currentWeatherContext;
    }
    
    /**
     * Get transactions by status for a user
     * @param userId User identifier
     * @param status Transaction status to filter by
     * @return List of transactions with the specified status
     */
    public List<Transaction> getTransactionsByUserIdAndStatus(String userId, Transaction.TransactionStatus status) {
        return getTransactionsByUserId(userId).stream()
            .filter(transaction -> transaction.getStatus() == status)
            .toList();
    }
    
    /**
     * Check if user has any overdue payments
     * @param userId User identifier
     * @return true if user has overdue payments
     */
    public boolean hasOverduePayment(String userId) {
        User user = getUserById(userId);
        return user != null && user.getPaymentStatus() == User.PaymentStatus.OVERDUE;
    }
}
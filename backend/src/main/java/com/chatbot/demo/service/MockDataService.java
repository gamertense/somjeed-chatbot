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

        // Scenario-specific users for Story 1.3 proactive intent prediction
        
        // Scenario 1: User with overdue payment (from assignment example)
        users.put("user_overdue", new User(
            "user_overdue",
            "****-****-****-4321",
            new BigDecimal("120000.00"), // 120,000 THB as per assignment
            new BigDecimal("150000.00"),
            LocalDate.of(2025, 9, 1), // Due date was 1 September 2025
            LocalDate.of(2025, 8, 1), // Last payment in August
            User.PaymentStatus.OVERDUE
        ));
        
        // Scenario 2: User with recent payment (received today)
        users.put("user_recent_payment", new User(
            "user_recent_payment",
            "****-****-****-8765",
            new BigDecimal("2500.00"), // Reduced balance after recent payment
            new BigDecimal("10000.00"),
            LocalDate.of(2025, 10, 28), // Next due date
            LocalDate.now(), // Payment received today
            User.PaymentStatus.CURRENT
        ));
        
        // Scenario 3: User with duplicate transaction pattern
        users.put("user_duplicate_txn", new User(
            "user_duplicate_txn",
            "****-****-****-3456",
            new BigDecimal("5678.90"),
            new BigDecimal("15000.00"),
            LocalDate.of(2025, 10, 15),
            LocalDate.of(2025, 9, 15),
            User.PaymentStatus.CURRENT
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

        // Scenario-specific transactions for Story 1.3

        // Transactions for user_overdue (overdue payment scenario)
        List<Transaction> userOverdueTransactions = Arrays.asList(
            new Transaction("txn_overdue_001", "user_overdue", new BigDecimal("15000.00"), 
                "Shopping Mall", LocalDateTime.of(2025, 8, 25, 14, 30), 
                Transaction.TransactionCategory.SHOPPING, Transaction.TransactionStatus.COMPLETED),
            new Transaction("txn_overdue_002", "user_overdue", new BigDecimal("8500.00"), 
                "Restaurant", LocalDateTime.of(2025, 8, 28, 19, 45), 
                Transaction.TransactionCategory.DINING, Transaction.TransactionStatus.COMPLETED),
            new Transaction("txn_overdue_003", "user_overdue", new BigDecimal("25000.00"), 
                "Hotel Booking", LocalDateTime.of(2025, 9, 10, 10, 0), 
                Transaction.TransactionCategory.TRAVEL, Transaction.TransactionStatus.COMPLETED)
        );
        userTransactions.put("user_overdue", userOverdueTransactions);
        
        // Transactions for user_recent_payment (recent payment scenario)
        List<Transaction> userRecentPaymentTransactions = Arrays.asList(
            new Transaction("txn_recent_001", "user_recent_payment", new BigDecimal("1250.00"), 
                "Department Store", LocalDateTime.of(2025, 9, 20, 16, 30), 
                Transaction.TransactionCategory.SHOPPING, Transaction.TransactionStatus.COMPLETED),
            new Transaction("txn_recent_002", "user_recent_payment", new BigDecimal("650.00"), 
                "Gas Station", LocalDateTime.of(2025, 9, 25, 8, 15), 
                Transaction.TransactionCategory.UTILITIES, Transaction.TransactionStatus.COMPLETED),
            // Payment transaction received today
            new Transaction("txn_recent_003", "user_recent_payment", new BigDecimal("-5000.00"), 
                "Payment Received", LocalDateTime.now().minusHours(2), 
                Transaction.TransactionCategory.OTHER, Transaction.TransactionStatus.COMPLETED)
        );
        userTransactions.put("user_recent_payment", userRecentPaymentTransactions);
        
        // Transactions for user_duplicate_txn (duplicate transaction pattern)
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> userDuplicateTransactions = Arrays.asList(
            // Two similar transactions within 48 hours - this triggers duplicate detection
            new Transaction("txn_dup_001", "user_duplicate_txn", new BigDecimal("2890.00"), 
                "Online Shopping Store A", now.minusHours(36), 
                Transaction.TransactionCategory.SHOPPING, Transaction.TransactionStatus.COMPLETED),
            new Transaction("txn_dup_002", "user_duplicate_txn", new BigDecimal("2890.00"), 
                "Online Shopping Store B", now.minusHours(12), 
                Transaction.TransactionCategory.SHOPPING, Transaction.TransactionStatus.COMPLETED),
            // Other regular transactions
            new Transaction("txn_dup_003", "user_duplicate_txn", new BigDecimal("450.00"), 
                "Coffee Shop", now.minusHours(72), 
                Transaction.TransactionCategory.DINING, Transaction.TransactionStatus.COMPLETED)
        );
        userTransactions.put("user_duplicate_txn", userDuplicateTransactions);
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
     * Get user scenario type for proactive intent prediction
     * @param userId User identifier
     * @return Scenario type string or null if not a scenario user
     */
    public String getUserScenario(String userId) {
        return switch (userId) {
            case "user_overdue" -> "OVERDUE_PAYMENT";
            case "user_recent_payment" -> "RECENT_PAYMENT"; 
            case "user_duplicate_txn" -> "DUPLICATE_TRANSACTION";
            default -> null; // Regular users don't have scenario types
        };
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
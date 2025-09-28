package com.chatbot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dispute case data for TRANSACTION_DISPUTE intent responses.
 * Contains dispute process information and next steps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeCase {
    
    /**
     * Dispute case identifier
     */
    private String disputeId;
    
    /**
     * Case reference number for customer communication
     */
    private String caseReference;
    
    /**
     * User identifier who initiated the dispute
     */
    private String userId;
    
    /**
     * Transaction being disputed (null for general dispute)
     */
    private Transaction disputedTransaction;
    
    /**
     * Dispute initiation timestamp
     */
    private LocalDateTime initiatedAt;
    
    /**
     * Current dispute status
     */
    private DisputeStatus status;
    
    /**
     * Next steps for the customer
     */
    private List<String> nextSteps;
    
    /**
     * Dispute status enum
     */
    public enum DisputeStatus {
        INITIATED, UNDER_REVIEW, RESOLVED, CLOSED
    }
}
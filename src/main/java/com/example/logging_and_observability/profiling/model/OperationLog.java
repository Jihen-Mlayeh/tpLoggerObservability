package com.example.logging_and_observability.profiling.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a single operation log entry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

    private String operationName;           // e.g., "getAllProducts", "addProduct"
    private UserOperationType operationType; // READ, WRITE, SEARCH_EXPENSIVE

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String userName;                // User who performed the operation
    private String userEmail;

    private Long productId;                 // Product ID if applicable
    private String productName;             // Product name if applicable
    private Double productPrice;            // Product price if applicable

    private String additionalInfo;          // Any additional context

    public OperationLog(String operationName, UserOperationType operationType,
                        LocalDateTime timestamp, String userName, String userEmail) {
        this.operationName = operationName;
        this.operationType = operationType;
        this.timestamp = timestamp;
        this.userName = userName;
        this.userEmail = userEmail;
    }
}

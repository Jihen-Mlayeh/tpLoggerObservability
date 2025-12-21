package com.example.logging_and_observability.logging.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Log Processing Structure (LPS) - Structured log entry
 * Uses Builder Pattern for flexible construction
 */
@Data
public class LogEntry {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    private String level;           // INFO, WARN, ERROR, DEBUG
    private String logger;          // Class name that generated the log
    private String thread;          // Thread name
    private String event;           // Event type: USER_LOGIN, PRODUCT_VIEW, etc.
    private String userName;        // User who performed the action
    private String userEmail;       // User email
    private String action;          // Specific action: getAllProducts, addProduct, etc.
    private String resourceType;    // PRODUCT, USER, PROFILE
    private String resourceId;        // ID of the resource
    private String resourceName;    // Name of the resource
    private Double resourcePrice;   // Price (for products)
    private String operationType;   // READ, WRITE, SEARCH_EXPENSIVE
    private String result;          // SUCCESS, FAILURE, ERROR
    private String errorMessage;    // Error details if any
    private Long duration;          // Duration in milliseconds
    private String additionalInfo;  // Any extra information

    // Private constructor - use Builder instead
    private LogEntry() {}

    /**
     * Builder Pattern for constructing LogEntry
     */
    public static class Builder {
        private final LogEntry logEntry;

        public Builder() {
            this.logEntry = new LogEntry();
            this.logEntry.timestamp = LocalDateTime.now();
        }

        public Builder timestamp(LocalDateTime timestamp) {
            logEntry.timestamp = timestamp;
            return this;
        }

        public Builder level(String level) {
            logEntry.level = level;
            return this;
        }

        public Builder logger(String logger) {
            logEntry.logger = logger;
            return this;
        }

        public Builder thread(String thread) {
            logEntry.thread = thread;
            return this;
        }

        public Builder event(String event) {
            logEntry.event = event;
            return this;
        }

        public Builder userName(String userName) {
            logEntry.userName = userName;
            return this;
        }

        public Builder userEmail(String userEmail) {
            logEntry.userEmail = userEmail;
            return this;
        }

        public Builder action(String action) {
            logEntry.action = action;
            return this;
        }

        public Builder resourceType(String resourceType) {
            logEntry.resourceType = resourceType;
            return this;
        }

        public Builder resourceId(String resourceId) {
            logEntry.resourceId = resourceId;
            return this;
        }

        public Builder resourceName(String resourceName) {
            logEntry.resourceName = resourceName;
            return this;
        }

        public Builder resourcePrice(Double resourcePrice) {
            logEntry.resourcePrice = resourcePrice;
            return this;
        }

        public Builder operationType(String operationType) {
            logEntry.operationType = operationType;
            return this;
        }

        public Builder result(String result) {
            logEntry.result = result;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            logEntry.errorMessage = errorMessage;
            return this;
        }

        public Builder duration(Long duration) {
            logEntry.duration = duration;
            return this;
        }

        public Builder additionalInfo(String additionalInfo) {
            logEntry.additionalInfo = additionalInfo;
            return this;
        }

        public LogEntry build() {
            // Validation
            if (logEntry.timestamp == null) {
                logEntry.timestamp = LocalDateTime.now();
            }
            if (logEntry.level == null) {
                logEntry.level = "INFO";
            }

            return logEntry;
        }
    }

    /**
     * Create a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Format as structured log string
     */
    public String toStructuredLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timestamp: ").append(timestamp);
        sb.append(" | Level: ").append(level);
        sb.append(" | Event: ").append(event);

        if (userName != null) {
            sb.append(" | User: ").append(userName);
        }
        if (userEmail != null) {
            sb.append(" (").append(userEmail).append(")");
        }
        if (action != null) {
            sb.append(" | Action: ").append(action);
        }
        if (operationType != null) {
            sb.append(" | Operation: ").append(operationType);
        }
        if (resourceType != null) {
            sb.append(" | Resource: ").append(resourceType);
            if (resourceId != null) {
                sb.append("#").append(resourceId);
            }
        }
        if (result != null) {
            sb.append(" | Result: ").append(result);
        }
        if (duration != null) {
            sb.append(" | Duration: ").append(duration).append("ms");
        }

        return sb.toString();
    }
}
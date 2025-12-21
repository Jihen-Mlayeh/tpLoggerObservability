package com.example.logging_and_observability.logging.service;
import com.example.logging_and_observability.logging.model.LogEntry;
import com.example.logging_and_observability.model.Product;
import com.example.logging_and_observability.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
/**
 * Service for structured logging using LPS (Log Processing Structure)
 * Implements Builder Pattern for log construction
 */
@Slf4j
@Service
public class StructuredLogger {
    private static final String STRUCTURED_LOGS_DIR = "structured-logs";

    private static final String JSON_LOGS_FILE = "structured-logs/application-logs.json";

    private static final String TEXT_LOGS_FILE = "structured-logs/application-logs.txt";

    private final ObjectMapper objectMapper;

    private final List<LogEntry> logBuffer = new ArrayList<>();

    public StructuredLogger() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Create directory
        new File(STRUCTURED_LOGS_DIR).mkdirs();
        log.info("StructuredLogger initialized. Logs directory: {}", STRUCTURED_LOGS_DIR);
    }

    /**
     * Log a user authentication event
     */
    public void logUserAuthentication(User user, boolean success) {
        LogEntry entry = LogEntry.builder().timestamp(LocalDateTime.now()).level("INFO").logger("UserService").event("USER_AUTHENTICATION").userName(user.getName()).userEmail(user.getEmail()).action("login").resourceType("USER").resourceId(user.getId()).result(success ? "SUCCESS" : "FAILURE").build();
        writeLog(entry);
    }

    /**
     * Log a user registration event
     */
    public void logUserRegistration(User user) {
        LogEntry entry = LogEntry.builder().timestamp(LocalDateTime.now()).level("INFO").logger("UserService").event("USER_REGISTRATION").userName(user.getName()).userEmail(user.getEmail()).action("register").resourceType("USER").resourceId(user.getId()).result("SUCCESS").build();
        writeLog(entry);
    }

    /**
     * Log a product operation
     */
    public void logProductOperation(User user, String action, Product product, String operationType, long duration) {
        LogEntry.Builder builder = LogEntry.builder().timestamp(LocalDateTime.now()).level("INFO").logger("ProductService").event("PRODUCT_OPERATION").action(action).operationType(operationType).resourceType("PRODUCT").duration(duration).result("SUCCESS");
        if (user != null) {
            builder.userName(user.getName()).userEmail(user.getEmail());
        }
        if (product != null) {
            builder.resourceId(product.getId()).resourceName(product.getName()).resourcePrice(product.getPrice());
        }
        writeLog(builder.build());
    }

    /**
     * Log an expensive product view
     */
    public void logExpensiveProductView(User user, Product product) {
        LogEntry entry = LogEntry.builder().timestamp(LocalDateTime.now()).level("INFO").logger("ProductService").event("EXPENSIVE_PRODUCT_VIEW").userName(user.getName()).userEmail(user.getEmail()).action("viewExpensiveProduct").operationType("SEARCH_EXPENSIVE").resourceType("PRODUCT").resourceId(product.getId()).resourceName(product.getName()).resourcePrice(product.getPrice()).result("SUCCESS").additionalInfo("Price >= €100").build();
        writeLog(entry);
    }

    /**
     * Log an error
     */
    public void logError(String logger, String event, String action, Exception e) {
        LogEntry entry = LogEntry.builder().timestamp(LocalDateTime.now()).level("ERROR").logger(logger).event(event).action(action).result("ERROR").errorMessage(e.getMessage()).build();
        writeLog(entry);
    }

    /**
     * Log profile generation
     */
    public void logProfileGeneration(User user, String profileType) {
        LogEntry entry = LogEntry.builder().timestamp(LocalDateTime.now()).level("INFO").logger("UserProfileService").event("PROFILE_GENERATION").userName(user.getName()).userEmail(user.getEmail()).action("generateProfile").resourceType("PROFILE").result("SUCCESS").additionalInfo("Profile Type: " + profileType).build();
        writeLog(entry);
    }

    /**
     * Write log entry to both JSON and text files
     */
    private void writeLog(LogEntry entry) {
        // Add to buffer
        logBuffer.add(entry);
        // Write to text file (append mode)
        writeToTextFile(entry);
        // Write to JSON file (overwrite with all entries)
        if ((logBuffer.size() % 10) == 0) {
            // Batch write every 10 entries
            writeToJsonFile();
        }
        // Also log to standard logger
        log.info(entry.toStructuredLog());
    }

    /**
     * Write single entry to text file
     */
    private void writeToTextFile(LogEntry entry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEXT_LOGS_FILE, true))) {
            writer.write(entry.toStructuredLog());
            writer.newLine();
        } catch (IOException e) {
            log.error("Failed to write to text log file", e);
        }
    }

    /**
     * Write all entries to JSON file
     */
    private void writeToJsonFile() {
        try {
            objectMapper.writeValue(new File(JSON_LOGS_FILE), logBuffer);
        } catch (IOException e) {
            log.error("Failed to write to JSON log file", e);
        }
    }

    /**
     * Flush all buffered logs
     */
    public void flush() {
        writeToJsonFile();
        log.info("Flushed {} log entries to {}", logBuffer.size(), JSON_LOGS_FILE);
    }

    /**
     * Get all log entries
     */
    public List<LogEntry> getAllLogs() {
        return new ArrayList<>(logBuffer);
    }

    /**
     * Get logs by user email
     */
    public List<LogEntry> getLogsByUser(String userEmail) {
        return logBuffer.stream().filter(log -> userEmail.equals(log.getUserEmail())).toList();
    }

    /**
     * Get logs by event type
     */
    public List<LogEntry> getLogsByEvent(String event) {
        return logBuffer.stream().filter(log -> event.equals(log.getEvent())).toList();
    }

    /**
     * Get logs by operation type
     */
    public List<LogEntry> getLogsByOperationType(String operationType) {
        return logBuffer.stream().filter(log -> operationType.equals(log.getOperationType())).toList();
    }

    /**
     * Clear all logs
     */
    public void clearLogs() {
        logBuffer.clear();
        log.info("Cleared all log entries from buffer");
    }
}
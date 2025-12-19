package com.example.logging_and_observability.logging.parser;

import com.example.logging_and_observability.logging.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IMPROVED Parser for extracting structured information from Logback logs
 * Converts raw log files into LogEntry objects
 */
@Slf4j
@Component
public class LogParser {

    // Logback default pattern: %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+" +  // timestamp
                    "\\[([^\\]]+)\\]\\s+" +                                        // thread
                    "([A-Z]+)\\s+" +                                               // level
                    "([^\\s]+)\\s+-\\s+" +                                         // logger
                    "(.+)"                                                         // message
    );

    private static final DateTimeFormatter LOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Parse a log file and extract LogEntry objects
     */
    public List<LogEntry> parseLogFile(String filePath) {
        List<LogEntry> entries = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            log.warn("Log file not found: {}", filePath);
            return entries;
        }

        log.info("Parsing log file: {}", filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    LogEntry entry = parseLine(line);
                    if (entry != null) {
                        entries.add(entry);
                    }
                } catch (Exception e) {
                    log.debug("Failed to parse line {}: {}", lineNumber, e.getMessage());
                }
            }

            log.info("Parsed {} log entries from {}", entries.size(), filePath);

        } catch (IOException e) {
            log.error("Error reading log file: {}", filePath, e);
        }

        return entries;
    }

    /**
     * Parse a single log line
     */
    private LogEntry parseLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);

        if (!matcher.matches()) {
            return null;
        }

        String timestampStr = matcher.group(1);
        String thread = matcher.group(2);
        String level = matcher.group(3);
        String logger = matcher.group(4);
        String message = matcher.group(5);

        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, LOG_DATE_FORMATTER);

        LogEntry.Builder builder = LogEntry.builder()
                .timestamp(timestamp)
                .thread(thread)
                .level(level)
                .logger(logger)
                .additionalInfo(message);

        // Parse message for structured information
        parseMessage(message, builder);

        return builder.build();
    }

    /**
     * Parse message content and extract structured information
     * IMPROVED to match the actual ProductService log format
     */
    private void parseMessage(String message, LogEntry.Builder builder) {
        // NEW: Parse structured format from ProductService
        // Example: "Operation: getAllProducts | User: Alice | Email: alice@email.com | Action: READ"
        if (message.contains("Operation:")) {
            parseStructuredProductLog(message, builder);
            return;
        }

        // Fallback: Check for user operations
        if (message.contains("User ") || message.contains("user")) {
            extractUserInfo(message, builder);
        }

        // Check for product operations
        if (message.contains("product") || message.contains("Product")) {
            extractProductInfo(message, builder);
            builder.event("PRODUCT_OPERATION");
        }

        // Check for specific actions
        if (message.contains("getAllProducts")) {
            builder.action("getAllProducts").operationType("READ");
        } else if (message.contains("getProductById")) {
            builder.action("getProductById").operationType("READ");
        } else if (message.contains("addProduct")) {
            builder.action("addProduct").operationType("WRITE");
        } else if (message.contains("updateProduct")) {
            builder.action("updateProduct").operationType("WRITE");
        } else if (message.contains("deleteProduct")) {
            builder.action("deleteProduct").operationType("WRITE");
        }

        // Check for expensive product operations
        if (message.contains("Expensive product view") || message.contains("SEARCH_EXPENSIVE")) {
            builder.action("viewExpensiveProduct").operationType("SEARCH_EXPENSIVE");
        }

        // Check for authentication
        if (message.contains("authenticated") || message.contains("login")) {
            builder.event("USER_AUTHENTICATION");
            builder.result(message.contains("Success") || message.contains("successfully") ? "SUCCESS" : "FAILURE");
        }

        // Check for registration
        if (message.contains("registered") || message.contains("registration")) {
            builder.event("USER_REGISTRATION");
            builder.result("SUCCESS");
        }

        // Check for errors
        if (message.contains("Error") || message.contains("failed") || message.contains("Exception")) {
            builder.result("ERROR");
            builder.errorMessage(message);
        } else if (builder.build().getResult() == null) {
            builder.result("SUCCESS");
        }

        // Extract duration if present
        if (message.contains("ms") || message.contains("milliseconds")) {
            extractDuration(message, builder);
        }
    }

    /**
     * NEW: Parse structured ProductService logs
     * Format: "Operation: getAllProducts | User: Alice | Email: alice@email.com | Action: READ"
     */
    private void parseStructuredProductLog(String message, LogEntry.Builder builder) {
        builder.event("PRODUCT_OPERATION");

        // Extract Operation
        Pattern opPattern = Pattern.compile("Operation:\\s*(\\w+)");
        Matcher opMatcher = opPattern.matcher(message);
        if (opMatcher.find()) {
            builder.action(opMatcher.group(1));
        }

        // Extract User name
        Pattern userPattern = Pattern.compile("User:\\s*([^|]+?)\\s*(?:\\||$)");
        Matcher userMatcher = userPattern.matcher(message);
        if (userMatcher.find()) {
            String userName = userMatcher.group(1).trim();
            if (!userName.equals("Unknown")) {
                builder.userName(userName);
            }
        }

        // Extract Email
        Pattern emailPattern = Pattern.compile("Email:\\s*([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher emailMatcher = emailPattern.matcher(message);
        if (emailMatcher.find()) {
            builder.userEmail(emailMatcher.group(1));
        }

        // Extract ProductID
        Pattern idPattern = Pattern.compile("ProductID:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(message);
        if (idMatcher.find()) {
            builder.resourceId(Long.parseLong(idMatcher.group(1)));
            builder.resourceType("PRODUCT");
        }

        // Extract ProductName
        Pattern namePattern = Pattern.compile("ProductName:\\s*([^|]+?)\\s*(?:\\||$)");
        Matcher nameMatcher = namePattern.matcher(message);
        if (nameMatcher.find()) {
            builder.resourceName(nameMatcher.group(1).trim());
            builder.resourceType("PRODUCT");
        }

        // Extract Action (READ/WRITE)
        Pattern actionPattern = Pattern.compile("Action:\\s*(READ|WRITE)");
        Matcher actionMatcher = actionPattern.matcher(message);
        if (actionMatcher.find()) {
            builder.operationType(actionMatcher.group(1));
        }

        // Extract Price
        Pattern pricePattern = Pattern.compile("Price:\\s*€([\\d.]+)");
        Matcher priceMatcher = pricePattern.matcher(message);
        if (priceMatcher.find()) {
            builder.resourcePrice(Double.parseDouble(priceMatcher.group(1)));
        }

        // Check for specific patterns
        if (message.contains("Product found") || message.contains("Retrieved") || message.contains("Product added") || message.contains("Product updated") || message.contains("Product deleted")) {
            // Extract ID
            Pattern idPattern2 = Pattern.compile("ID:\\s*(\\d+)");
            Matcher idMatcher2 = idPattern2.matcher(message);
            if (idMatcher2.find()) {
                builder.resourceId(Long.parseLong(idMatcher2.group(1)));
                builder.resourceType("PRODUCT");
            }

            // Extract Name
            Pattern namePattern2 = Pattern.compile("Name:\\s*([^|]+?)\\s*(?:\\||$)");
            Matcher nameMatcher2 = namePattern2.matcher(message);
            if (nameMatcher2.find()) {
                builder.resourceName(nameMatcher2.group(1).trim());
            }

            // Extract Price
            Pattern pricePattern2 = Pattern.compile("Price:\\s*€([\\d.]+)");
            Matcher priceMatcher2 = pricePattern2.matcher(message);
            if (priceMatcher2.find()) {
                builder.resourcePrice(Double.parseDouble(priceMatcher2.group(1)));
            }
        }

        // Check for expensive product view
        if (message.contains("Expensive product view")) {
            builder.action("viewExpensiveProduct");
            builder.operationType("SEARCH_EXPENSIVE");
        }

        // Set result
        if (message.contains("SUCCESS") || message.contains("Status: SUCCESS")) {
            builder.result("SUCCESS");
        } else if (message.contains("ERROR") || message.contains("Status: ERROR")) {
            builder.result("ERROR");
        }
    }

    /**
     * Extract user information from message
     */
    private void extractUserInfo(String message, LogEntry.Builder builder) {
        // Try to extract email
        Pattern emailPattern = Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher emailMatcher = emailPattern.matcher(message);
        if (emailMatcher.find()) {
            builder.userEmail(emailMatcher.group(1));
        }

        // Try to extract user name
        Pattern namePattern = Pattern.compile("User[:\\s]+([A-Za-z\\s]+)(?:\\s|$)");
        Matcher nameMatcher = namePattern.matcher(message);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1).trim();
            if (!name.equals("Unknown")) {
                builder.userName(name);
            }
        }
    }

    /**
     * Extract product information from message
     */
    private void extractProductInfo(String message, LogEntry.Builder builder) {
        builder.resourceType("PRODUCT");

        // Try to extract product ID
        Pattern idPattern = Pattern.compile("(?:ID|id)[:\\s]+(\\d+)");
        Matcher idMatcher = idPattern.matcher(message);
        if (idMatcher.find()) {
            builder.resourceId(Long.parseLong(idMatcher.group(1)));
        }

        // Try to extract price
        Pattern pricePattern = Pattern.compile("(?:€|EUR|Price:)[:\\s]*([\\d.]+)");
        Matcher priceMatcher = pricePattern.matcher(message);
        if (priceMatcher.find()) {
            builder.resourcePrice(Double.parseDouble(priceMatcher.group(1)));
        }

        // Try to extract product name
        Pattern namePattern = Pattern.compile("Name[:\\s]+([^|]+?)\\s*(?:\\||$)");
        Matcher nameMatcher = namePattern.matcher(message);
        if (nameMatcher.find()) {
            builder.resourceName(nameMatcher.group(1).trim());
        }
    }

    /**
     * Extract duration from message
     */
    private void extractDuration(String message, LogEntry.Builder builder) {
        Pattern durationPattern = Pattern.compile("(\\d+)\\s*(?:ms|milliseconds)");
        Matcher durationMatcher = durationPattern.matcher(message);
        if (durationMatcher.find()) {
            builder.duration(Long.parseLong(durationMatcher.group(1)));
        }
    }

    /**
     * Parse logs from default log directory
     */
    public List<LogEntry> parseDefaultLogs() {
        List<LogEntry> allEntries = new ArrayList<>();

        // Parse main log file
        allEntries.addAll(parseLogFile("logs/product-management.log"));

        // Parse structured logs if available
        allEntries.addAll(parseLogFile("structured-logs/application-logs.txt"));

        return allEntries;
    }
}
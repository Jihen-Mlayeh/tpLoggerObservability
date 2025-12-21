package com.example.logging_and_observability.logging.extractor;

import com.example.logging_and_observability.logging.model.LogEntry;
import com.example.logging_and_observability.logging.parser.LogParser;
import com.example.logging_and_observability.profiling.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts user profiles from parsed log entries
 * Implements Question 5 requirements
 */
@Slf4j
@Component
public class ProfileExtractor {

    private final LogParser logParser;
    private final ObjectMapper objectMapper;

    private static final String EXTRACTED_PROFILES_DIR = "extracted-profiles";

    public ProfileExtractor(LogParser logParser) {
        this.logParser = logParser;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create directory
        new File(EXTRACTED_PROFILES_DIR).mkdirs();

        log.info("ProfileExtractor initialized. Output directory: {}", EXTRACTED_PROFILES_DIR);
    }

    /**
     * Extract all user profiles from logs
     */
    public Map<String, UserProfile> extractProfilesFromLogs() {
        log.info("Extracting user profiles from logs...");

        // Parse all logs
        List<LogEntry> logs = logParser.parseDefaultLogs();
        log.info("Found {} log entries to process", logs.size());

        // Group logs by user email
        Map<String, List<LogEntry>> logsByUser = logs.stream()
                .filter(log -> log.getUserEmail() != null)
                .collect(Collectors.groupingBy(LogEntry::getUserEmail));

        log.info("Found logs for {} users", logsByUser.size());

        // Build profile for each user
        Map<String, UserProfile> profiles = new HashMap<>();

        for (Map.Entry<String, List<LogEntry>> entry : logsByUser.entrySet()) {
            String userEmail = entry.getKey();
            List<LogEntry> userLogs = entry.getValue();

            UserProfile profile = buildProfileFromLogs(userEmail, userLogs);
            profiles.put(userEmail, profile);

            log.info("Built {} profile for user: {}", profile.getProfileType(), userEmail);
        }

        return profiles;
    }

    /**
     * Build a user profile from their log entries
     */
    private UserProfile buildProfileFromLogs(String userEmail, List<LogEntry> logs) {
        // Extract user info
        String userName = logs.stream()
                .map(LogEntry::getUserName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Unknown");

        // Count operations by type
        long readOps = logs.stream()
                .filter(log -> "READ".equals(log.getOperationType()))
                .count();

        long writeOps = logs.stream()
                .filter(log -> "WRITE".equals(log.getOperationType()))
                .count();

        long expensiveOps = logs.stream()
                .filter(log -> "SEARCH_EXPENSIVE".equals(log.getOperationType()))
                .count();

        long totalOps = readOps + writeOps + expensiveOps;

        // Determine profile type
        double expensivePercentage = totalOps > 0 ? (expensiveOps * 100.0 / totalOps) : 0;
        double writePercentage = totalOps > 0 ? (writeOps * 100.0 / totalOps) : 0;
        double readPercentage = totalOps > 0 ? (readOps * 100.0 / totalOps) : 0;

        UserProfile profile;

        if (expensivePercentage >= 50.0) {
            profile = buildExpensiveSeekerProfile(userName, userEmail, logs);
        } else if (writePercentage >= 60.0) {
            profile = buildWriteHeavyProfile(userName, userEmail, logs);
        } else {
            profile = buildReadHeavyProfile(userName, userEmail, logs);
        }

        // Set common fields
        profile.setTotalOperations((int) totalOps);
        profile.setProfileCreatedAt(logs.stream()
                .map(LogEntry::getTimestamp)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()));
        profile.setLastActivityAt(logs.stream()
                .map(LogEntry::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()));

        // Convert LogEntry to OperationLog
        List<OperationLog> operationHistory = logs.stream()
                .map(this::convertToOperationLog)
                .collect(Collectors.toList());
        profile.setOperationHistory(operationHistory);

        return profile;
    }

    /**
     * Build ReadHeavyProfile from logs
     */
    private ReadHeavyProfile buildReadHeavyProfile(String userName, String userEmail, List<LogEntry> logs) {
        ReadHeavyProfile profile = new ReadHeavyProfile();
        profile.setUserName(userName);
        profile.setUserEmail(userEmail);
        profile.setUserAge(0); // Age not available from logs

        long readOps = logs.stream()
                .filter(log -> "READ".equals(log.getOperationType()))
                .count();

        long writeOps = logs.stream()
                .filter(log -> "WRITE".equals(log.getOperationType()))
                .count();

        profile.setTotalReadOperations((int) readOps);
        profile.setTotalWriteOperations((int) writeOps);

        // Count specific operations
        profile.setGetAllProductsCount((int) logs.stream()
                .filter(log -> "getAllProducts".equals(log.getAction()))
                .count());

        profile.setGetProductByIdCount((int) logs.stream()
                .filter(log -> "getProductById".equals(log.getAction()))
                .count());

        // Track product views
        logs.stream()
                .filter(log -> "getProductById".equals(log.getAction()))
                .filter(log -> log.getResourceId() != null)
                .forEach(log -> {
                    String productId = log.getResourceId();
                    String productName = log.getResourceName();
                    if (productName != null) {
                        profile.trackProductView(productId, productName);
                    }
                });

        return profile;
    }

    /**
     * Build WriteHeavyProfile from logs
     */
    private WriteHeavyProfile buildWriteHeavyProfile(String userName, String userEmail, List<LogEntry> logs) {
        WriteHeavyProfile profile = new WriteHeavyProfile();
        profile.setUserName(userName);
        profile.setUserEmail(userEmail);
        profile.setUserAge(0);

        long readOps = logs.stream()
                .filter(log -> "READ".equals(log.getOperationType()))
                .count();

        long writeOps = logs.stream()
                .filter(log -> "WRITE".equals(log.getOperationType()))
                .count();

        profile.setTotalReadOperations((int) readOps);
        profile.setTotalWriteOperations((int) writeOps);

        // Count specific operations
        profile.setAddProductCount((int) logs.stream()
                .filter(log -> "addProduct".equals(log.getAction()))
                .count());

        profile.setUpdateProductCount((int) logs.stream()
                .filter(log -> "updateProduct".equals(log.getAction()))
                .count());

        profile.setDeleteProductCount((int) logs.stream()
                .filter(log -> "deleteProduct".equals(log.getAction()))
                .count());

        // Track products modified
        logs.stream()
                .filter(log -> "WRITE".equals(log.getOperationType()))
                .filter(log -> log.getResourceId() != null)
                .forEach(log -> {
                    profile.incrementWriteOperation(log.getAction(), log.getResourceId());
                });

        return profile;
    }

    /**
     * Build ExpensiveProductSeekerProfile from logs
     */
    private ExpensiveProductSeekerProfile buildExpensiveSeekerProfile(String userName, String userEmail, List<LogEntry> logs) {
        ExpensiveProductSeekerProfile profile = new ExpensiveProductSeekerProfile();
        profile.setUserName(userName);
        profile.setUserEmail(userEmail);
        profile.setUserAge(0);

        // Track expensive product views
        logs.stream()
                .filter(log -> log.getResourcePrice() != null)
                .forEach(log -> {
                    String productId = log.getResourceId();
                    String productName = log.getResourceName();
                    Double price = log.getResourcePrice();

                    if (productId != null && productName != null && price != null) {
                        profile.trackProductView(productId, productName, price);
                    }
                });

        return profile;
    }

    /**
     * Convert LogEntry to OperationLog
     */
    private OperationLog convertToOperationLog(LogEntry logEntry) {
        OperationLog opLog = new OperationLog();
        opLog.setOperationName(logEntry.getAction());
        opLog.setTimestamp(logEntry.getTimestamp());
        opLog.setUserName(logEntry.getUserName());
        opLog.setUserEmail(logEntry.getUserEmail());
        opLog.setProductId(logEntry.getResourceId());
        opLog.setProductName(logEntry.getResourceName());
        opLog.setProductPrice(logEntry.getResourcePrice());
        opLog.setAdditionalInfo(logEntry.getAdditionalInfo());

        // Convert operation type
        if ("READ".equals(logEntry.getOperationType())) {
            opLog.setOperationType(UserOperationType.READ);
        } else if ("WRITE".equals(logEntry.getOperationType())) {
            opLog.setOperationType(UserOperationType.WRITE);
        } else if ("SEARCH_EXPENSIVE".equals(logEntry.getOperationType())) {
            opLog.setOperationType(UserOperationType.SEARCH_EXPENSIVE);
        }

        return opLog;
    }

    /**
     * Export extracted profiles to JSON
     */
    public void exportProfiles(Map<String, UserProfile> profiles) {
        log.info("Exporting {} extracted profiles...", profiles.size());

        for (Map.Entry<String, UserProfile> entry : profiles.entrySet()) {
            String userEmail = entry.getKey();
            UserProfile profile = entry.getValue();

            String fileName = String.format("%s/%s_%s_extracted.json",
                    EXTRACTED_PROFILES_DIR,
                    userEmail.replaceAll("[^a-zA-Z0-9]", "_"),
                    profile.getProfileType());

            try {
                objectMapper.writeValue(new File(fileName), profile);
                log.info("Exported extracted profile: {}", fileName);
            } catch (IOException e) {
                log.error("Failed to export profile for {}", userEmail, e);
            }
        }

        log.info("Profile extraction complete. {} profiles exported to {}",
                profiles.size(), EXTRACTED_PROFILES_DIR);
    }

    /**
     * Generate extraction report
     */
    public String generateExtractionReport(Map<String, UserProfile> profiles) {
        StringBuilder report = new StringBuilder();

        report.append("\n╔════════════════════════════════════════════════════════╗\n");
        report.append("║      PROFILE EXTRACTION REPORT (Question 5)        ║\n");
        report.append("╚════════════════════════════════════════════════════════╝\n\n");

        long readHeavy = profiles.values().stream()
                .filter(p -> p instanceof ReadHeavyProfile).count();
        long writeHeavy = profiles.values().stream()
                .filter(p -> p instanceof WriteHeavyProfile).count();
        long expensiveSeekers = profiles.values().stream()
                .filter(p -> p instanceof ExpensiveProductSeekerProfile).count();

        report.append(String.format("Total Profiles Extracted: %d\n", profiles.size()));
        report.append(String.format("  - READ-HEAVY: %d (%.1f%%)\n",
                readHeavy, (readHeavy * 100.0 / profiles.size())));
        report.append(String.format("  - WRITE-HEAVY: %d (%.1f%%)\n",
                writeHeavy, (writeHeavy * 100.0 / profiles.size())));
        report.append(String.format("  - EXPENSIVE-SEEKER: %d (%.1f%%)\n\n",
                expensiveSeekers, (expensiveSeekers * 100.0 / profiles.size())));

        report.append("Extracted Profiles:\n");
        report.append("══════════════════════════════════════════════════════\n\n");

        profiles.values().forEach(profile -> {
            report.append(String.format("User: %s <%s>\n",
                    profile.getUserName(), profile.getUserEmail()));
            report.append(String.format("  Profile Type: %s\n", profile.getProfileType()));
            report.append(String.format("  Description: %s\n", profile.getProfileDescription()));
            report.append(String.format("  Total Operations: %d\n", profile.getTotalOperations()));
            report.append(String.format("  Period: %s to %s\n\n",
                    profile.getProfileCreatedAt(), profile.getLastActivityAt()));
        });

        report.append("📁 Profiles exported to: ").append(EXTRACTED_PROFILES_DIR).append("/\n");

        return report.toString();
    }
}
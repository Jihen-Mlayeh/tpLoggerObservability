package com.example.logging_and_observability.profiling.service;

import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.profiling.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing user profiles based on their operations
 */
@Slf4j
@Service
public class UserProfileService {

    private static final String PROFILES_DIRECTORY = "user-profiles";
    private static final double READ_HEAVY_THRESHOLD = 60.0;  // 60% or more reads
    private static final double WRITE_HEAVY_THRESHOLD = 60.0; // 60% or more writes
    private static final double EXPENSIVE_THRESHOLD = 50.0;   // 50% or more expensive product views

    private final Map<String, UserProfile> userProfiles = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public UserProfileService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create profiles directory if not exists
        new File(PROFILES_DIRECTORY).mkdirs();

        log.info("UserProfileService initialized. Profiles directory: {}", PROFILES_DIRECTORY);
    }

    /**
     * Log an operation for a user
     */
    public void logOperation(User user, String operationName, UserOperationType operationType,
                             String productId, String productName, Double productPrice) {

        String userKey = getUserKey(user);

        UserProfile profile = userProfiles.computeIfAbsent(userKey, k ->
                createInitialProfile(user));

        // Create operation log
        OperationLog opLog = new OperationLog(
                operationName,
                operationType,
                LocalDateTime.now(),
                user.getName(),
                user.getEmail()
        );
        opLog.setProductId(productId);
        opLog.setProductName(productName);
        opLog.setProductPrice(productPrice);

        // Add to history
        profile.getOperationHistory().add(opLog);
        profile.setLastActivityAt(LocalDateTime.now());

        // Update profile based on operation type
        updateProfileStatistics(profile, operationName, operationType, productId, productName, productPrice);

        // Check if profile type should change
        UserProfile newProfile = determineProfileType(profile, user);
        if (!newProfile.getClass().equals(profile.getClass())) {
            log.info("Profile type changed for user {} from {} to {}",
                    user.getName(), profile.getProfileType(), newProfile.getProfileType());
            userProfiles.put(userKey, newProfile);
        }

        log.debug("Logged {} operation for user {}: {}", operationType, user.getName(), operationName);
    }

    /**
     * Create initial profile for a new user
     */
    private UserProfile createInitialProfile(User user) {
        ReadHeavyProfile profile = new ReadHeavyProfile();
        profile.setUserName(user.getName());
        profile.setUserEmail(user.getEmail());
        profile.setUserAge(user.getAge());
        profile.setProfileCreatedAt(LocalDateTime.now());
        profile.setLastActivityAt(LocalDateTime.now());
        profile.setTotalOperations(0);

        log.info("Created initial profile for user: {}", user.getName());
        return profile;
    }

    /**
     * Update profile statistics based on operation
     */
    private void updateProfileStatistics(UserProfile profile, String operationName,
                                         UserOperationType operationType, String productId,
                                         String productName, Double productPrice) {

        if (profile instanceof ReadHeavyProfile readProfile) {
            if (operationType == UserOperationType.READ) {
                readProfile.incrementReadOperation(operationName);
                if (productId != null && productName != null) {
                    readProfile.trackProductView(productId, productName);
                }
            } else if (operationType == UserOperationType.WRITE) {
                readProfile.incrementWriteOperation();
            }
        } else if (profile instanceof WriteHeavyProfile writeProfile) {
            if (operationType == UserOperationType.WRITE) {
                writeProfile.incrementWriteOperation(operationName, productId);
            } else if (operationType == UserOperationType.READ) {
                writeProfile.incrementReadOperation();
            }
        } else if (profile instanceof ExpensiveProductSeekerProfile expensiveProfile) {
            if (productId != null && productName != null && productPrice != null) {
                expensiveProfile.trackProductView(productId, productName, productPrice);
            }
        }
    }

    /**
     * Determine profile type based on user behavior
     */
    private UserProfile determineProfileType(UserProfile currentProfile, User user) {
        int totalOps = currentProfile.getTotalOperations();

        if (totalOps < 5) {
            // Not enough data, keep current profile
            return currentProfile;
        }

        // Count operation types
        long readOps = currentProfile.getOperationHistory().stream()
                .filter(log -> log.getOperationType() == UserOperationType.READ)
                .count();

        long writeOps = currentProfile.getOperationHistory().stream()
                .filter(log -> log.getOperationType() == UserOperationType.WRITE)
                .count();

        long expensiveViews = currentProfile.getOperationHistory().stream()
                .filter(log -> log.getProductPrice() != null && log.getProductPrice() >= 100.0)
                .count();

        double readPercentage = (readOps * 100.0) / totalOps;
        double writePercentage = (writeOps * 100.0) / totalOps;
        double expensivePercentage = (expensiveViews * 100.0) / totalOps;

        // Determine profile type
        if (expensivePercentage >= EXPENSIVE_THRESHOLD) {
            if (!(currentProfile instanceof ExpensiveProductSeekerProfile)) {
                return migrateToExpensiveSeeker(currentProfile, user);
            }
        } else if (writePercentage >= WRITE_HEAVY_THRESHOLD) {
            if (!(currentProfile instanceof WriteHeavyProfile)) {
                return migrateToWriteHeavy(currentProfile, user);
            }
        } else if (readPercentage >= READ_HEAVY_THRESHOLD) {
            if (!(currentProfile instanceof ReadHeavyProfile)) {
                return migrateToReadHeavy(currentProfile, user);
            }
        }

        return currentProfile;
    }

    /**
     * Migration methods
     */
    private ReadHeavyProfile migrateToReadHeavy(UserProfile from, User user) {
        ReadHeavyProfile profile = new ReadHeavyProfile();
        copyBaseFields(from, profile, user);
        reprocessOperations(profile);
        return profile;
    }

    private WriteHeavyProfile migrateToWriteHeavy(UserProfile from, User user) {
        WriteHeavyProfile profile = new WriteHeavyProfile();
        copyBaseFields(from, profile, user);
        reprocessOperations(profile);
        return profile;
    }

    private ExpensiveProductSeekerProfile migrateToExpensiveSeeker(UserProfile from, User user) {
        ExpensiveProductSeekerProfile profile = new ExpensiveProductSeekerProfile();
        copyBaseFields(from, profile, user);
        reprocessOperations(profile);
        return profile;
    }

    private void copyBaseFields(UserProfile from, UserProfile to, User user) {
        to.setUserName(user.getName());
        to.setUserEmail(user.getEmail());
        to.setUserAge(user.getAge());
        to.setProfileCreatedAt(from.getProfileCreatedAt());
        to.setLastActivityAt(from.getLastActivityAt());
        to.setTotalOperations(0); // Will be recalculated
        to.setOperationHistory(new ArrayList<>(from.getOperationHistory()));
    }

    private void reprocessOperations(UserProfile profile) {
        for (OperationLog log : profile.getOperationHistory()) {
            updateProfileStatistics(profile, log.getOperationName(), log.getOperationType(),
                    log.getProductId(), log.getProductName(), log.getProductPrice());
        }
    }

    /**
     * Get user profile
     */
    public Optional<UserProfile> getUserProfile(User user) {
        return Optional.ofNullable(userProfiles.get(getUserKey(user)));
    }

    /**
     * Get all profiles
     */
    public Collection<UserProfile> getAllProfiles() {
        return new ArrayList<>(userProfiles.values());
    }

    /**
     * Export profile to JSON file
     */
    public void exportProfileToJson(User user) throws IOException {
        UserProfile profile = userProfiles.get(getUserKey(user));
        if (profile == null) {
            log.warn("No profile found for user: {}", user.getName());
            return;
        }

        String fileName = String.format("%s/%s_%s_profile.json",
                PROFILES_DIRECTORY,
                user.getEmail().replaceAll("[^a-zA-Z0-9]", "_"),
                profile.getProfileType());

        objectMapper.writeValue(new File(fileName), profile);
        log.info("Exported profile for user {} to {}", user.getName(), fileName);
    }

    /**
     * Export all profiles to JSON
     */
    public void exportAllProfiles() {
        userProfiles.forEach((key, profile) -> {
            try {
                String fileName = String.format("%s/%s_%s_profile.json",
                        PROFILES_DIRECTORY,
                        profile.getUserEmail().replaceAll("[^a-zA-Z0-9]", "_"),
                        profile.getProfileType());

                objectMapper.writeValue(new File(fileName), profile);
                log.info("Exported profile: {}", fileName);
            } catch (IOException e) {
                log.error("Failed to export profile for {}", profile.getUserName(), e);
            }
        });

        log.info("Exported {} profiles to {}", userProfiles.size(), PROFILES_DIRECTORY);
    }

    /**
     * Generate summary report
     */
    public String generateSummaryReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════╗\n");
        report.append("║      USER PROFILING SUMMARY REPORT     ║\n");
        report.append("╚════════════════════════════════════════╝\n\n");

        long readHeavy = userProfiles.values().stream()
                .filter(p -> p instanceof ReadHeavyProfile).count();
        long writeHeavy = userProfiles.values().stream()
                .filter(p -> p instanceof WriteHeavyProfile).count();
        long expensiveSeekers = userProfiles.values().stream()
                .filter(p -> p instanceof ExpensiveProductSeekerProfile).count();

        report.append(String.format("Total Users: %d\n", userProfiles.size()));
        report.append(String.format("  - Read-Heavy Users: %d\n", readHeavy));
        report.append(String.format("  - Write-Heavy Users: %d\n", writeHeavy));
        report.append(String.format("  - Expensive Product Seekers: %d\n\n", expensiveSeekers));

        userProfiles.values().forEach(profile -> {
            report.append(String.format("User: %s <%s>\n", profile.getUserName(), profile.getUserEmail()));
            report.append(String.format("  Profile: %s\n", profile.getProfileType()));
            report.append(String.format("  Description: %s\n", profile.getProfileDescription()));
            report.append(String.format("  Total Operations: %d\n", profile.getTotalOperations()));
            report.append(String.format("  Last Activity: %s\n\n", profile.getLastActivityAt()));
        });

        return report.toString();
    }

    private String getUserKey(User user) {
        return user.getEmail();
    }
}

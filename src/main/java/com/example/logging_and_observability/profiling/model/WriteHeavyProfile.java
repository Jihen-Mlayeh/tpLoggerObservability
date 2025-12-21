package com.example.logging_and_observability.profiling.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Profile for users who mostly perform WRITE operations
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WriteHeavyProfile extends UserProfile {

    private int totalReadOperations;
    private int totalWriteOperations;
    private double writePercentage;

    // Statistics by operation type
    private int addProductCount;
    private int updateProductCount;
    private int deleteProductCount;

    // Tracking products modified
    private Map<String, Integer> productsModified = new HashMap<>();
    private Map<String, Integer> operationTypeCount = new HashMap<>();

    @Override
    public String getProfileType() {
        return "WRITE_HEAVY";
    }

    @Override
    public String getProfileDescription() {
        return String.format("User who performs mostly WRITE operations (%.1f%% writes)", writePercentage);
    }

    public void incrementWriteOperation(String operationName, String productId) {
        totalWriteOperations++;
        totalOperations++;

        // Track specific operation types
        switch (operationName) {
            case "addProduct" -> addProductCount++;
            case "updateProduct" -> updateProductCount++;
            case "deleteProduct" -> deleteProductCount++;
        }

        // Track operation frequency
        operationTypeCount.put(operationName, operationTypeCount.getOrDefault(operationName, 0) + 1);

        // Track products modified
        if (productId != null) {
            productsModified.put(productId, productsModified.getOrDefault(productId, 0) + 1);
        }

        updateWritePercentage();
    }

    public void incrementReadOperation() {
        totalReadOperations++;
        totalOperations++;
        updateWritePercentage();
    }

    private void updateWritePercentage() {
        if (totalOperations > 0) {
            writePercentage = (totalWriteOperations * 100.0) / totalOperations;
        }
    }

    public String getMostFrequentWriteOperation() {
        return operationTypeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }
}

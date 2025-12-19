package com.example.logging_and_observability.profiling.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Profile for users who mostly perform READ operations
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReadHeavyProfile extends UserProfile {

    private int totalReadOperations;
    private int totalWriteOperations;
    private double readPercentage;

    // Statistics
    private int getAllProductsCount;
    private int getProductByIdCount;

    // Most viewed products
    private Map<Long, Integer> productViewCount = new HashMap<>();
    private Map<Long, String> productNames = new HashMap<>();

    @Override
    public String getProfileType() {
        return "READ_HEAVY";
    }

    @Override
    public String getProfileDescription() {
        return String.format("User who performs mostly READ operations (%.1f%% reads)", readPercentage);
    }

    public void incrementReadOperation(String operationName) {
        totalReadOperations++;
        totalOperations++;

        if ("getAllProducts".equals(operationName)) {
            getAllProductsCount++;
        } else if ("getProductById".equals(operationName)) {
            getProductByIdCount++;
        }

        updateReadPercentage();
    }

    public void incrementWriteOperation() {
        totalWriteOperations++;
        totalOperations++;
        updateReadPercentage();
    }

    public void trackProductView(Long productId, String productName) {
        productViewCount.put(productId, productViewCount.getOrDefault(productId, 0) + 1);
        productNames.put(productId, productName);
    }

    private void updateReadPercentage() {
        if (totalOperations > 0) {
            readPercentage = (totalReadOperations * 100.0) / totalOperations;
        }
    }

    public Map<Long, Integer> getTopViewedProducts(int limit) {
        return productViewCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

    public double getReadPercentage() {
        if (totalOperations > 0) {
            return (totalReadOperations * 100.0) / totalOperations;
        }
        return 0.0;
    }
}

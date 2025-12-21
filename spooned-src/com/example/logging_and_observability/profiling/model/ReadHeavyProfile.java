package com.example.logging_and_observability.profiling.model;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
    private Map<String, Integer> productViewCount = new HashMap<>();

    private Map<String, String> productNames = new HashMap<>();

    @Override
    public String getProfileType() {
        return "READ_HEAVY";
    }

    @Override
    public String getProfileDescription() {
        return String.format("User who performs mostly READ operations (%.1f%% reads)", getReadPercentage());
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

    public void trackProductView(String productId, String productName) {
        productViewCount.put(productId, productViewCount.getOrDefault(productId, 0) + 1);
        productNames.put(productId, productName);
    }

    private void updateReadPercentage() {
        if (totalOperations > 0) {
            readPercentage = (totalReadOperations * 100.0) / totalOperations;
        }
    }

    public Map<String, Integer> getTopViewedProducts(int limit) {
        return productViewCount.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(limit).collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

    public double getReadPercentage() {
        if (totalOperations > 0) {
            return (totalReadOperations * 100.0) / totalOperations;
        }
        return 0.0;
    }
}
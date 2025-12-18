package com.example.logging_and_observability.profiling.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.DoubleSummaryStatistics;

/**
 * Profile for users who search for expensive products
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExpensiveProductSeekerProfile extends UserProfile {

    private static final double EXPENSIVE_THRESHOLD = 100.0; // Products above €100

    private int expensiveProductViews;
    private int totalProductViews;
    private double expensiveProductViewPercentage;

    // Price statistics
    private Double averagePriceViewed;
    private Double highestPriceViewed;
    private Double lowestPriceViewed;

    // List of expensive products viewed
    private List<ExpensiveProductView> expensiveProducts = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class ExpensiveProductView {
        private Long productId;
        private String productName;
        private Double price;
        private int viewCount;

        public ExpensiveProductView(Long productId, String productName, Double price) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.viewCount = 1;
        }
    }

    @Override
    public String getProfileType() {
        return "EXPENSIVE_SEEKER";
    }

    @Override
    public String getProfileDescription() {
        return String.format("User interested in expensive products (%.1f%% of views > €%.2f)",
                expensiveProductViewPercentage, EXPENSIVE_THRESHOLD);
    }

    public void trackProductView(Long productId, String productName, Double price) {
        totalProductViews++;
        totalOperations++;

        if (price != null && price >= EXPENSIVE_THRESHOLD) {
            expensiveProductViews++;

            // Find existing or create new
            ExpensiveProductView existing = expensiveProducts.stream()
                    .filter(p -> p.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setViewCount(existing.getViewCount() + 1);
            } else {
                expensiveProducts.add(new ExpensiveProductView(productId, productName, price));
            }
        }

        updateStatistics(price);
    }

    private void updateStatistics(Double price) {
        // Update percentage
        if (totalProductViews > 0) {
            expensiveProductViewPercentage = (expensiveProductViews * 100.0) / totalProductViews;
        }

        // Update price statistics
        if (price != null) {
            if (highestPriceViewed == null || price > highestPriceViewed) {
                highestPriceViewed = price;
            }
            if (lowestPriceViewed == null || price < lowestPriceViewed) {
                lowestPriceViewed = price;
            }
        }

        // Calculate average
        DoubleSummaryStatistics stats = operationHistory.stream()
                .filter(log -> log.getProductPrice() != null)
                .mapToDouble(OperationLog::getProductPrice)
                .summaryStatistics();

        if (stats.getCount() > 0) {
            averagePriceViewed = stats.getAverage();
        }
    }

    public List<ExpensiveProductView> getTopExpensiveProducts(int limit) {
        return expensiveProducts.stream()
                .sorted((a, b) -> Double.compare(b.getPrice(), a.getPrice()))
                .limit(limit)
                .toList();
    }
}

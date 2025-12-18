package com.example.logging_and_observability.scenario;

import com.example.logging_and_observability.model.Product;
import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.service.ProductService;
import com.example.logging_and_observability.service.UserService;
import com.example.logging_and_observability.profiling.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Automated scenario executor for generating diverse user profiles
 * Creates 10 users with 20+ operations each
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioExecutor {

    private final UserService userService;
    private final ProductService productService;
    private final UserProfileService userProfileService;

    private final Random random = new Random();
    private final List<Product> testProducts = new ArrayList<>();

    /**
     * Execute all scenarios
     */
    public void executeAllScenarios() {
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║       SCENARIO EXECUTION - QUESTION 4                  ║");
        log.info("║  Generating 10 users with diverse operation patterns  ║");
        log.info("╚════════════════════════════════════════════════════════╝");

        try {
            // Step 1: Setup test data
            setupTestProducts();

            // Step 2: Create and execute scenarios for 10 different users
            executeScenario1_CasualBrowser();      // READ-HEAVY
            executeScenario2_PowerShopper();       // WRITE-HEAVY
            executeScenario3_LuxuryHunter();       // EXPENSIVE-SEEKER
            executeScenario4_ProductManager();     // WRITE-HEAVY
            executeScenario5_WindowShopper();      // READ-HEAVY
            executeScenario6_TechEnthusiast();     // EXPENSIVE-SEEKER
            executeScenario7_BudgetBuyer();        // READ-HEAVY
            executeScenario8_InventoryClerk();     // WRITE-HEAVY
            executeScenario9_ComparisonShopper();  // READ-HEAVY + EXPENSIVE
            executeScenario10_DataAnalyst();       // READ-HEAVY

            // Step 3: Generate final report
            generateFinalReport();

            log.info("\n✅ All scenarios executed successfully!");
            log.info("📊 Check user-profiles/ directory for generated profiles");

        } catch (Exception e) {
            log.error("❌ Scenario execution failed", e);
        }
    }

    /**
     * Setup test products in database
     */
    private void setupTestProducts() {
        log.info("\n📦 Setting up test products...");

        testProducts.clear();

        // Budget products (< €50)
        testProducts.add(createProduct(1L, "USB Cable", 9.99));
        testProducts.add(createProduct(2L, "Mouse Pad", 14.99));
        testProducts.add(createProduct(3L, "HDMI Cable", 19.99));
        testProducts.add(createProduct(4L, "Phone Case", 24.99));
        testProducts.add(createProduct(5L, "Keyboard Cover", 29.99));

        // Mid-range products (€50-€100)
        testProducts.add(createProduct(6L, "Wireless Mouse", 49.99));
        testProducts.add(createProduct(7L, "Mechanical Keyboard", 79.99));
        testProducts.add(createProduct(8L, "Webcam HD", 89.99));
        testProducts.add(createProduct(9L, "Desk Lamp", 69.99));
        testProducts.add(createProduct(10L, "USB Hub", 59.99));

        // Expensive products (> €100)
        testProducts.add(createProduct(11L, "Gaming Monitor", 299.99));
        testProducts.add(createProduct(12L, "Mechanical Keyboard RGB", 149.99));
        testProducts.add(createProduct(13L, "Noise Cancelling Headphones", 349.99));
        testProducts.add(createProduct(14L, "iPad Pro", 899.99));
        testProducts.add(createProduct(15L, "MacBook Air M3", 1299.99));

        // Premium/Luxury products (> €1000)
        testProducts.add(createProduct(16L, "MacBook Pro M3 Max", 2499.99));
        testProducts.add(createProduct(17L, "iPhone 15 Pro Max 1TB", 1599.99));
        testProducts.add(createProduct(18L, "Sony A7R V Camera", 3899.99));
        testProducts.add(createProduct(19L, "LG OLED TV 77\"", 2999.99));
        testProducts.add(createProduct(20L, "Gaming Laptop RTX 4090", 3499.99));

        // Add all products to database
        for (Product product : testProducts) {
            try {
                productService.addProduct(product);
            } catch (Exception e) {
                // Product might already exist, that's ok
                log.debug("Product {} already exists", product.getName());
            }
        }

        log.info("✅ Created {} test products", testProducts.size());
    }

    private Product createProduct(Long id, String name, double price) {
        LocalDate expiration = LocalDate.now().plusMonths(
                ThreadLocalRandom.current().nextInt(6, 24)
        );
        return new Product(id, name, price, expiration);
    }

    // ============================================================================
    //                          SCENARIO 1: CASUAL BROWSER
    // ============================================================================

    /**
     * Scenario 1: Casual Browser (READ-HEAVY profile expected)
     * - Mostly browses products
     * - Rarely makes purchases
     * - Views many products but doesn't commit
     */
    private void executeScenario1_CasualBrowser() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 1: Casual Browser (Alice)   ║");
        log.info("╚════════════════════════════════════════╝");

        User alice = createUser(
                "Alice Johnson",
                28,
                "alice.johnson@email.com",
                "alice123"
        );

        productService.setCurrentUser(alice);

        // 22 operations: 20 reads, 2 writes
        for (int i = 0; i < 4; i++) {
            productService.getAllProducts(); // Browse all
            sleep(100);
        }

        for (int i = 0; i < 16; i++) {
            Product randomProduct = getRandomProduct();
            productService.getProductById(randomProduct.getId());
            sleep(50);
        }

        // Buy 2 cheap products
        productService.addProduct(createProduct(101L, "Alice's USB Drive", 19.99));
        sleep(100);
        productService.addProduct(createProduct(102L, "Alice's Phone Stand", 24.99));

        productService.clearCurrentUser();
        exportProfile(alice, "READ-HEAVY");
    }

    // ============================================================================
    //                          SCENARIO 2: POWER SHOPPER
    // ============================================================================

    /**
     * Scenario 2: Power Shopper (WRITE-HEAVY profile expected)
     * - Frequently adds products
     * - Updates product details
     * - Manages inventory actively
     */
    private void executeScenario2_PowerShopper() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 2: Power Shopper (Bob)      ║");
        log.info("╚════════════════════════════════════════╝");

        User bob = createUser(
                "Bob Smith",
                35,
                "bob.smith@email.com",
                "bob123"
        );

        productService.setCurrentUser(bob);

        // 24 operations: 6 reads, 18 writes
        productService.getAllProducts(); // Check inventory
        sleep(100);

        // Add 10 products
        for (int i = 0; i < 10; i++) {
            productService.addProduct(createProduct(
                    200L + i,
                    "Bob's Product " + (i + 1),
                    random.nextDouble() * 200 + 50
            ));
            sleep(50);
        }

        // Browse a bit
        for (int i = 0; i < 5; i++) {
            productService.getProductById(getRandomProduct().getId());
            sleep(50);
        }

        // Update 5 products
        for (int i = 0; i < 5; i++) {
            Product updated = createProduct(
                    null,
                    "Bob's Updated Product " + (i + 1),
                    random.nextDouble() * 150 + 100
            );
            productService.updateProduct(200L + i, updated);
            sleep(50);
        }

        // Delete 3 products
        for (int i = 0; i < 3; i++) {
            try {
                productService.deleteProduct(207L + i);
            } catch (Exception e) {
                log.debug("Delete failed, continuing...");
            }
            sleep(50);
        }

        productService.clearCurrentUser();
        exportProfile(bob, "WRITE-HEAVY");
    }

    // ============================================================================
    //                          SCENARIO 3: LUXURY HUNTER
    // ============================================================================

    /**
     * Scenario 3: Luxury Hunter (EXPENSIVE-SEEKER profile expected)
     * - Only interested in premium products (>€1000)
     * - Views expensive items multiple times
     * - Eventually purchases luxury goods
     */
    private void executeScenario3_LuxuryHunter() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 3: Luxury Hunter (Charlie)  ║");
        log.info("╚════════════════════════════════════════╝");

        User charlie = createUser(
                "Charlie Davis",
                42,
                "charlie.davis@email.com",
                "charlie123"
        );

        productService.setCurrentUser(charlie);

        // 25 operations: Focus on expensive products
        productService.getAllProducts(); // Initial browse
        sleep(100);

        // View expensive products multiple times (16-20: MacBook Pro, iPhone, Camera, TV, Gaming Laptop)
        List<Long> expensiveIds = Arrays.asList(16L, 17L, 18L, 19L, 20L);

        for (int round = 0; round < 4; round++) {
            for (Long id : expensiveIds) {
                try {
                    productService.getProductById(id);
                    sleep(50);
                } catch (Exception e) {
                    log.debug("Product not found: {}", id);
                }
            }
        }

        // Also view some mid-expensive (11-15)
        for (Long id = 11L; id <= 15L; id++) {
            try {
                productService.getProductById(id);
                sleep(50);
            } catch (Exception e) {
                log.debug("Product not found: {}", id);
            }
        }

        productService.clearCurrentUser();
        exportProfile(charlie, "EXPENSIVE-SEEKER");
    }

    // ============================================================================
    //                          SCENARIO 4: PRODUCT MANAGER
    // ============================================================================

    /**
     * Scenario 4: Product Manager (WRITE-HEAVY profile expected)
     * - Manages product catalog
     * - Frequent updates and deletions
     * - Data maintenance focus
     */
    private void executeScenario4_ProductManager() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 4: Product Manager (Diana)  ║");
        log.info("╚════════════════════════════════════════╝");

        User diana = createUser(
                "Diana Martinez",
                31,
                "diana.martinez@email.com",
                "diana123"
        );

        productService.setCurrentUser(diana);

        // 26 operations: 5 reads, 21 writes
        for (int i = 0; i < 2; i++) {
            productService.getAllProducts();
            sleep(100);
        }

        // Add 8 products
        for (int i = 0; i < 8; i++) {
            productService.addProduct(createProduct(
                    300L + i,
                    "Diana's Managed Product " + (i + 1),
                    random.nextDouble() * 300 + 20
            ));
            sleep(50);
        }

        // Review products
        for (int i = 0; i < 3; i++) {
            productService.getProductById(300L + i);
            sleep(50);
        }

        // Update 8 products (price adjustments, descriptions)
        for (int i = 0; i < 8; i++) {
            Product updated = createProduct(
                    null,
                    "Diana's Updated Product " + (i + 1),
                    random.nextDouble() * 250 + 50
            );
            try {
                productService.updateProduct(300L + i, updated);
            } catch (Exception e) {
                log.debug("Update failed, continuing...");
            }
            sleep(50);
        }

        // Archive old products (delete 5)
        for (int i = 0; i < 5; i++) {
            try {
                productService.deleteProduct(303L + i);
            } catch (Exception e) {
                log.debug("Delete failed, continuing...");
            }
            sleep(50);
        }

        productService.clearCurrentUser();
        exportProfile(diana, "WRITE-HEAVY");
    }

    // ============================================================================
    //                          SCENARIO 5: WINDOW SHOPPER
    // ============================================================================

    /**
     * Scenario 5: Window Shopper (READ-HEAVY profile expected)
     * - Browses extensively but never buys
     * - Comparison shopping behavior
     * - Views same products multiple times
     */
    private void executeScenario5_WindowShopper() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 5: Window Shopper (Eve)     ║");
        log.info("╚════════════════════════════════════════╝");

        User eve = createUser(
                "Eve Wilson",
                24,
                "eve.wilson@email.com",
                "eve123"
        );

        productService.setCurrentUser(eve);

        // 28 operations: 28 reads, 0 writes
        for (int i = 0; i < 6; i++) {
            productService.getAllProducts();
            sleep(100);
        }

        // View products randomly, some multiple times
        List<Long> favoriteIds = Arrays.asList(7L, 8L, 12L, 13L, 15L);

        for (int round = 0; round < 3; round++) {
            for (Long id : favoriteIds) {
                try {
                    productService.getProductById(id);
                    sleep(50);
                } catch (Exception e) {
                    log.debug("Product not found: {}", id);
                }
            }
        }

        // Browse more random products
        for (int i = 0; i < 7; i++) {
            productService.getProductById(getRandomProduct().getId());
            sleep(50);
        }

        productService.clearCurrentUser();
        exportProfile(eve, "READ-HEAVY");
    }

    // ============================================================================
    //                          SCENARIO 6: TECH ENTHUSIAST
    // ============================================================================

    /**
     * Scenario 6: Tech Enthusiast (EXPENSIVE-SEEKER profile expected)
     * - Interested in high-end tech
     * - Researches expensive gadgets
     * - Eventual buyer of premium tech
     */
    private void executeScenario6_TechEnthusiast() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 6: Tech Enthusiast (Frank)  ║");
        log.info("╚════════════════════════════════════════╝");

        User frank = createUser(
                "Frank Zhang",
                29,
                "frank.zhang@email.com",
                "frank123"
        );

        productService.setCurrentUser(frank);

        // 23 operations: Heavy focus on tech products >€100
        productService.getAllProducts();
        sleep(100);

        // Research phase: view all expensive tech (15-20)
        for (int round = 0; round < 3; round++) {
            for (Long id = 15L; id <= 20L; id++) {
                try {
                    productService.getProductById(id);
                    sleep(50);
                } catch (Exception e) {
                    log.debug("Product not found: {}", id);
                }
            }
        }

        // Purchase expensive product
        productService.addProduct(createProduct(
                400L,
                "Frank's Custom Gaming PC",
                3999.99
        ));

        productService.clearCurrentUser();
        exportProfile(frank, "EXPENSIVE-SEEKER");
    }

    // ============================================================================
    //                          SCENARIO 7: BUDGET BUYER
    // ============================================================================

    /**
     * Scenario 7: Budget Buyer (READ-HEAVY profile expected)
     * - Only looks at cheap products (<€50)
     * - Price-conscious shopper
     * - Many views, few purchases
     */
    private void executeScenario7_BudgetBuyer() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 7: Budget Buyer (Grace)     ║");
        log.info("╚════════════════════════════════════════╝");

        User grace = createUser(
                "Grace Lee",
                22,
                "grace.lee@email.com",
                "grace123"
        );

        productService.setCurrentUser(grace);

        // 24 operations: 22 reads, 2 writes
        for (int i = 0; i < 5; i++) {
            productService.getAllProducts();
            sleep(100);
        }

        // View budget products (1-10) multiple times
        for (int round = 0; round < 3; round++) {
            for (Long id = 1L; id <= 5L; id++) {
                try {
                    productService.getProductById(id);
                    sleep(50);
                } catch (Exception e) {
                    log.debug("Product not found: {}", id);
                }
            }
        }

        // Buy 2 cheap items
        productService.addProduct(createProduct(500L, "Grace's Notebook", 5.99));
        productService.addProduct(createProduct(501L, "Grace's Pen Set", 12.99));

        productService.clearCurrentUser();
        exportProfile(grace, "READ-HEAVY");
    }

    // ============================================================================
    //                          SCENARIO 8: INVENTORY CLERK
    // ============================================================================

    /**
     * Scenario 8: Inventory Clerk (WRITE-HEAVY profile expected)
     * - Manages stock levels
     * - Frequent additions and removals
     * - Data entry focused
     */
    private void executeScenario8_InventoryClerk() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 8: Inventory Clerk (Henry)  ║");
        log.info("╚════════════════════════════════════════╝");

        User henry = createUser(
                "Henry Brown",
                38,
                "henry.brown@email.com",
                "henry123"
        );

        productService.setCurrentUser(henry);

        // 27 operations: 4 reads, 23 writes
        productService.getAllProducts(); // Check current stock
        sleep(100);

        // Add new stock (12 items)
        for (int i = 0; i < 12; i++) {
            productService.addProduct(createProduct(
                    600L + i,
                    "Henry's Stock Item " + (i + 1),
                    random.nextDouble() * 500 + 10
            ));
            sleep(50);
        }

        // Verify additions
        for (int i = 0; i < 3; i++) {
            productService.getProductById(600L + i);
            sleep(50);
        }

        // Update prices (6 items)
        for (int i = 0; i < 6; i++) {
            Product updated = createProduct(
                    null,
                    "Henry's Updated Stock " + (i + 1),
                    random.nextDouble() * 400 + 20
            );
            try {
                productService.updateProduct(600L + i, updated);
            } catch (Exception e) {
                log.debug("Update failed");
            }
            sleep(50);
        }

        // Remove damaged stock (5 items)
        for (int i = 0; i < 5; i++) {
            try {
                productService.deleteProduct(607L + i);
            } catch (Exception e) {
                log.debug("Delete failed");
            }
            sleep(50);
        }

        productService.clearCurrentUser();
        exportProfile(henry, "WRITE-HEAVY");
    }

    // ============================================================================
    //                          SCENARIO 9: COMPARISON SHOPPER
    // ============================================================================

    /**
     * Scenario 9: Comparison Shopper (Mixed profile)
     * - Compares expensive vs budget options
     * - Research-heavy behavior
     * - Eventually buys mid-range
     */
    private void executeScenario9_ComparisonShopper() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 9: Comparison Shopper (Ivy) ║");
        log.info("╚════════════════════════════════════════╝");

        User ivy = createUser(
                "Ivy Chen",
                27,
                "ivy.chen@email.com",
                "ivy123"
        );

        productService.setCurrentUser(ivy);

        // 25 operations: Mix of reads focusing on price comparison
        productService.getAllProducts();
        sleep(100);

        // Compare expensive options (3 rounds)
        for (int round = 0; round < 3; round++) {
            for (Long id = 14L; id <= 17L; id++) {
                try {
                    productService.getProductById(id);
                    sleep(50);
                } catch (Exception e) {
                    log.debug("Product not found: {}", id);
                }
            }
        }

        // Check budget alternatives
        for (Long id = 6L; id <= 10L; id++) {
            try {
                productService.getProductById(id);
                sleep(50);
            } catch (Exception e) {
                log.debug("Product not found: {}", id);
            }
        }

        // Final decision: buy mid-range
        productService.addProduct(createProduct(
                700L,
                "Ivy's Mid-Range Laptop",
                799.99
        ));

        productService.clearCurrentUser();
        exportProfile(ivy, "READ-HEAVY or EXPENSIVE-SEEKER");
    }

    // ============================================================================
    //                          SCENARIO 10: DATA ANALYST
    // ============================================================================

    /**
     * Scenario 10: Data Analyst (READ-HEAVY profile expected)
     * - Analyzing product catalog
     * - Systematic viewing of all products
     * - No modifications, pure research
     */
    private void executeScenario10_DataAnalyst() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║  SCENARIO 10: Data Analyst (Jack)     ║");
        log.info("╚════════════════════════════════════════╝");

        User jack = createUser(
                "Jack Miller",
                33,
                "jack.miller@email.com",
                "jack123"
        );

        productService.setCurrentUser(jack);

        // 30 operations: 30 reads, 0 writes - Pure analysis
        for (int i = 0; i < 10; i++) {
            productService.getAllProducts(); // Systematic catalog review
            sleep(100);
        }

        // Systematic viewing of each product
        for (Long id = 1L; id <= 20L; id++) {
            try {
                productService.getProductById(id);
                sleep(50);
            } catch (Exception e) {
                log.debug("Product not found: {}", id);
            }
        }

        productService.clearCurrentUser();
        exportProfile(jack, "READ-HEAVY");
    }

    // ============================================================================
    //                              HELPER METHODS
    // ============================================================================

    private User createUser(String name, int age, String email, String password) {
        try {
            User user = new User(name, age, email, password);
            User registered = userService.registerUser(user);
            log.info("✅ Created user: {} ({})", name, email);
            return registered;
        } catch (Exception e) {
            log.warn("User {} already exists, retrieving...", email);
            return userService.getUserByEmail(email);
        }
    }

    private Product getRandomProduct() {
        if (testProducts.isEmpty()) {
            return testProducts.get(0);
        }
        return testProducts.get(random.nextInt(testProducts.size()));
    }

    private void exportProfile(User user, String expectedProfile) {
        try {
            userProfileService.exportProfileToJson(user);
            log.info("📊 Exported profile for {} - Expected: {}", user.getName(), expectedProfile);
        } catch (Exception e) {
            log.error("Failed to export profile for {}", user.getName(), e);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generateFinalReport() {
        log.info("\n╔════════════════════════════════════════════════════════╗");
        log.info("║              FINAL SCENARIO REPORT                     ║");
        log.info("╚════════════════════════════════════════════════════════╝\n");

        String report = userProfileService.generateSummaryReport();
        log.info(report);

        // Export all profiles
        userProfileService.exportAllProfiles();

        log.info("\n📁 All profiles exported to: user-profiles/");
        log.info("✅ Question 4 completed successfully!");
    }
}

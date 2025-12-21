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

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioExecutor {

    private final UserService userService;
    private final ProductService productService;
    private final UserProfileService userProfileService;

    private final Random random = new Random();
    private final List<Product> testProducts = new ArrayList<>();

    public void executeAllScenarios() {
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║       SCENARIO EXECUTION - QUESTION 4                  ║");
        log.info("║  Generating 10 users with diverse operation patterns  ║");
        log.info("╚════════════════════════════════════════════════════════╝");

        try {
            setupTestProducts();

            executeScenario1_CasualBrowser();
            executeScenario2_PowerShopper();
            executeScenario3_LuxuryHunter();
            executeScenario4_ProductManager();
            executeScenario5_WindowShopper();
            executeScenario6_TechEnthusiast();
            executeScenario7_BudgetBuyer();
            executeScenario8_InventoryClerk();
            executeScenario9_ComparisonShopper();
            executeScenario10_DataAnalyst();

            generateFinalReport();

        } catch (Exception e) {
            log.error("❌ Scenario execution failed", e);
        }
    }

    private void setupTestProducts() {
        log.info("\n📦 Setting up test products...");
        testProducts.clear();

        // Budget products (< €50)
        addProductToTest("USB Cable", 9.99);
        addProductToTest("Mouse Pad", 14.99);
        addProductToTest("HDMI Cable", 19.99);
        addProductToTest("Phone Case", 24.99);
        addProductToTest("Keyboard Cover", 29.99);

        // Mid-range products (€50-€100)
        addProductToTest("Wireless Mouse", 49.99);
        addProductToTest("Mechanical Keyboard", 79.99);
        addProductToTest("Webcam HD", 89.99);
        addProductToTest("Desk Lamp", 69.99);
        addProductToTest("USB Hub", 59.99);

        // Expensive products (> €100)
        addProductToTest("Gaming Monitor", 299.99);
        addProductToTest("Mechanical Keyboard RGB", 149.99);
        addProductToTest("Noise Cancelling Headphones", 349.99);
        addProductToTest("iPad Pro", 899.99);
        addProductToTest("MacBook Air M3", 1299.99);

        // Premium/Luxury products (> €1000)
        addProductToTest("MacBook Pro M3 Max", 2499.99);
        addProductToTest("iPhone 15 Pro Max 1TB", 1599.99);
        addProductToTest("Sony A7R V Camera", 3899.99);
        addProductToTest("LG OLED TV 77\"", 2999.99);
        addProductToTest("Gaming Laptop RTX 4090", 3499.99);

        log.info("✅ Created {} test products", testProducts.size());
    }

    private void addProductToTest(String name, double price) {
        Product product = createProduct(name, price);
        try {
            productService.addProduct(product);
            testProducts.add(product);
        } catch (Exception e) {
            log.debug("Product {} already exists", product.getName());
        }
    }

    private Product createProduct(String name, double price) {
        LocalDate expiration = LocalDate.now().plusMonths(ThreadLocalRandom.current().nextInt(6, 24));
        return new Product(null, name, price, expiration); // id = null -> MongoDB generates it
    }

    private Product getRandomProduct() {
        if (testProducts.isEmpty()) throw new IllegalStateException("No test products available");
        return testProducts.get(random.nextInt(testProducts.size()));
    }

    private User createUser(String name, int age, String email, String password) {
        try {
            User user = new User(name, age, email, password);
            return userService.registerUser(user);
        } catch (Exception e) {
            return userService.getUserByEmail(email);
        }
    }

    private void exportProfile(User user, String expectedProfile) {
        try {
            userProfileService.exportProfileToJson(user);
            log.info("📊 Exported profile for {} - Expected: {}", user.getName(), expectedProfile);
        } catch (Exception e) {
            log.error("Failed to export profile for {}", user.getName(), e);
        }
    }

    private void generateFinalReport() {
        log.info("\n╔════════════════════════════════════════════════════════╗");
        log.info("║              FINAL SCENARIO REPORT                     ║");
        log.info("╚════════════════════════════════════════════════════════╝\n");

        String report = userProfileService.generateSummaryReport();
        log.info(report);

        userProfileService.exportAllProfiles();

        log.info("\n📁 All profiles exported to: user-profiles/");
        log.info("✅ Question 4 completed successfully!");
    }

    // =================== SCENARIOS 1-10 ===================

    private void executeScenario1_CasualBrowser() {
        User alice = createUser("Alice Johnson", 28, "alice.johnson@email.com", "alice123");
        productService.setCurrentUser(alice);

        for (int i = 0; i < 4; i++) productService.getAllProducts();
        for (int i = 0; i < 16; i++) productService.getProductById(getRandomProduct().getId());

        Product p1 = createProduct("Alice's USB Drive", 19.99);
        Product p2 = createProduct("Alice's Phone Stand", 24.99);
        productService.addProduct(p1);
        productService.addProduct(p2);
        testProducts.add(p1);
        testProducts.add(p2);

        productService.clearCurrentUser();
        exportProfile(alice, "READ-HEAVY");
    }

    private void executeScenario2_PowerShopper() {
        User bob = createUser("Bob Smith", 35, "bob.smith@email.com", "bob123");
        productService.setCurrentUser(bob);

        productService.getAllProducts();

        for (int i = 0; i < 10; i++) {
            Product p = createProduct("Bob's Product " + (i + 1), random.nextDouble() * 200 + 50);
            productService.addProduct(p);
            testProducts.add(p);
        }

        for (int i = 0; i < 5; i++) productService.getProductById(getRandomProduct().getId());

        for (int i = 0; i < 5; i++) {
            Product oldProduct = testProducts.get(random.nextInt(testProducts.size()));
            Product updated = createProduct("Bob's Updated Product " + (i + 1), random.nextDouble() * 150 + 100);
            updated.setId(oldProduct.getId());
            productService.updateProduct(updated.getId(), updated);
        }

        productService.clearCurrentUser();
        exportProfile(bob, "WRITE-HEAVY");
    }

    private void executeScenario3_LuxuryHunter() {
        User charlie = createUser("Charlie Davis", 42, "charlie.davis@email.com", "charlie123");
        productService.setCurrentUser(charlie);

        productService.getAllProducts();

        List<Product> expensiveProducts = testProducts.subList(15, 20); // 16-20
        for (int round = 0; round < 4; round++)
            for (Product p : expensiveProducts) productService.getProductById(p.getId());

        List<Product> expensiveMid = testProducts.subList(10, 15); // 11-15
        for (Product p : expensiveMid) productService.getProductById(p.getId());

        productService.clearCurrentUser();
        exportProfile(charlie, "EXPENSIVE-SEEKER");
    }

    private void executeScenario4_ProductManager() {
        User diana = createUser("Diana Martinez", 31, "diana.martinez@email.com", "diana123");
        productService.setCurrentUser(diana);

        for (int i = 0; i < 2; i++) productService.getAllProducts();

        for (int i = 0; i < 8; i++) {
            Product p = createProduct("Diana's Managed Product " + (i + 1), random.nextDouble() * 300 + 20);
            productService.addProduct(p);
            testProducts.add(p);
        }

        for (int i = 0; i < 8; i++) {
            Product oldProduct = testProducts.get(testProducts.size() - 8 + i);
            Product updated = createProduct("Diana's Updated Product " + (i + 1), random.nextDouble() * 250 + 50);
            updated.setId(oldProduct.getId());
            productService.updateProduct(updated.getId(), updated);
        }

        productService.clearCurrentUser();
        exportProfile(diana, "WRITE-HEAVY");
    }

    private void executeScenario5_WindowShopper() {
        User eve = createUser("Eve Wilson", 24, "eve.wilson@email.com", "eve123");
        productService.setCurrentUser(eve);

        for (int i = 0; i < 6; i++) productService.getAllProducts();

        List<Product> favorites = Arrays.asList(
                testProducts.get(6), // Mechanical Keyboard
                testProducts.get(7), // Webcam HD
                testProducts.get(12), // Mechanical Keyboard RGB
                testProducts.get(13), // Noise Cancelling Headphones
                testProducts.get(14)  // iPad Pro
        );

        for (int round = 0; round < 3; round++)
            for (Product p : favorites) productService.getProductById(p.getId());

        for (int i = 0; i < 7; i++) productService.getProductById(getRandomProduct().getId());

        productService.clearCurrentUser();
        exportProfile(eve, "READ-HEAVY");
    }

    private void executeScenario6_TechEnthusiast() {
        User frank = createUser("Frank Zhang", 29, "frank.zhang@email.com", "frank123");
        productService.setCurrentUser(frank);

        productService.getAllProducts();

        List<Product> luxury = testProducts.subList(14, 20); // products 15-20
        for (int round = 0; round < 3; round++)
            for (Product p : luxury) productService.getProductById(p.getId());

        Product p = createProduct("Frank's Custom Gaming PC", 3999.99);
        productService.addProduct(p);
        testProducts.add(p);

        productService.clearCurrentUser();
        exportProfile(frank, "EXPENSIVE-SEEKER");
    }

    private void executeScenario7_BudgetBuyer() {
        User grace = createUser("Grace Lee", 22, "grace.lee@email.com", "grace123");
        productService.setCurrentUser(grace);

        for (int i = 0; i < 5; i++) productService.getAllProducts();

        List<Product> budget = testProducts.subList(0, 5); // products 1-5
        for (int round = 0; round < 3; round++)
            for (Product p : budget) productService.getProductById(p.getId());

        Product p1 = createProduct("Grace's Notebook", 5.99);
        Product p2 = createProduct("Grace's Pen Set", 12.99);
        productService.addProduct(p1);
        productService.addProduct(p2);
        testProducts.add(p1);
        testProducts.add(p2);

        productService.clearCurrentUser();
        exportProfile(grace, "READ-HEAVY");
    }

    private void executeScenario8_InventoryClerk() {
        User henry = createUser("Henry Brown", 38, "henry.brown@email.com", "henry123");
        productService.setCurrentUser(henry);

        productService.getAllProducts();

        for (int i = 0; i < 12; i++) {
            Product p = createProduct("Henry's Stock Item " + (i + 1), random.nextDouble() * 500 + 10);
            productService.addProduct(p);
            testProducts.add(p);
        }

        for (int i = 0; i < 3; i++) productService.getProductById(getRandomProduct().getId());

        for (int i = 0; i < 6; i++) {
            Product oldProduct = testProducts.get(random.nextInt(testProducts.size()));
            Product updated = createProduct("Henry's Updated Stock " + (i + 1), random.nextDouble() * 400 + 20);
            updated.setId(oldProduct.getId());
            productService.updateProduct(updated.getId(), updated);
        }

        productService.clearCurrentUser();
        exportProfile(henry, "WRITE-HEAVY");
    }

    private void executeScenario9_ComparisonShopper() {
        User ivy = createUser("Ivy Chen", 27, "ivy.chen@email.com", "ivy123");
        productService.setCurrentUser(ivy);

        productService.getAllProducts();

        List<Product> luxuryMid = testProducts.subList(13, 19); // products 14-19
        for (int round = 0; round < 3; round++)
            for (Product p : luxuryMid) productService.getProductById(p.getId());

        List<Product> midRange = testProducts.subList(5, 10); // products 6-10
        for (Product p : midRange) productService.getProductById(p.getId());

        Product p = createProduct("Ivy's Mid-Range Laptop", 799.99);
        productService.addProduct(p);
        testProducts.add(p);

        productService.clearCurrentUser();
        exportProfile(ivy, "EXPENSIVE-SEEKER");
    }

    private void executeScenario10_DataAnalyst() {
        User jack = createUser("Jack Miller", 33, "jack.miller@email.com", "jack123");
        productService.setCurrentUser(jack);

        for (int i = 0; i < 10; i++) productService.getAllProducts();

        for (Product p : testProducts) productService.getProductById(p.getId());

        productService.clearCurrentUser();
        exportProfile(jack, "READ-HEAVY");
    }
}

package com.example.logging_and_observability.cli;
import com.example.logging_and_observability.execption.UserAlreadyExistsException;
import com.example.logging_and_observability.execption.UserNotFoundException;
import com.example.logging_and_observability.model.Product;
import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.profiling.model.ExpensiveProductSeekerProfile;
import com.example.logging_and_observability.profiling.model.ReadHeavyProfile;
import com.example.logging_and_observability.profiling.model.UserOperationType;
import com.example.logging_and_observability.profiling.model.WriteHeavyProfile;
import com.example.logging_and_observability.profiling.service.UserProfileService;
import com.example.logging_and_observability.service.ProductService;
import com.example.logging_and_observability.service.UserService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class ProductCLI implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ProductCLI.class);

    private final ProductService productService;

    private final UserService userService;

    private final UserProfileService userProfileService;

    private final Scanner scanner = new Scanner(System.in);

    private User currentUser;

    @Override
    public void run(String... args) throws Exception {
        // Check if we should skip CLI (for scenarios)
        for (String arg : args) {
            if ("--run-scenarios".equals(arg) || "--scenarios".equals(arg)) {
                logger.info("Skipping ProductCLI - Running scenarios instead");
                return;// Exit early, let ScenarioRunner take over

            }
        }
        logger.info("Starting Product Management CLI with User Authentication & Profiling");
        authenticateUser();
        productService.setCurrentUser(currentUser);
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getUserChoice();
            try {
                switch (choice) {
                    case 1 ->
                        displayAllProducts();
                    case 2 ->
                        fetchProductById();
                    case 3 ->
                        addNewProduct();
                    case 4 ->
                        deleteProduct();
                    case 5 ->
                        updateProduct();
                    case 6 ->
                        searchExpensiveProducts();
                    case 7 ->
                        displayUserProfile();
                    case 8 ->
                        exportUserProfile();
                    case 9 ->
                        displayAllUserProfiles();
                    case 0 ->
                        {
                            logger.info("User {} logged out", currentUser.getName());
                            running = false;
                        }
                    default ->
                        System.out.println("❌ Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                logger.error("Error in CLI loop", e);
                System.out.println("❌ Error: " + e.getMessage());
            }
        } 
        try {
            userProfileService.exportProfileToJson(currentUser);
            System.out.println("✅ Your profile has been saved!");
        } catch (IOException e) {
            logger.error("Failed to export user profile", e);
        }
        productService.clearCurrentUser();
        System.out.println(("\n👋 Goodbye, " + currentUser.getName()) + "!");
        logger.info("CLI application terminated");
    }

    // ================= AUTHENTICATION =================
    private void authenticateUser() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  PRODUCT MANAGEMENT SYSTEM             ║");
        System.out.println("║  WITH USER AUTHENTICATION & PROFILING  ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        System.out.println("1. Login (existing user)");
        System.out.println("2. Register (new user)");
        System.out.print("\nYour choice: ");
        int choice = getUserChoice();
        if (choice == 1) {
            loginUser();
        } else if (choice == 2) {
            registerNewUser();
        } else {
            System.out.println("Invalid choice. Defaulting to registration...");
            registerNewUser();
        }
    }

    private void loginUser() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║              LOGIN                     ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        try {
            currentUser = userService.authenticateUser(email, password);
            System.out.println(("\n✅ Welcome back, " + currentUser.getName()) + "!");
            logger.info("User logged in: {} ({})", currentUser.getName(), currentUser.getEmail());
        } catch (UserNotFoundException e) {
            System.out.println("\n❌ Login failed: " + e.getMessage());
            System.out.println("Let's register you as a new user...\n");
            registerNewUser();
        }
    }

    private void registerNewUser() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           REGISTRATION                 ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your age: ");
        int age = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your password (min 6 characters): ");
        String password = scanner.nextLine();
        try {
            User newUser = new User(name, age, email, password);
            currentUser = userService.registerUser(newUser);
            System.out.println(("\n✅ Registration successful! Welcome, " + currentUser.getName()) + "!");
            System.out.println("📊 Your activities will be profiled for personalized experience.");
            logger.info("User registered: {} ({})", name, email);
        } catch (UserAlreadyExistsException e) {
            System.out.println("\n❌ Registration failed: " + e.getMessage());
            System.out.println("Try logging in instead...\n");
            loginUser();
        }
    }

    // ================= MENU =================
    private void displayMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         PRODUCT MANAGEMENT MENU        ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 1. Display all products                ║");
        System.out.println("║ 2. Fetch product by ID                 ║");
        System.out.println("║ 3. Add new product                     ║");
        System.out.println("║ 4. Delete product                      ║");
        System.out.println("║ 5. Update product                      ║");
        System.out.println("║ 6. Search expensive products (>€100)   ║");
        System.out.println("║ ────────────────────────────────────   ║");
        System.out.println("║ 7. View my profile                     ║");
        System.out.println("║ 8. Export my profile (JSON)            ║");
        System.out.println("║ 9. View all user profiles              ║");
        System.out.println("║ 0. Exit                                ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.print(("\n👤 " + currentUser.getName()) + " > ");
    }

    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            logger.warn("Invalid menu input");
            return -1;
        }
    }

    // ================= PRODUCTS =================
    private void displayAllProducts() {
        logger.info("CLI: User {} requested all products", currentUser.getName());
        List<Product> products = productService.getAllProducts();
        System.out.println("\n📦 ALL PRODUCTS");
        System.out.println("══════════════════════════════════════════");
        if (products.isEmpty()) {
            System.out.println("No products available.");
        } else {
            for (Product p : products) {
                String priceIndicator = (p.getPrice() >= 100.0) ? " 💎" : "";
                System.out.printf("ID: %s | Name: %s | Price: €%.2f%s | Exp: %s%n", p.getId(), p.getName(), p.getPrice(), priceIndicator, p.getExpirationDate());
            }
        }
    }

    private void fetchProductById() {
        logger.info("CLI: User {} fetch product by ID", currentUser.getName());
        System.out.print("Enter product ID: ");
        String id = scanner.nextLine();
        Product product = productService.getProductById(id);
        System.out.println("\n✅ Product Found");
        System.out.println("═══════════════════════════════");
        System.out.println("ID: " + product.getId());
        System.out.println("Name: " + product.getName());
        System.out.println("Price: €" + product.getPrice());
        System.out.println("Expiration Date: " + product.getExpirationDate());
        if (product.getPrice() >= 100.0) {
            System.out.println("\n💎 This is an EXPENSIVE product!");
            System.out.println("   (Counted as 'expensive product search')");
        }
    }

    private void addNewProduct() {
        logger.info("CLI: User {} add new product", currentUser.getName());
        try {
            System.out.print("Enter product name: ");
            String name = scanner.nextLine();
            System.out.print("Enter product price (€): ");
            Double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter expiration date (yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ISO_LOCAL_DATE);
            // Ne pas fournir d'ID, MongoDB va le générer automatiquement
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setExpirationDate(date);
            productService.addProduct(product);
            System.out.println("✅ Product added successfully!");
            if (price >= 100.0) {
                System.out.println("💎 You added an expensive product!");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format (yyyy-MM-dd)");
        }
    }

    private void deleteProduct() {
        logger.info("CLI: User {} delete product", currentUser.getName());
        System.out.print("Enter product ID to delete: ");
        String id = scanner.nextLine();
        productService.deleteProduct(id);
        System.out.println("✅ Product deleted successfully!");
    }

    private void updateProduct() {
        logger.info("CLI: User {} update product", currentUser.getName());
        try {
            System.out.print("Enter product ID: ");
            String id = scanner.nextLine();
            System.out.print("Enter new product name: ");
            String name = scanner.nextLine();
            System.out.print("Enter new product price (€): ");
            Double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter new expiration date (yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ISO_LOCAL_DATE);
            Product updated = new Product(name, price, date);
            productService.updateProduct(id, updated);
            System.out.println("✅ Product updated successfully!");
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format");
        }
    }

    private void searchExpensiveProducts() {
        logger.info("CLI: User {} searching for expensive products", currentUser.getName());
        System.out.println("\n💎 EXPENSIVE PRODUCTS (Price ≥ €100)");
        System.out.println("══════════════════════════════════════════");
        List<Product> allProducts = productService.getAllProducts();
        List<Product> expensiveProducts = allProducts.stream().filter(p -> p.getPrice() >= 100.0).sorted((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice())).toList();
        if (expensiveProducts.isEmpty()) {
            System.out.println("No expensive products found.");
        } else {
            System.out.println(("Found " + expensiveProducts.size()) + " expensive products:\n");
            for (Product p : expensiveProducts) {
                System.out.printf("💎 ID: %s | %s | €%.2f | Exp: %s%n", p.getId(), p.getName(), p.getPrice(), p.getExpirationDate());
                userProfileService.logOperation(currentUser, "searchExpensiveProduct", UserOperationType.SEARCH_EXPENSIVE, p.getId(), p.getName(), p.getPrice());
            }
            System.out.println("\n✅ All expensive products have been logged to your profile!");
        }
    }

    private void displayUserProfile() {
        logger.info("CLI: User {} viewing their profile", currentUser.getName());
        userProfileService.getUserProfile(currentUser).ifPresentOrElse(profile -> {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║          YOUR USER PROFILE             ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("\n👤 User Information:");
            System.out.println("   Name: " + profile.getUserName());
            System.out.println("   Email: " + profile.getUserEmail());
            System.out.println("   Age: " + profile.getUserAge());
            System.out.println("\n📊 Profile Statistics:");
            System.out.println("   Profile Type: " + profile.getProfileType());
            System.out.println("   Description: " + profile.getProfileDescription());
            System.out.println("   Total Operations: " + profile.getTotalOperations());
            System.out.println("   Profile Created: " + profile.getProfileCreatedAt());
            System.out.println("   Last Activity: " + profile.getLastActivityAt());
            if (profile instanceof ReadHeavyProfile readProfile) {
                System.out.println("\n📖 READ OPERATIONS DETAILS:");
                System.out.println("   Total Reads: " + readProfile.getTotalReadOperations());
                System.out.println("   Total Writes: " + readProfile.getTotalWriteOperations());
                System.out.println("   Read Percentage: " + String.format("%.1f%%", readProfile.getReadPercentage()));
                System.out.println("   Get All Products: " + readProfile.getGetAllProductsCount());
                System.out.println("   Get Product By ID: " + readProfile.getGetProductByIdCount());
            } else if (profile instanceof WriteHeavyProfile writeProfile) {
                System.out.println("\n✏️  WRITE OPERATIONS DETAILS:");
                System.out.println("   Total Writes: " + writeProfile.getTotalWriteOperations());
                System.out.println("   Total Reads: " + writeProfile.getTotalReadOperations());
                System.out.println("   Write Percentage: " + String.format("%.1f%%", writeProfile.getWritePercentage()));
                System.out.println("   Products Added: " + writeProfile.getAddProductCount());
                System.out.println("   Products Updated: " + writeProfile.getUpdateProductCount());
                System.out.println("   Products Deleted: " + writeProfile.getDeleteProductCount());
            } else if (profile instanceof ExpensiveProductSeekerProfile expProfile) {
                System.out.println("\n💎 EXPENSIVE PRODUCT SEEKER DETAILS:");
                System.out.println("   Expensive Product Views: " + expProfile.getExpensiveProductViews());
                System.out.println("   Total Product Views: " + expProfile.getTotalProductViews());
                System.out.println("   Expensive View %: " + String.format("%.1f%%", expProfile.getExpensiveProductViewPercentage()));
                if (expProfile.getHighestPriceViewed() != null) {
                    System.out.println("   Highest Price Viewed: €" + String.format("%.2f", expProfile.getHighestPriceViewed()));
                }
                if (expProfile.getAveragePriceViewed() != null) {
                    System.out.println("   Average Price Viewed: €" + String.format("%.2f", expProfile.getAveragePriceViewed()));
                }
            }
        }, () -> System.out.println("\n⚠️  No profile data yet. Perform some operations!"));
    }

    private void exportUserProfile() {
        logger.info("CLI: User {} exporting their profile", currentUser.getName());
        try {
            userProfileService.exportProfileToJson(currentUser);
            System.out.println("\n✅ Profile exported to JSON successfully!");
            System.out.println(("📁 Location: user-profiles/" + currentUser.getEmail().replaceAll("[^a-zA-Z0-9]", "_")) + "_*_profile.json");
        } catch (IOException e) {
            System.out.println("\n❌ Failed to export profile: " + e.getMessage());
            logger.error("Profile export failed", e);
        }
    }

    private void displayAllUserProfiles() {
        logger.info("CLI: Displaying all user profiles summary");
        String summary = userProfileService.generateSummaryReport();
        System.out.println(summary);
    }
}
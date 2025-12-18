package com.example.logging_and_observability.service;

import com.example.logging_and_observability.execption.ProductAlreadyExistsException;
import com.example.logging_and_observability.execption.ProductNotFoundException;
import com.example.logging_and_observability.model.Product;
import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.repository.ProductRepository;
import com.example.logging_and_observability.profiling.service.UserProfileService;
import com.example.logging_and_observability.profiling.model.UserOperationType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final UserProfileService userProfileService; // NEW: Profiling service

    // Thread-local to store current user context
    private static final ThreadLocal<User> currentUserContext = new ThreadLocal<>();

    /**
     * Set the current user for the thread (call this before operations)
     */
    public void setCurrentUser(User user) {
        currentUserContext.set(user);
        logger.debug("Current user set: {}", user.getName());
    }

    /**
     * Get the current user from thread-local
     */
    public User getCurrentUser() {
        return currentUserContext.get();
    }

    /**
     * Clear the current user (call after operations or in finally block)
     */
    public void clearCurrentUser() {
        currentUserContext.remove();
    }

    /**
     * Get all products from the repository
     */
    public List<Product> getAllProducts() {
        logger.info("Fetching all products from repository");
        long startTime = System.currentTimeMillis();

        try {
            // PROFILING: Log READ operation
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "getAllProducts",
                        UserOperationType.READ,
                        null, null, null
                );
            }

            List<Product> products = productRepository.findAll();
            long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully retrieved {} products in {}ms", products.size(), duration);
            logger.debug("Products: {}", products);

            return products;
        } catch (Exception e) {
            logger.error("Error occurred while fetching all products", e);
            throw e;
        }
    }

    /**
     * Get a product by its ID
     */
    public Product getProductById(Long id) {
        logger.info("Attempting to fetch product with ID: {}", id);

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Product with ID '{}' not found", id);
                        return new ProductNotFoundException(id);
                    });

            // PROFILING: Log READ operation with product details
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "getProductById",
                        UserOperationType.READ,
                        product.getId(),
                        product.getName(),
                        product.getPrice()
                );

                // Check if expensive product
                if (product.getPrice() >= 100.0) {
                    userProfileService.logOperation(
                            currentUser,
                            "viewExpensiveProduct",
                            UserOperationType.SEARCH_EXPENSIVE,
                            product.getId(),
                            product.getName(),
                            product.getPrice()
                    );
                }
            }

            logger.info("Successfully retrieved product: {}", product.getName());
            logger.debug("Product details: {}", product);

            return product;
        } catch (ProductNotFoundException e) {
            logger.error("Product not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while fetching product with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Add a new product to the repository
     */
    public Product addProduct(Product product) {
        logger.info("Attempting to add new product: {}", product.getName());
        logger.debug("Product details: {}", product);

        try {
            // Check if product with same ID already exists
            if (product.getId() != null && productRepository.existsById(product.getId())) {
                logger.warn("Product with ID '{}' already exists", product.getId());
                throw new ProductAlreadyExistsException(product.getId());
            }

            Product savedProduct = productRepository.save(product);

            // PROFILING: Log WRITE operation
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "addProduct",
                        UserOperationType.WRITE,
                        savedProduct.getId(),
                        savedProduct.getName(),
                        savedProduct.getPrice()
                );
            }

            logger.info("Successfully added product with ID: {}", savedProduct.getId());
            logger.debug("Saved product: {}", savedProduct);

            return savedProduct;
        } catch (ProductAlreadyExistsException e) {
            logger.error("Failed to add product: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while adding product: {}", product.getName(), e);
            throw e;
        }
    }

    /**
     * Update an existing product
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        logger.info("Attempting to update product with ID: {}", id);
        logger.debug("Updated product data: {}", updatedProduct);

        try {
            // Check if product exists
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Cannot update - Product with ID '{}' not found", id);
                        return new ProductNotFoundException(id);
                    });

            logger.debug("Current product state: {}", existingProduct);

            // Update fields
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setExpirationDate(updatedProduct.getExpirationDate());

            Product savedProduct = productRepository.save(existingProduct);

            // PROFILING: Log WRITE operation
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "updateProduct",
                        UserOperationType.WRITE,
                        savedProduct.getId(),
                        savedProduct.getName(),
                        savedProduct.getPrice()
                );
            }

            logger.info("Successfully updated product with ID: {}", id);
            logger.debug("Updated product: {}", savedProduct);

            return savedProduct;
        } catch (ProductNotFoundException e) {
            logger.error("Failed to update product: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while updating product with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Delete a product by its ID
     */
    public void deleteProduct(Long id) {
        logger.info("Attempting to delete product with ID: {}", id);

        try {
            // Check if product exists
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Cannot delete - Product with ID '{}' not found", id);
                        return new ProductNotFoundException(id);
                    });

            logger.debug("Deleting product: {}", product);

            productRepository.deleteById(id);

            // PROFILING: Log WRITE operation
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "deleteProduct",
                        UserOperationType.WRITE,
                        id,
                        product.getName(),
                        product.getPrice()
                );
            }

            logger.info("Successfully deleted product with ID: {}", id);
        } catch (ProductNotFoundException e) {
            logger.error("Failed to delete product: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while deleting product with ID: {}", id, e);
            throw e;
        }
    }
}

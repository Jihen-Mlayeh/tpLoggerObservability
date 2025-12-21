package com.example.logging_and_observability.service;

import com.example.logging_and_observability.execption.ProductAlreadyExistsException;
import com.example.logging_and_observability.execption.ProductNotFoundException;
import com.example.logging_and_observability.model.Product;
import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.repository.ProductRepository;
import com.example.logging_and_observability.profiling.model.UserOperationType;
import com.example.logging_and_observability.profiling.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProductService with enhanced logging for Question 5
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final UserProfileService userProfileService;

    private static final ThreadLocal<User> currentUserContext = new ThreadLocal<>();

    public void setCurrentUser(User user) {
        currentUserContext.set(user);
        logger.info("User context set for: {} ({})", user.getName(), user.getEmail());
    }

    public User getCurrentUser() {
        return currentUserContext.get();
    }

    public void clearCurrentUser() {
        User user = currentUserContext.get();
        if (user != null) {
            logger.info("Clearing user context for: {}", user.getEmail());
        }
        currentUserContext.remove();
    }

    public List<Product> getAllProducts() {
        User currentUser = getCurrentUser();

        logger.info(
                "Operation: getAllProducts | User: {} | Email: {} | Action: READ",
                currentUser != null ? currentUser.getName() : "Unknown",
                currentUser != null ? currentUser.getEmail() : "unknown@email.com"
        );

        List<Product> products = productRepository.findAll();

        logger.info(
                "Retrieved {} products | User: {} | Operation: READ | Status: SUCCESS",
                products.size(),
                currentUser != null ? currentUser.getEmail() : "unknown@email.com"
        );

        if (currentUser != null) {
            userProfileService.logOperation(
                    currentUser,
                    "getAllProducts",
                    UserOperationType.READ,
                    null,
                    null,
                    null
            );
        }

        return products;
    }

    public Product getProductById(String id) {
        User currentUser = getCurrentUser();

        logger.info(
                "Operation: getProductById | User: {} | Email: {} | ProductID: {} | Action: READ",
                currentUser != null ? currentUser.getName() : "Unknown",
                currentUser != null ? currentUser.getEmail() : "unknown@email.com",
                id
        );

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(
                            "Product not found | ID: {} | User: {} | Status: ERROR",
                            id,
                            currentUser != null ? currentUser.getEmail() : "unknown"
                    );
                    return new ProductNotFoundException(id);
                });

        logger.info(
                "Product found | ID: {} | Name: {} | Price: €{} | User: {} | Operation: READ | Status: SUCCESS",
                product.getId(),
                product.getName(),
                product.getPrice(),
                currentUser != null ? currentUser.getEmail() : "unknown@email.com"
        );

        if (currentUser != null) {
            userProfileService.logOperation(
                    currentUser,
                    "getProductById",
                    UserOperationType.READ,
                    product.getId(),
                    product.getName(),
                    product.getPrice()
            );

            if (product.getPrice() >= 100.0) {
                logger.info(
                        "Expensive product view | ID: {} | Name: {} | Price: €{} | User: {} | Operation: SEARCH_EXPENSIVE",
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        currentUser.getEmail()
                );

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

        return product;
    }

    public Product addProduct(Product product) {
        User currentUser = getCurrentUser();

        logger.info(
                "Operation: addProduct | User: {} | Email: {} | ProductName: {} | Price: €{} | Action: WRITE",
                currentUser != null ? currentUser.getName() : "Unknown",
                currentUser != null ? currentUser.getEmail() : "unknown@email.com",
                product.getName(),
                product.getPrice()
        );

        if (product.getId() != null && productRepository.existsById(product.getId())) {
            logger.error(
                    "Product already exists | ID: {} | User: {} | Status: ERROR",
                    product.getId(),
                    currentUser != null ? currentUser.getEmail() : "unknown"
            );
            throw new ProductAlreadyExistsException(product.getId());
        }

        Product saved = productRepository.save(product);

        logger.info(
                "Product added | ID: {} | Name: {} | Price: €{} | User: {} | Operation: WRITE | Status: SUCCESS",
                saved.getId(),
                saved.getName(),
                saved.getPrice(),
                currentUser != null ? currentUser.getEmail() : "unknown@email.com"
        );

        if (currentUser != null) {
            userProfileService.logOperation(
                    currentUser,
                    "addProduct",
                    UserOperationType.WRITE,
                    saved.getId(),
                    saved.getName(),
                    saved.getPrice()
            );
        }

        return saved;
    }

    public Product updateProduct(String id, Product updatedProduct) {
        User currentUser = getCurrentUser();

        logger.info(
                "Operation: updateProduct | User: {} | Email: {} | ProductID: {} | Action: WRITE",
                currentUser != null ? currentUser.getName() : "Unknown",
                currentUser != null ? currentUser.getEmail() : "unknown@email.com",
                id
        );

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(
                            "Product not found for update | ID: {} | User: {} | Status: ERROR",
                            id,
                            currentUser != null ? currentUser.getEmail() : "unknown"
                    );
                    return new ProductNotFoundException(id);
                });

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setExpirationDate(updatedProduct.getExpirationDate());

        Product saved = productRepository.save(existingProduct);

        logger.info(
                "Product updated | ID: {} | Name: {} | Price: €{} | User: {} | Operation: WRITE | Status: SUCCESS",
                saved.getId(),
                saved.getName(),
                saved.getPrice(),
                currentUser != null ? currentUser.getEmail() : "unknown@email.com"
        );

        if (currentUser != null) {
            userProfileService.logOperation(
                    currentUser,
                    "updateProduct",
                    UserOperationType.WRITE,
                    saved.getId(),
                    saved.getName(),
                    saved.getPrice()
            );
        }

        return saved;
    }

    public void deleteProduct(String id) {
        User currentUser = getCurrentUser();

        logger.info(
                "Operation: deleteProduct | User: {} | Email: {} | ProductID: {} | Action: WRITE",
                currentUser != null ? currentUser.getName() : "Unknown",
                currentUser != null ? currentUser.getEmail() : "unknown@email.com",
                id
        );

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(
                            "Product not found for deletion | ID: {} | User: {} | Status: ERROR",
                            id,
                            currentUser != null ? currentUser.getEmail() : "unknown"
                    );
                    return new ProductNotFoundException(id);
                });

        productRepository.deleteById(id);

        logger.info(
                "Product deleted | ID: {} | Name: {} | User: {} | Operation: WRITE | Status: SUCCESS",
                id,
                product.getName(),
                currentUser != null ? currentUser.getEmail() : "unknown@email.com"
        );

        if (currentUser != null) {
            userProfileService.logOperation(
                    currentUser,
                    "deleteProduct",
                    UserOperationType.WRITE,
                    product.getId(),
                    product.getName(),
                    product.getPrice()
            );
        }
    }
}

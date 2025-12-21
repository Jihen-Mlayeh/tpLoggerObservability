package com.example.logging_and_observability.controller;
import com.example.logging_and_observability.execption.ProductAlreadyExistsException;
import com.example.logging_and_observability.execption.ProductNotFoundException;
import com.example.logging_and_observability.model.Product;
import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    /**
     * Helper method to set user context from headers
     */
    private void setUserContext(String userName, String userEmail) {
        User user = new User();
        user.setName(userName);
        user.setEmail(userEmail);
        productService.setCurrentUser(user);
    }

    /**
     * GET /api/products - Get all products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(@RequestHeader(value = "X-User-Name", defaultValue = "Unknown")
    String userName, @RequestHeader(value = "X-User-Email", defaultValue = "unknown@email.com")
    String userEmail) {
        setUserContext(userName, userEmail);// ✅ SET USER

        logger.info("REST API: GET /api/products - Fetching all products");
        try {
            List<Product> products = productService.getAllProducts();
            logger.info("REST API: Returning {} products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("REST API: Error fetching all products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            productService.clearCurrentUser();// ✅ CLEAN UP

        }
    }

    /**
     * GET /api/products/{id} - Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable
    String id, @RequestHeader(value = "X-User-Name", defaultValue = "Unknown")
    String userName, @RequestHeader(value = "X-User-Email", defaultValue = "unknown@email.com")
    String userEmail) {
        setUserContext(userName, userEmail);// ✅ SET USER

        logger.info("REST API: GET /api/products/{} - Fetching product by ID", id);
        try {
            Product product = productService.getProductById(id);
            logger.info("REST API: Product found with ID: {}", id);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            logger.warn("REST API: Product not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("REST API: Error fetching product with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        } finally {
            productService.clearCurrentUser();// ✅ CLEAN UP

        }
    }

    /**
     * POST /api/products - Add a new product
     */
    @PostMapping
    public ResponseEntity<?> addProduct(@Valid
    @RequestBody
    Product product, @RequestHeader(value = "X-User-Name", defaultValue = "Unknown")
    String userName, @RequestHeader(value = "X-User-Email", defaultValue = "unknown@email.com")
    String userEmail) {
        setUserContext(userName, userEmail);// ✅ SET USER

        logger.info("REST API: POST /api/products - Adding new product: {}", product.getName());
        try {
            Product savedProduct = productService.addProduct(product);
            logger.info("REST API: Product added successfully with ID: {}", savedProduct.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (ProductAlreadyExistsException e) {
            logger.warn("REST API: Product already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("REST API: Error adding product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        } finally {
            productService.clearCurrentUser();// ✅ CLEAN UP

        }
    }

    /**
     * PUT /api/products/{id} - Update an existing product
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable
    String id, @Valid
    @RequestBody
    Product product, @RequestHeader(value = "X-User-Name", defaultValue = "Unknown")
    String userName, @RequestHeader(value = "X-User-Email", defaultValue = "unknown@email.com")
    String userEmail) {
        setUserContext(userName, userEmail);// ✅ SET USER

        logger.info("REST API: PUT /api/products/{} - Updating product", id);
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            logger.info("REST API: Product updated successfully with ID: {}", id);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductNotFoundException e) {
            logger.warn("REST API: Cannot update - Product not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("REST API: Error updating product with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        } finally {
            productService.clearCurrentUser();// ✅ CLEAN UP

        }
    }

    /**
     * DELETE /api/products/{id} - Delete a product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable
    String id, @RequestHeader(value = "X-User-Name", defaultValue = "Unknown")
    String userName, @RequestHeader(value = "X-User-Email", defaultValue = "unknown@email.com")
    String userEmail) {
        setUserContext(userName, userEmail);// ✅ SET USER

        logger.info("REST API: DELETE /api/products/{} - Deleting product", id);
        try {
            productService.deleteProduct(id);
            logger.info("REST API: Product deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ProductNotFoundException e) {
            logger.warn("REST API: Cannot delete - Product not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("REST API: Error deleting product with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            productService.clearCurrentUser();// ✅ CLEAN UP

        }
    }
}
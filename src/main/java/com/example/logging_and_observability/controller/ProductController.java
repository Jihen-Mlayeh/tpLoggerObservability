package com.example.logging_and_observability.controller;

import com.example.logging_and_observability.execption.ProductAlreadyExistsException;
import com.example.logging_and_observability.execption.ProductNotFoundException;
import com.example.logging_and_observability.model.Product;
import com.example.logging_and_observability.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    /**
     * GET /api/products - Get all products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("REST API: GET /api/products - Fetching all products");

        try {
            List<Product> products = productService.getAllProducts();
            logger.info("REST API: Returning {} products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("REST API: Error fetching all products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/products/{id} - Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        logger.info("REST API: GET /api/products/{} - Fetching product by ID", id);

        try {
            Product product = productService.getProductById(id);
            logger.info("REST API: Product found with ID: {}", id);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            logger.warn("REST API: Product not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("REST API: Error fetching product with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    /**
     * POST /api/products - Add a new product
     */
    @PostMapping
    public ResponseEntity<?> addProduct(@Valid @RequestBody Product product) {
        logger.info("REST API: POST /api/products - Adding new product: {}", product.getName());

        try {
            Product savedProduct = productService.addProduct(product);
            logger.info("REST API: Product added successfully with ID: {}", savedProduct.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (ProductAlreadyExistsException e) {
            logger.warn("REST API: Product already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("REST API: Error adding product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    /**
     * PUT /api/products/{id} - Update an existing product
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody Product product) {
        logger.info("REST API: PUT /api/products/{} - Updating product", id);

        try {
            Product updatedProduct = productService.updateProduct(id, product);
            logger.info("REST API: Product updated successfully with ID: {}", id);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductNotFoundException e) {
            logger.warn("REST API: Cannot update - Product not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("REST API: Error updating product with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    /**
     * DELETE /api/products/{id} - Delete a product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        logger.info("REST API: DELETE /api/products/{} - Deleting product", id);

        try {
            productService.deleteProduct(id);
            logger.info("REST API: Product deleted successfully with ID: {}", id);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (ProductNotFoundException e) {
            logger.warn("REST API: Cannot delete - Product not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("REST API: Error deleting product with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }
}
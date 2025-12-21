package com.example.logging_and_observability.repository;


import com.example.logging_and_observability.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Find products by name (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find products expiring before a certain date
    List<Product> findByExpirationDateBefore(LocalDate date);

    // Find products within a price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Check if product exists by ID
    boolean existsById(long id);
}
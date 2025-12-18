package com.example.logging_and_observability.profiling.model;

/**
 * Types of operations performed by users
 */
public enum UserOperationType {
    READ,           // getAllProducts, getProductById
    WRITE,          // addProduct, updateProduct, deleteProduct
    SEARCH_EXPENSIVE // Searching for expensive products
}

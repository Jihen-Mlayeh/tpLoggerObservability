package com.example.logging_and_observability.profiling.model;
/**
 * Types of operations performed by users
 */
// Searching for expensive products
public enum UserOperationType {

    READ,
    // getAllProducts, getProductById
    WRITE,
    // addProduct, updateProduct, deleteProduct
    SEARCH_EXPENSIVE;}
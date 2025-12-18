package com.example.logging_and_observability.execption;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product with ID '" + id + "' not found");
    }

    public ProductNotFoundException(Long id, Throwable cause) {
        super("Product with ID '" + id + "' not found", cause);
    }
}

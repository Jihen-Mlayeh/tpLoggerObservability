package com.example.logging_and_observability.execption;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String id) {
        super("Product with ID '" + id + "' already exists");
    }

    public ProductAlreadyExistsException(Long id, Throwable cause) {
        super("Product with ID '" + id + "' already exists", cause);
    }
}
package com.example.logging_and_observability.execption;

/**
 * Exception thrown when trying to register a user that already exists
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("User with email '" + email + "' already exists");
    }

    public UserAlreadyExistsException(Long id) {
        super("User with ID '" + id + "' already exists");
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

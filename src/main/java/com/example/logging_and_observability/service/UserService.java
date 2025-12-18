package com.example.logging_and_observability.service;

import com.example.logging_and_observability.execption.UserAlreadyExistsException;
import com.example.logging_and_observability.execption.UserNotFoundException;
import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing users in MongoDB
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    /**
     * Register a new user
     */
    public User registerUser(User user) {
        logger.info("Attempting to register user: {}", user.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("User with email '{}' already exists", user.getEmail());
            throw new UserAlreadyExistsException(user.getEmail());
        }

        // Generate ID if not provided
        if (user.getId() == null) {
            user.setId(generateUserId());
        }

        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user: {} with ID: {}", savedUser.getEmail(), savedUser.getId());

        return savedUser;
    }

    /**
     * Authenticate user (login)
     */
    public User authenticateUser(String email, String password) {
        logger.info("Attempting to authenticate user: {}", email);

        User user = userRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed for user: {}", email);
                    return new UserNotFoundException("Invalid email or password");
                });

        logger.info("Successfully authenticated user: {}", email);
        return user;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UserNotFoundException(email);
                });
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new UserNotFoundException(id);
                });
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.info("Found {} users", users.size());
        return users;
    }

    /**
     * Update user
     */
    public User updateUser(Long id, User updatedUser) {
        logger.info("Updating user with ID: {}", id);

        User existingUser = getUserById(id);

        existingUser.setName(updatedUser.getName());
        existingUser.setAge(updatedUser.getAge());
        existingUser.setEmail(updatedUser.getEmail());

        // Only update password if provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        User savedUser = userRepository.save(existingUser);
        logger.info("Successfully updated user: {}", savedUser.getEmail());

        return savedUser;
    }

    /**
     * Delete user
     */
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);

        User user = getUserById(id);
        userRepository.deleteById(id);

        logger.info("Successfully deleted user: {}", user.getEmail());
    }

    /**
     * Check if user exists by email
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Generate unique user ID
     */
    private Long generateUserId() {
        // Simple ID generation - you can improve this
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return 1L;
        }
        return users.stream()
                .map(User::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}
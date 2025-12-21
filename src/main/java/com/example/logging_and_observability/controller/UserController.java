package com.example.logging_and_observability.controller;

import com.example.logging_and_observability.model.LoginRequest;
import com.example.logging_and_observability.model.LoginResponse;
import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * REGISTER
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        logger.info("Registering user: {}", user.getEmail());

        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * LOGIN (✔ CORRIGÉ)
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt: {}", loginRequest.getEmail());

        try {
            // ✅ VRAIE authentification (email + password)
            User user = userService.authenticateUser(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            LoginResponse response = new LoginResponse(
                    "mock-jwt-token-" + user.getId(),
                    user
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("Login failed for {}", loginRequest.getEmail());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }

    /**
     * GET ALL USERS
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET BY ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * GET BY EMAIL
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /**
     * DELETE
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}

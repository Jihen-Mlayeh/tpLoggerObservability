package com.example.logging_and_observability.repository;
import com.example.logging_and_observability.model.User;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
/**
 * Repository for User persistence in MongoDB
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    /**
     * Find user by email (unique)
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email and password (for authentication)
     */
    Optional<User> findByEmailAndPassword(String email, String password);

    /**
     * Delete user by email
     */
    void deleteByEmail(String email);
}
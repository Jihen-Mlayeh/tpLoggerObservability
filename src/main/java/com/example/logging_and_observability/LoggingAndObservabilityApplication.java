package com.example.logging_and_observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.logging_and_observability")
public class LoggingAndObservabilityApplication {


    private static final Logger logger = LoggerFactory.getLogger(LoggingAndObservabilityApplication.class);

    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("Starting Product Management Application");
        logger.info("========================================");

        SpringApplication.run(LoggingAndObservabilityApplication.class, args);

        logger.info("Application started successfully");
    }
}

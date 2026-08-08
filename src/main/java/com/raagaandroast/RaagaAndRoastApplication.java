package com.raagaandroast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * RaagaAndRoast - Portfolio-grade café ordering and management platform.
 * 
 * This Spring Boot application demonstrates advanced Java backend engineering
 * skills including:
 * - Clean Architecture with proper separation of concerns
 * - Spring Security with JWT authentication and role-based authorization
 * - Spring Data JPA with performance optimizations and relationship management
 * - Comprehensive validation and exception handling
 * - Production-ready configuration and monitoring
 * 
 * @author RaagaAndRoast Development Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableTransactionManagement
public class RaagaAndRoastApplication {

    /**
     * Main method to start the RaagaAndRoast application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RaagaAndRoastApplication.class, args);
    }
}
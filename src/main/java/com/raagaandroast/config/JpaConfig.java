package com.raagaandroast.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration for RaagaAndRoast application.
 * 
 * This configuration enables:
 * - JPA Auditing for automatic @CreatedDate and @LastModifiedDate
 * - JPA Repositories with custom base repository if needed
 * - Transaction Management for proper ACID compliance
 * 
 * Key Design Decisions:
 * - Auditing enabled for tracking entity lifecycle
 * - Custom repository base package for clean separation
 * - Transaction management for service layer boundaries
 * 
 * @author RaagaAndRoast Development Team
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableJpaRepositories(basePackages = "com.raagaandroast.*.repository")
@EnableTransactionManagement
public class JpaConfig {

    // Additional JPA configuration can be added here if needed
    // For example: custom naming strategies, connection pool settings, etc.
}
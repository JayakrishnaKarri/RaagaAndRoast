package com.raagaandroast.customer.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raagaandroast.customer.entity.Customer;

/**
 * Repository interface for Customer entity operations.
 * 
 * This repository provides data access methods for customer management,
 * including custom queries for business operations and performance
 * optimization.
 * 
 * Design Decisions:
 * - Extends JpaRepository for standard CRUD operations
 * - Custom queries using @Query for complex business logic
 * - JOIN FETCH for performance optimization (avoiding N+1 problems)
 * - Method naming follows Spring Data JPA conventions
 * - Pagination support for list operations
 * 
 * Performance Considerations:
 * - JOIN FETCH used to load related entities in single query
 * - Indexed fields used in WHERE clauses for optimal performance
 * - Pagination prevents memory issues with large datasets
 * 
 * Interview Points:
 * - Why JOIN FETCH? Prevents N+1 query problem
 * - Why Optional? Null safety and modern Java practices
 * - Why custom queries? Business logic that can't be expressed with method
 * names
 * - Why pagination? Performance and user experience
 * 
 * @author RaagaAndRoast Development Team
 */

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

        // ================================================================
        // Basic Finder Methods
        // ================================================================

        /**
         * Finds a customer by their associated user ID.
         * Uses JOIN FETCH to load the user relationship in a single query.
         * 
         * @param userId the user ID to search for
         * @return Optional containing the customer if found
         */
        @Query("SELECT c FROM Customer c JOIN FETCH c.user u WHERE u.id = :userId")
        Optional<Customer> findByUserId(@Param("userId") UUID userId);

        /**
         * Finds a customer by their associated username.
         * Useful for authentication and authorization scenarios.
         * 
         * @param username the username to search for
         * @return Optional containing the customer if found
         */
        @Query("SELECT c FROM Customer c JOIN FETCH c.user u WHERE u.username = :username")
        Optional<Customer> findByUsername(@Param("username") String username);

        /**
         * Finds a customer by their associated email.
         * Useful for customer lookup and communication.
         * 
         * @param email the email to search for
         * @return Optional containing the customer if found
         */
        @Query("SELECT c FROM Customer c JOIN FETCH c.user u WHERE u.email = :email")
        Optional<Customer> findByEmail(@Param("email") String email);

        /**
         * Finds a customer by phone number.
         * Useful for customer support and order coordination.
         * 
         * @param phoneNumber the phone number to search for
         * @return Optional containing the customer if found
         */
        Optional<Customer> findByPhoneNumber(String phoneNumber);

        // ================================================================
        // Search and Filter Methods
        // ================================================================

        /**
         * Searches customers by name (first name or last name).
         * Case-insensitive search with partial matching.
         * 
         * @param name     the name to search for
         * @param pageable pagination information
         * @return Page of customers matching the search criteria
         */
        @Query("SELECT c FROM Customer c WHERE " +
                        "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
                        "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
        Page<Customer> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

        /**
         * Finds customers by marketing consent status.
         * Useful for marketing campaigns and communication preferences.
         * 
         * @param marketingConsent the marketing consent status
         * @param pageable         pagination information
         * @return Page of customers with the specified marketing consent
         */
        Page<Customer> findByMarketingConsent(Boolean marketingConsent, Pageable pageable);

        /**
         * Finds customers born on a specific date.
         * Useful for birthday promotions and campaigns.
         * 
         * @param dateOfBirth the date of birth to search for
         * @return List of customers born on the specified date
         */
        List<Customer> findByDateOfBirth(LocalDate dateOfBirth);

        /**
         * Finds customers born in a specific month and day.
         * Useful for birthday promotions regardless of year.
         * 
         * @param month the birth month (1-12)
         * @param day   the birth day (1-31)
         * @return List of customers with birthdays on the specified month and day
         */
        @Query("SELECT c FROM Customer c WHERE " +
                        "MONTH(c.dateOfBirth) = :month AND DAY(c.dateOfBirth) = :day")
        List<Customer> findByBirthdayMonthAndDay(@Param("month") int month, @Param("day") int day);

        // ================================================================
        // Relationship-based Queries
        // ================================================================

        /**
         * Finds customers with addresses in a specific city.
         * Uses JOIN to filter customers based on their address information.
         * 
         * @param city     the city to search for
         * @param pageable pagination information
         * @return Page of customers with addresses in the specified city
         */
        @Query("SELECT DISTINCT c FROM Customer c JOIN c.addresses a WHERE " +
                        "LOWER(a.city) = LOWER(:city) AND a.isActive = true")
        Page<Customer> findByAddressCity(@Param("city") String city, Pageable pageable);

        /**
         * Finds customers with addresses in a specific state.
         * Uses JOIN to filter customers based on their address information.
         * 
         * @param state    the state to search for
         * @param pageable pagination information
         * @return Page of customers with addresses in the specified state
         */
        @Query("SELECT DISTINCT c FROM Customer c JOIN c.addresses a WHERE " +
                        "LOWER(a.state) = LOWER(:state) AND a.isActive = true")
        Page<Customer> findByAddressState(@Param("state") String state, Pageable pageable);

        /**
         * Finds customers without any addresses.
         * Useful for data quality checks and customer onboarding follow-up.
         * 
         * @param pageable pagination information
         * @return Page of customers without addresses
         */
        @Query("SELECT c FROM Customer c WHERE c.addresses IS EMPTY")
        Page<Customer> findCustomersWithoutAddresses(Pageable pageable);

        // ================================================================
        // Performance-Optimized Queries
        // ================================================================

        /**
         * Finds a customer with all related data loaded in a single query.
         * Uses JOIN FETCH to prevent N+1 problems when accessing relationships.
         * 
         * @param customerId the customer ID
         * @return Optional containing the customer with all relationships loaded
         */
        @Query("SELECT c FROM Customer c " +
                        "LEFT JOIN FETCH c.user u " +
                        "LEFT JOIN FETCH c.addresses a " +
                        "WHERE c.id = :customerId")
        Optional<Customer> findByIdWithAllRelationships(@Param("customerId") UUID customerId);

        /**
         * Finds customers with their user information loaded.
         * Optimized for scenarios where user data is always needed.
         * 
         * @param pageable pagination information
         * @return Page of customers with user information loaded
         */
        @Query("SELECT c FROM Customer c JOIN FETCH c.user u")
        Page<Customer> findAllWithUser(Pageable pageable);

        /**
         * Finds customers with their addresses loaded.
         * Optimized for scenarios where address data is always needed.
         * 
         * @param pageable pagination information
         * @return Page of customers with addresses loaded
         */
        @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.addresses a")
        Page<Customer> findAllWithAddresses(Pageable pageable);

        // ================================================================
        // Statistical and Reporting Queries
        // ================================================================

        /**
         * Counts customers by marketing consent status.
         * Useful for marketing analytics and reporting.
         * 
         * @param marketingConsent the marketing consent status
         * @return count of customers with the specified marketing consent
         */
        long countByMarketingConsent(Boolean marketingConsent);

        /**
         * Counts customers registered in a specific date range.
         * Useful for growth analytics and reporting.
         * 
         * @param startDate the start date (inclusive)
         * @param endDate   the end date (inclusive)
         * @return count of customers registered in the date range
         */
        @Query("SELECT COUNT(c) FROM Customer c WHERE " +
                        "c.createdAt >= :startDate AND c.createdAt <= :endDate")
        long countByRegistrationDateRange(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Finds customers registered in the last N days.
         * Useful for recent customer analysis and onboarding follow-up.
         * 
         * @param days     the number of days to look back
         * @param pageable pagination information
         * @return Page of recently registered customers
         */
        @Query("SELECT c FROM Customer c WHERE " +
                        "c.createdAt >= CURRENT_TIMESTAMP - :days DAY")
        Page<Customer> findRecentCustomers(@Param("days") int days, Pageable pageable);

        // ================================================================
        // Existence and Validation Queries
        // ================================================================

        /**
         * Checks if a customer exists for a given user ID.
         * Useful for validation and business logic.
         * 
         * @param userId the user ID to check
         * @return true if a customer exists for the user
         */
        boolean existsByUserId(UUID userId);

        /**
         * Checks if a customer exists with the given phone number.
         * Useful for duplicate prevention and validation.
         * 
         * @param phoneNumber the phone number to check
         * @return true if a customer exists with the phone number
         */
        boolean existsByPhoneNumber(String phoneNumber);

        /**
         * Checks if a customer exists with the given email (through user relationship).
         * Useful for duplicate prevention and validation.
         * 
         * @param email the email to check
         * @return true if a customer exists with the email
         */
        @Query("SELECT COUNT(c) > 0 FROM Customer c JOIN c.user u WHERE u.email = :email")
        boolean existsByEmail(@Param("email") String email);
}
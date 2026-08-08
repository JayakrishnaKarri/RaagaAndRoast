package com.raagaandroast.customer.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.raagaandroast.customer.entity.Address;
import com.raagaandroast.customer.entity.AddressType;

/**
 * Repository interface for Address entity operations.
 * 
 * This repository provides data access methods for address management,
 * including customer-specific queries and address validation operations.
 * 
 * Design Decisions:
 * - Extends JpaRepository for standard CRUD operations
 * - Customer-centric queries for address management
 * - Address type filtering for business logic
 * - Soft delete support through isActive flag
 * - Default address management operations
 * 
 * Performance Considerations:
 * - Indexed fields used in WHERE clauses
 * - Batch operations for multiple address updates
 * - Optimized queries for common access patterns
 * 
 * Interview Points:
 * - Why separate Address repository? Single Responsibility Principle
 * - Why customer-centric queries? Business domain alignment
 * - Why soft delete? Data preservation and audit trail
 * - Why default address logic? Business requirement for primary address
 * 
 * @author RaagaAndRoast Development Team
 */

public interface AddressRepository extends JpaRepository<Address, UUID> {

        // ================================================================
        // Performance-Optimized Basic Finders (N+1 Prevention)
        // ================================================================

        /**
         * Finds an address by ID with customer loaded in single query.
         * Prevents N+1 problem when accessing customer information.
         *
         * @param id the address ID
         * @return Optional containing the address with customer if found
         */
        @Query("SELECT a FROM Address a JOIN FETCH a.customer WHERE a.id = :id")
        Optional<Address> findByIdWithCustomer(@Param("id") UUID id);

        // ================================================================
        // Customer-based Queries
        // ================================================================

        /**
         * Finds all addresses for a specific customer.
         * Returns only active addresses by default.
         * 
         * @param customerId the customer ID
         * @return List of active addresses for the customer
         */
        @Query("SELECT a FROM Address a WHERE a.customer.id = :customerId AND a.isActive = true " +
                        "ORDER BY a.isDefault DESC, a.createdAt ASC")
        List<Address> findByCustomerId(@Param("customerId") UUID customerId);

        /**
         * Finds all addresses for a specific customer with pagination.
         * Includes both active and inactive addresses.
         * 
         * @param customerId the customer ID
         * @param pageable   pagination information
         * @return Page of addresses for the customer
         */
        Page<Address> findByCustomerId(UUID customerId, Pageable pageable);

        /**
         * Finds active addresses for a specific customer.
         * 
         * @param customerId the customer ID
         * @return List of active addresses for the customer
         */
        List<Address> findByCustomerIdAndIsActiveTrue(UUID customerId);

        /**
         * Finds addresses for a specific customer by type.
         * Returns only active addresses of the specified type.
         * 
         * @param customerId the customer ID
         * @param type       the address type
         * @return List of addresses matching the criteria
         */
        List<Address> findByCustomerIdAndTypeAndIsActiveTrue(UUID customerId, AddressType type);

        // ================================================================
        // Default Address Management
        // ================================================================

        /**
         * Finds the default address for a specific customer.
         * 
         * @param customerId the customer ID
         * @return Optional containing the default address if found
         */
        Optional<Address> findByCustomerIdAndIsDefaultTrueAndIsActiveTrue(UUID customerId);

        /**
         * Removes default flag from all addresses for a customer.
         * Used when setting a new default address.
         * 
         * @param customerId the customer ID
         * @return number of addresses updated
         */
        @Modifying
        @Transactional
        @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId")
        int removeDefaultFlagForCustomer(@Param("customerId") UUID customerId);

        /**
         * Sets a specific address as default for a customer.
         * Should be used after removing default flag from other addresses.
         * 
         * @param addressId  the address ID to set as default
         * @param customerId the customer ID (for security)
         * @return number of addresses updated (should be 1)
         */
        @Modifying
        @Transactional
        @Query("UPDATE Address a SET a.isDefault = true WHERE a.id = :addressId AND a.customer.id = :customerId")
        int setAsDefault(@Param("addressId") UUID addressId, @Param("customerId") UUID customerId);

        // ================================================================
        // Address Type Queries
        // ================================================================

        /**
         * Finds addresses by type across all customers.
         * Useful for analytics and reporting.
         * 
         * @param type     the address type
         * @param pageable pagination information
         * @return Page of addresses of the specified type
         */
        Page<Address> findByTypeAndIsActiveTrue(AddressType type, Pageable pageable);

        /**
         * Counts addresses by type for a specific customer.
         * 
         * @param customerId the customer ID
         * @param type       the address type
         * @return count of addresses of the specified type
         */
        long countByCustomerIdAndTypeAndIsActiveTrue(UUID customerId, AddressType type);

        // ================================================================
        // Geographic Queries
        // ================================================================

        /**
         * Finds addresses in a specific city.
         * Useful for delivery zone analysis and logistics.
         * 
         * @param city     the city name
         * @param pageable pagination information
         * @return Page of addresses in the specified city
         */
        @Query("SELECT a FROM Address a WHERE LOWER(a.city) = LOWER(:city) AND a.isActive = true")
        Page<Address> findByCity(@Param("city") String city, Pageable pageable);

        /**
         * Finds addresses in a specific state.
         * Useful for regional analysis and tax calculations.
         * 
         * @param state    the state name
         * @param pageable pagination information
         * @return Page of addresses in the specified state
         */
        @Query("SELECT a FROM Address a WHERE LOWER(a.state) = LOWER(:state) AND a.isActive = true")
        Page<Address> findByState(@Param("state") String state, Pageable pageable);

        /**
         * Finds addresses by postal code.
         * Useful for delivery zone management and logistics optimization.
         * 
         * @param postalCode the postal code
         * @return List of addresses with the specified postal code
         */
        List<Address> findByPostalCodeAndIsActiveTrue(String postalCode);

        /**
         * Finds addresses within a list of postal codes.
         * Useful for delivery zone queries and bulk operations.
         * 
         * @param postalCodes list of postal codes
         * @return List of addresses within the specified postal codes
         */
        @Query("SELECT a FROM Address a WHERE a.postalCode IN :postalCodes AND a.isActive = true")
        List<Address> findByPostalCodeIn(@Param("postalCodes") List<String> postalCodes);

        // ================================================================
        // Search and Filter Queries
        // ================================================================

        /**
         * Searches addresses by street address.
         * Case-insensitive partial matching.
         * 
         * @param streetAddress the street address to search for
         * @param pageable      pagination information
         * @return Page of addresses matching the search criteria
         */
        @Query("SELECT a FROM Address a WHERE " +
                        "LOWER(a.streetAddress) LIKE LOWER(CONCAT('%', :streetAddress, '%')) AND a.isActive = true")
        Page<Address> findByStreetAddressContainingIgnoreCase(@Param("streetAddress") String streetAddress,
                        Pageable pageable);

        /**
         * Finds addresses with delivery instructions.
         * Useful for delivery planning and special handling requirements.
         * 
         * @param pageable pagination information
         * @return Page of addresses with delivery instructions
         */
        @Query("SELECT a FROM Address a WHERE a.deliveryInstructions IS NOT NULL AND " +
                        "a.deliveryInstructions != '' AND a.isActive = true")
        Page<Address> findAddressesWithDeliveryInstructions(Pageable pageable);

        // ================================================================
        // Validation and Business Logic Queries
        // ================================================================

        /**
         * Checks if a customer has any addresses.
         * 
         * @param customerId the customer ID
         * @return true if the customer has at least one active address
         */
        boolean existsByCustomerIdAndIsActiveTrue(UUID customerId);

        /**
         * Checks if a customer has a default address.
         * 
         * @param customerId the customer ID
         * @return true if the customer has a default address
         */
        boolean existsByCustomerIdAndIsDefaultTrueAndIsActiveTrue(UUID customerId);

        /**
         * Counts active addresses for a customer.
         * 
         * @param customerId the customer ID
         * @return count of active addresses
         */
        long countByCustomerIdAndIsActiveTrue(UUID customerId);

        /**
         * Finds duplicate addresses for a customer.
         * Checks for same street address, city, state, and postal code.
         * 
         * @param customerId    the customer ID
         * @param streetAddress the street address
         * @param city          the city
         * @param state         the state
         * @param postalCode    the postal code
         * @return List of addresses matching the criteria
         */
        @Query("SELECT a FROM Address a WHERE a.customer.id = :customerId AND " +
                        "LOWER(a.streetAddress) = LOWER(:streetAddress) AND " +
                        "LOWER(a.city) = LOWER(:city) AND " +
                        "LOWER(a.state) = LOWER(:state) AND " +
                        "a.postalCode = :postalCode AND a.isActive = true")
        List<Address> findDuplicateAddresses(@Param("customerId") UUID customerId,
                        @Param("streetAddress") String streetAddress,
                        @Param("city") String city,
                        @Param("state") String state,
                        @Param("postalCode") String postalCode);

        // ================================================================
        // Soft Delete Operations
        // ================================================================

        /**
         * Soft deletes an address by setting isActive to false.
         * Also removes default flag if the address was default.
         * 
         * @param addressId  the address ID
         * @param customerId the customer ID (for security)
         * @return number of addresses updated (should be 1)
         */
        @Modifying
        @Transactional
        @Query("UPDATE Address a SET a.isActive = false, a.isDefault = false " +
                        "WHERE a.id = :addressId AND a.customer.id = :customerId")
        int softDeleteAddress(@Param("addressId") UUID addressId, @Param("customerId") UUID customerId);

        /**
         * Reactivates a soft-deleted address.
         * 
         * @param addressId  the address ID
         * @param customerId the customer ID (for security)
         * @return number of addresses updated (should be 1)
         */
        @Modifying
        @Transactional
        @Query("UPDATE Address a SET a.isActive = true WHERE a.id = :addressId AND a.customer.id = :customerId")
        int reactivateAddress(@Param("addressId") UUID addressId, @Param("customerId") UUID customerId);

        // ================================================================
        // Bulk Operations
        // ================================================================

        /**
         * Soft deletes all addresses for a customer.
         * Used when a customer account is deactivated.
         * 
         * @param customerId the customer ID
         * @return number of addresses updated
         */
        @Modifying
        @Transactional
        @Query("UPDATE Address a SET a.isActive = false, a.isDefault = false WHERE a.customer.id = :customerId")
        int softDeleteAllAddressesForCustomer(@Param("customerId") UUID customerId);

        /**
         * Counts addresses by city for analytics.
         * 
         * @return List of objects containing city and count
         */
        @Query("SELECT a.city, COUNT(a) FROM Address a WHERE a.isActive = true GROUP BY a.city ORDER BY COUNT(a) DESC")
        List<Object[]> countAddressesByCity();

        /**
         * Counts addresses by state for analytics.
         * 
         * @return List of objects containing state and count
         */
        @Query("SELECT a.state, COUNT(a) FROM Address a WHERE a.isActive = true GROUP BY a.state ORDER BY COUNT(a) DESC")
        List<Object[]> countAddressesByState();
}
package com.raagaandroast.customer.service;

import com.raagaandroast.customer.dto.*;
import com.raagaandroast.customer.entity.Customer;
import com.raagaandroast.customer.mapper.CustomerMapper;
import com.raagaandroast.customer.repository.CustomerRepository;
import com.raagaandroast.common.exception.*;
import com.raagaandroast.user.entity.User;
import com.raagaandroast.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service class for Customer business logic.
 * 
 * This service handles all customer-related business operations including
 * customer creation, updates, profile management, and business rule
 * enforcement.
 * It demonstrates proper transaction management, error handling, and security.
 * 
 * Design Decisions:
 * - Transactional service layer for data consistency
 * - Business logic separation from controllers
 * - Comprehensive validation and error handling
 * - Performance-optimized queries
 * - Security-conscious operations
 * 
 * Transaction Strategy:
 * - Read operations: @Transactional(readOnly = true) for performance
 * - Write operations: @Transactional for ACID compliance
 * - Proper exception handling for rollback scenarios
 * 
 * Interview Points:
 * - Why service layer? Business logic separation, transaction boundaries
 * - Why @Transactional? Data consistency, rollback on exceptions
 * - Why readOnly transactions? Performance optimization, connection pooling
 * - How to handle business rules? Validation, custom exceptions
 * 
 * @author RaagaAndRoast Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    // ================================================================
    // Customer Creation and Registration
    // ================================================================

    /**
     * Creates a new customer profile for an existing user.
     * 
     * Business Rules:
     * - User must exist and be enabled
     * - User cannot already have a customer profile
     * - Phone number must be unique if provided
     * - First address becomes default if provided
     * 
     * @param request the customer creation request
     * @param userId  the user ID to associate with the customer
     * @return the created customer response
     * @throws IllegalArgumentException if user not found or already has customer
     * @throws IllegalStateException    if phone number already exists
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request, UUID userId) {
        log.info("Creating customer profile for user: {}", userId);

        // Validate user exists and is enabled
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.getEnabled()) {
            throw DisabledUserException.forUser(userId.toString());
        }

        // Check if user already has a customer profile
        if (customerRepository.existsByUserId(userId)) {
            throw new CustomerProfileAlreadyExistsException("User already has a customer profile: " + userId);
        }

        // Validate phone number uniqueness if provided
        if (request.phoneNumber() != null && customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists: " + request.phoneNumber());
        }

        // Create customer entity
        Customer customer = customerMapper.toEntity(request, user);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Successfully created customer profile: {} for user: {}", savedCustomer.getId(), userId);
        return customerMapper.toResponse(savedCustomer);
    }

    // ================================================================
    // Customer Retrieval Operations
    // ================================================================

    /**
     * Finds a customer by ID with all relationships loaded.
     * 
     * @param customerId the customer ID
     * @return the customer response
     * @throws IllegalArgumentException if customer not found
     */
    public CustomerResponse findById(UUID customerId) {
        log.debug("Finding customer by ID: {}", customerId);

        Customer customer = customerRepository.findByIdWithAllRelationships(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        return customerMapper.toResponse(customer);
    }

    /**
     * Finds a customer by user ID.
     * 
     * @param userId the user ID
     * @return the customer response
     * @throws IllegalArgumentException if customer not found
     */
    public CustomerResponse findByUserId(UUID userId) {
        log.debug("Finding customer by user ID: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + userId));

        return customerMapper.toResponse(customer);
    }

    /**
     * Finds a customer by username.
     * 
     * @param username the username
     * @return the customer response
     * @throws IllegalArgumentException if customer not found
     */
    public CustomerResponse findByUsername(String username) {
        log.debug("Finding customer by username: {}", username);

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for username: " + username));

        return customerMapper.toResponse(customer);
    }

    /**
     * Finds a customer by email.
     * 
     * @param email the email
     * @return the customer response
     * @throws IllegalArgumentException if customer not found
     */
    public CustomerResponse findByEmail(String email) {
        log.debug("Finding customer by email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for email: " + email));

        return customerMapper.toResponse(customer);
    }

    /**
     * Checks if a customer exists for the given user ID.
     *
     * @param userId the user ID
     * @return true if customer exists
     */
    public boolean existsByUserId(UUID userId) {
        return customerRepository.existsByUserId(userId);
    }

    // ================================================================
    // Customer Update Operations
    // ================================================================

    /**
     * Updates an existing customer profile.
     *
     * Business Rules:
     * - Customer must exist
     * - Version must match for optimistic locking
     * - Phone number must be unique if changed
     * - Only provided fields are updated
     *
     * @param customerId the customer ID
     * @param request    the update request
     * @return the updated customer response
     * @throws ResourceNotFoundException  if customer not found
     * @throws OptimisticLockingException if version mismatch
     * @throws DuplicateResourceException if phone number conflict
     */
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, UpdateCustomerRequest request) {
        log.info("Updating customer: {}", customerId);

        // Find existing customer with all relationships loaded
        Customer customer = customerRepository.findByIdWithAllRelationships(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        // Validate version for optimistic locking
        if (!customer.getVersion().equals(request.version())) {
            throw OptimisticLockingException.forEntity("Customer", customerId);
        }

        // Validate phone number uniqueness if being changed
        if (request.phoneNumber() != null &&
                !request.phoneNumber().equals(customer.getPhoneNumber()) &&
                customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists: " + request.phoneNumber());
        }

        // Update customer fields
        customerMapper.updateEntity(customer, request);
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Successfully updated customer: {}", customerId);
        return customerMapper.toResponse(updatedCustomer);
    }

    // ================================================================
    // Customer Search and Filtering
    // ================================================================

    /**
     * Searches customers by name with pagination.
     * 
     * @param name     the name to search for
     * @param pageable pagination information
     * @return page of customer responses
     */
    public Page<CustomerResponse> searchByName(String name, Pageable pageable) {
        log.debug("Searching customers by name: {} with pagination: {}", name, pageable);

        Page<Customer> customers = customerRepository.findByNameContainingIgnoreCase(name, pageable);
        return customers.map(customerMapper::toResponseWithoutAddresses);
    }

    /**
     * Finds customers by marketing consent status.
     * 
     * @param marketingConsent the marketing consent status
     * @param pageable         pagination information
     * @return page of customer responses
     */
    public Page<CustomerResponse> findByMarketingConsent(Boolean marketingConsent, Pageable pageable) {
        log.debug("Finding customers by marketing consent: {} with pagination: {}", marketingConsent, pageable);

        Page<Customer> customers = customerRepository.findByMarketingConsent(marketingConsent, pageable);
        return customers.map(customerMapper::toResponseWithoutAddresses);
    }

    /**
     * Finds customers by city.
     * 
     * @param city     the city name
     * @param pageable pagination information
     * @return page of customer responses
     */
    public Page<CustomerResponse> findByCity(String city, Pageable pageable) {
        log.debug("Finding customers by city: {} with pagination: {}", city, pageable);

        Page<Customer> customers = customerRepository.findByAddressCity(city, pageable);
        return customers.map(customerMapper::toResponseWithoutAddresses);
    }

    /**
     * Finds customers by state.
     * 
     * @param state    the state name
     * @param pageable pagination information
     * @return page of customer responses
     */
    public Page<CustomerResponse> findByState(String state, Pageable pageable) {
        log.debug("Finding customers by state: {} with pagination: {}", state, pageable);

        Page<Customer> customers = customerRepository.findByAddressState(state, pageable);
        return customers.map(customerMapper::toResponseWithoutAddresses);
    }

    /**
     * Finds all customers with pagination.
     * 
     * @param pageable pagination information
     * @return page of customer responses
     */
    public Page<CustomerResponse> findAll(Pageable pageable) {
        log.debug("Finding all customers with pagination: {}", pageable);

        Page<Customer> customers = customerRepository.findAllWithUser(pageable);
        return customers.map(customerMapper::toResponseWithoutAddresses);
    }

    // ================================================================
    // Birthday and Special Occasions
    // ================================================================

    /**
     * Finds customers with birthdays today.
     * 
     * @return list of customers with birthdays today
     */
    public List<CustomerResponse> findCustomersWithBirthdayToday() {
        LocalDate today = LocalDate.now();
        log.debug("Finding customers with birthday today: {}", today);

        List<Customer> customers = customerRepository.findByBirthdayMonthAndDay(
                today.getMonthValue(), today.getDayOfMonth());

        return customers.stream()
                .map(customerMapper::toResponseWithoutAddresses)
                .toList();
    }

    /**
     * Finds customers born on a specific date.
     * 
     * @param date the birth date
     * @return list of customers born on the date
     */
    public List<CustomerResponse> findByDateOfBirth(LocalDate date) {
        log.debug("Finding customers by date of birth: {}", date);

        List<Customer> customers = customerRepository.findByDateOfBirth(date);
        return customers.stream()
                .map(customerMapper::toResponseWithoutAddresses)
                .toList();
    }

    // ================================================================
    // Customer Analytics and Reporting
    // ================================================================

    /**
     * Counts customers by marketing consent status.
     * 
     * @param marketingConsent the marketing consent status
     * @return count of customers
     */
    public long countByMarketingConsent(Boolean marketingConsent) {
        log.debug("Counting customers by marketing consent: {}", marketingConsent);
        return customerRepository.countByMarketingConsent(marketingConsent);
    }

    /**
     * Finds recently registered customers.
     * 
     * @param days     number of days to look back
     * @param pageable pagination information
     * @return page of recently registered customers
     */
    public Page<CustomerResponse> findRecentCustomers(int days, Pageable pageable) {
        log.debug("Finding customers registered in last {} days", days);

        Page<Customer> customers = customerRepository.findRecentCustomers(days, pageable);
        return customers.map(customerMapper::toResponseWithoutAddresses);
    }

    /**
     * Finds customers without addresses.
     * 
     * @param pageable pagination information
     * @return page of customers without addresses
     */
    public Page<CustomerResponse> findCustomersWithoutAddresses(Pageable pageable) {
        log.debug("Finding customers without addresses");

        Page<Customer> customers = customerRepository.findCustomersWithoutAddresses(pageable);
        return customers.map(customerMapper::toResponseWithoutAddresses);
    }

    // ================================================================
    // Business Logic Helpers
    // ================================================================

    /**
     * Gets customer summary information for dashboard views.
     * 
     * @param customerId the customer ID
     * @return customer summary response
     */
    public CustomerResponse getCustomerSummary(UUID customerId) {
        log.debug("Getting customer summary for: {}", customerId);

        Customer customer = customerRepository.findByIdWithAllRelationships(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        return customerMapper.toSummaryResponse(customer);
    }

    /**
     * Gets public customer information (for staff views).
     *
     * @param customerId the customer ID
     * @return public customer response
     */
    public CustomerResponse getPublicCustomerInfo(UUID customerId) {
        log.debug("Getting public customer info for: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        return customerMapper.toPublicResponse(customer);
    }

    /**
     * Gets customer ID by username.
     *
     * This is a convenience method for authentication-based operations
     * where we need to get the customer ID from the authenticated username.
     *
     * @param username the username
     * @return the customer ID
     * @throws ResourceNotFoundException if customer not found
     */
    public UUID getCustomerIdByUsername(String username) {
        log.debug("Getting customer ID by username: {}", username);

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for username: " + username));

        return customer.getId();
    }

    /**
     * Gets customer ID by user ID.
     *
     * This is a convenience method for getting customer ID from user ID.
     *
     * @param userId the user ID
     * @return the customer ID
     * @throws ResourceNotFoundException if customer not found
     */
    public UUID getCustomerIdByUserId(UUID userId) {
        log.debug("Getting customer ID by user ID: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + userId));

        return customer.getId();
    }
}
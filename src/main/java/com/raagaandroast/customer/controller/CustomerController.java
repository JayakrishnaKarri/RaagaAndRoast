package com.raagaandroast.customer.controller;

import com.raagaandroast.customer.dto.*;
import com.raagaandroast.customer.service.CustomerService;
import com.raagaandroast.security.authentication.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Customer management operations.
 * 
 * This controller provides comprehensive customer management endpoints
 * with proper authorization, validation, and error handling. It demonstrates
 * multiple authorization patterns and RESTful API design principles.
 * 
 * Authorization Patterns Demonstrated:
 * 1. Role-based: @PreAuthorize("hasRole('ADMIN')")
 * 2. Permission-based: @PreAuthorize("hasAuthority('CUSTOMER_READ')")
 * 3. Resource
 * ownership: @PreAuthorize("@resourceOwnership.isOwnerOrAdmin(...)")
 * 4. Combined: Multiple conditions with AND/OR logic
 * 5. Method-level security with dynamic parameters
 * 
 * API Design Principles:
 * - RESTful endpoints with proper HTTP methods
 * - Consistent response formats
 * - Proper HTTP status codes
 * - Pagination support for list operations
 * - Comprehensive validation
 * 
 * Interview Points:
 * - Why different authorization patterns? Different security requirements
 * - Why @AuthenticationPrincipal? Access to current user context
 * - Why pagination? Performance, user experience
 * - Why validation? Data integrity, security
 * 
 * @author RaagaAndRoast Development Team
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "Customer profile and management operations")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    // ================================================================
    // Customer Profile Management (Self-Service)
    // ================================================================

    /**
     * Creates a customer profile for the authenticated user.
     * 
     * Authorization: Any authenticated user can create their own customer profile
     * Business Rule: One customer profile per user
     * 
     * @param request   the customer creation request
     * @param principal the authenticated user
     * @return the created customer response
     */
    @PostMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create customer profile", description = "Creates a customer profile for the authenticated user. One profile per user allowed.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation errors"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions - CUSTOMER role required"),
            @ApiResponse(responseCode = "409", description = "Customer profile already exists for this user")
    })
    public ResponseEntity<CustomerResponse> createProfile(
            @Valid @RequestBody CreateCustomerRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.info("Creating customer profile for user: {}", principal.getId());

        CustomerResponse response = customerService.createCustomer(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets the authenticated user's customer profile.
     * 
     * Authorization: Customer can access their own profile
     * 
     * @param principal the authenticated user
     * @return the customer response
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my customer profile", description = "Retrieves the authenticated user's customer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions - CUSTOMER role required"),
            @ApiResponse(responseCode = "404", description = "Customer profile not found")
    })
    public ResponseEntity<CustomerResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("Getting customer profile for user: {}", principal.getId());

        CustomerResponse response = customerService.findByUserId(principal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the authenticated user's customer profile.
     * 
     * Authorization: Customer can update their own profile
     * 
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated customer response
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> updateMyProfile(
            @Valid @RequestBody UpdateCustomerRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.info("Updating customer profile for user: {}", principal.getId());

        // First get the customer to find their ID
        CustomerResponse currentProfile = customerService.findByUserId(principal.getId());
        CustomerResponse response = customerService.updateCustomer(currentProfile.id(), request);

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // Customer Management (Admin/Manager Operations)
    // ================================================================

    /**
     * Gets a customer by ID.
     * 
     * Authorization:
     * - Customer can access their own profile
     * - Admin/Manager can access any customer profile
     * 
     * @param customerId the customer ID
     * @param principal  the authenticated user
     * @return the customer response
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable UUID customerId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("Getting customer: {} by user: {}", customerId, principal.getUsername());

        CustomerResponse response = customerService.findById(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a customer profile.
     * 
     * Authorization:
     * - Customer can update their own profile
     * - Admin/Manager can update any customer profile
     * 
     * @param customerId the customer ID
     * @param request    the update request
     * @param principal  the authenticated user
     * @return the updated customer response
     */
    @PutMapping("/{customerId}")
    @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.info("Updating customer: {} by user: {}", customerId, principal.getUsername());

        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // Customer Search and Listing (Staff Operations)
    // ================================================================

    /**
     * Gets all customers with pagination.
     * 
     * Authorization: Staff members (Manager, Admin) can list customers
     * 
     * @param pageable pagination parameters
     * @return page of customer responses
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    @Operation(summary = "Get all customers", description = "Retrieves all customers with pagination support. Requires MANAGER or ADMIN role with CUSTOMER_READ permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions - MANAGER/ADMIN role and CUSTOMER_READ permission required")
    })
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @PageableDefault(size = 20, sort = "createdAt") @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {

        log.debug("Getting all customers with pagination: {}", pageable);

        Page<CustomerResponse> response = customerService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches customers by name.
     * 
     * Authorization: Staff members can search customers
     * 
     * @param name     the name to search for
     * @param pageable pagination parameters
     * @return page of matching customer responses
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<Page<CustomerResponse>> searchCustomers(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {

        log.debug("Searching customers by name: {} with pagination: {}", name, pageable);

        Page<CustomerResponse> response = customerService.searchByName(name, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds customers by marketing consent status.
     * 
     * Authorization: Marketing staff and admins
     * 
     * @param marketingConsent the marketing consent status
     * @param pageable         pagination parameters
     * @return page of customer responses
     */
    @GetMapping("/marketing-consent/{marketingConsent}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and hasAuthority('MARKETING_READ'))")
    public ResponseEntity<Page<CustomerResponse>> getCustomersByMarketingConsent(
            @PathVariable Boolean marketingConsent,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("Finding customers by marketing consent: {} with pagination: {}", marketingConsent, pageable);

        Page<CustomerResponse> response = customerService.findByMarketingConsent(marketingConsent, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds customers by city.
     * 
     * Authorization: Staff members for delivery planning
     * 
     * @param city     the city name
     * @param pageable pagination parameters
     * @return page of customer responses
     */
    @GetMapping("/city/{city}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<Page<CustomerResponse>> getCustomersByCity(
            @PathVariable String city,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {

        log.debug("Finding customers by city: {} with pagination: {}", city, pageable);

        Page<CustomerResponse> response = customerService.findByCity(city, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds customers by state.
     * 
     * Authorization: Staff members for regional analysis
     * 
     * @param state    the state name
     * @param pageable pagination parameters
     * @return page of customer responses
     */
    @GetMapping("/state/{state}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<Page<CustomerResponse>> getCustomersByState(
            @PathVariable String state,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {

        log.debug("Finding customers by state: {} with pagination: {}", state, pageable);

        Page<CustomerResponse> response = customerService.findByState(state, pageable);
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // Customer Analytics and Reporting
    // ================================================================

    /**
     * Gets customers with birthdays today.
     * 
     * Authorization: Staff members for birthday promotions
     * 
     * @return list of customers with birthdays today
     */
    @GetMapping("/birthdays/today")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<List<CustomerResponse>> getCustomersWithBirthdayToday() {

        log.debug("Getting customers with birthday today");

        List<CustomerResponse> response = customerService.findCustomersWithBirthdayToday();
        return ResponseEntity.ok(response);
    }

    /**
     * Gets customers born on a specific date.
     * 
     * Authorization: Staff members for targeted promotions
     * 
     * @param date the birth date (YYYY-MM-DD format)
     * @return list of customers born on the date
     */
    @GetMapping("/birthdays/{date}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<List<CustomerResponse>> getCustomersByBirthDate(
            @PathVariable LocalDate date) {

        log.debug("Getting customers by birth date: {}", date);

        List<CustomerResponse> response = customerService.findByDateOfBirth(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets recently registered customers.
     * 
     * Authorization: Management for onboarding analysis
     * 
     * @param days     number of days to look back (default: 7)
     * @param pageable pagination parameters
     * @return page of recently registered customers
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<Page<CustomerResponse>> getRecentCustomers(
            @RequestParam(defaultValue = "7") int days,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("Getting customers registered in last {} days", days);

        Page<CustomerResponse> response = customerService.findRecentCustomers(days, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets customers without addresses.
     * 
     * Authorization: Staff for data quality and customer onboarding
     * 
     * @param pageable pagination parameters
     * @return page of customers without addresses
     */
    @GetMapping("/no-addresses")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<Page<CustomerResponse>> getCustomersWithoutAddresses(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.debug("Getting customers without addresses");

        Page<CustomerResponse> response = customerService.findCustomersWithoutAddresses(pageable);
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // Customer Lookup Operations
    // ================================================================

    /**
     * Finds a customer by username.
     * 
     * Authorization: Staff members for customer support
     * 
     * @param username the username
     * @return the customer response
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<CustomerResponse> getCustomerByUsername(
            @PathVariable String username) {

        log.debug("Finding customer by username: {}", username);

        CustomerResponse response = customerService.findByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds a customer by email.
     * 
     * Authorization: Staff members for customer support
     * 
     * @param email the email address
     * @return the customer response
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(
            @PathVariable String email) {

        log.debug("Finding customer by email: {}", email);

        CustomerResponse response = customerService.findByEmail(email);
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // Customer Summary Operations
    // ================================================================

    /**
     * Gets customer summary information.
     * 
     * Authorization: Customer can get their own summary, staff can get any
     * 
     * @param customerId the customer ID
     * @param principal  the authenticated user
     * @return the customer summary response
     */
    @GetMapping("/{customerId}/summary")
    @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
    public ResponseEntity<CustomerResponse> getCustomerSummary(
            @PathVariable UUID customerId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("Getting customer summary: {} by user: {}", customerId, principal.getUsername());

        CustomerResponse response = customerService.getCustomerSummary(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets public customer information (for staff views).
     * 
     * Authorization: Staff members only
     * 
     * @param customerId the customer ID
     * @return the public customer response
     */
    @GetMapping("/{customerId}/public")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN') and hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<CustomerResponse> getPublicCustomerInfo(
            @PathVariable UUID customerId) {

        log.debug("Getting public customer info: {}", customerId);

        CustomerResponse response = customerService.getPublicCustomerInfo(customerId);
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // Utility Endpoints
    // ================================================================

    /**
     * Checks if a customer profile exists for the authenticated user.
     * 
     * Authorization: Any authenticated user
     * 
     * @param principal the authenticated user
     * @return true if customer profile exists
     */
    @GetMapping("/profile/exists")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> checkProfileExists(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("Checking if customer profile exists for user: {}", principal.getId());

        boolean exists = customerService.existsByUserId(principal.getId());
        return ResponseEntity.ok(exists);
    }

    /**
     * Gets marketing consent statistics.
     * 
     * Authorization: Marketing staff and admins
     * 
     * @return marketing consent statistics
     */
    @GetMapping("/statistics/marketing-consent")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and hasAuthority('MARKETING_READ'))")
    public ResponseEntity<MarketingConsentStats> getMarketingConsentStats() {

        log.debug("Getting marketing consent statistics");

        long consentedCount = customerService.countByMarketingConsent(true);
        long notConsentedCount = customerService.countByMarketingConsent(false);

        MarketingConsentStats stats = new MarketingConsentStats(
                consentedCount, notConsentedCount, consentedCount + notConsentedCount);

        return ResponseEntity.ok(stats);
    }

    /**
     * DTO for marketing consent statistics.
     */
    public record MarketingConsentStats(
            long consentedCount,
            long notConsentedCount,
            long totalCount) {
    }
}
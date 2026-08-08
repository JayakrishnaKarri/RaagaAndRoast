package com.raagaandroast.customer.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raagaandroast.customer.dto.AddressResponse;
import com.raagaandroast.customer.dto.CreateAddressRequest;
import com.raagaandroast.customer.dto.UpdateAddressRequest;
import com.raagaandroast.customer.entity.AddressType;
import com.raagaandroast.customer.service.AddressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Address management operations.
 * 
 * This controller provides comprehensive address management functionality
 * with proper authorization patterns, validation, and business logic.
 * 
 * Authorization Patterns Demonstrated:
 * 1. Role-based: @PreAuthorize("hasRole('ADMIN')")
 * 2. Permission-based: @PreAuthorize("hasAuthority('ADDRESS_READ')")
 * 3. Resource
 * ownership: @PreAuthorize("@resourceOwnership.isAddressOwnerOrStaff(...)")
 * 4. Combined authorization: Multiple conditions with business logic
 * 
 * Design Decisions:
 * - RESTful endpoint design following HTTP conventions
 * - Comprehensive authorization covering all access patterns
 * - Proper HTTP status codes for different scenarios
 * - Validation using Jakarta Bean Validation
 * - Pagination support for list operations
 * - Business logic delegated to service layer
 * 
 * Performance Considerations:
 * - Pagination prevents memory issues with large datasets
 * - Service layer handles performance optimization
 * - Minimal data transfer through DTOs
 * 
 * Security Considerations:
 * - All endpoints require authentication
 * - Resource ownership validation for customer data
 * - Staff can access customer addresses for support
 * - Admins have full access for management
 * 
 * Interview Points:
 * - Why separate address controller? Single Responsibility Principle
 * - Why multiple authorization patterns? Different business requirements
 * - Why DTOs? Security, validation, and API contract stability
 * - Why service delegation? Separation of concerns and testability
 * - Why proper HTTP status codes? RESTful API design and client clarity
 * 
 * @author RaagaAndRoast Development Team
 */

@Slf4j
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Validated
@Tag(name = "Address Management", description = "Customer address management operations")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    // ================================================================
    // Customer Address Management
    // ================================================================

    /**
     * Creates a new address for the authenticated customer.
     * 
     * Authorization: Customer can create addresses for themselves
     * 
     * @param request        the address creation request
     * @param authentication the authentication context
     * @return ResponseEntity with created address
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create new address", description = "Creates a new address for the authenticated customer. Supports multiple address types (HOME, WORK, OTHER).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation errors"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions - CUSTOMER role required"),
            @ApiResponse(responseCode = "409", description = "Business rule violation (e.g., duplicate default address)")
    })
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody CreateAddressRequest request,
            Authentication authentication) {

        log.info("Creating address for customer: {}", authentication.getName());

        AddressResponse response = addressService.createAddressForAuthenticatedCustomer(request, authentication);

        log.info("Successfully created address with ID: {} for customer: {}",
                response.id(), authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all addresses for the authenticated customer.
     * 
     * Authorization: Customer can view their own addresses
     * 
     * @param authentication the authentication context
     * @return ResponseEntity with list of customer addresses
     */
    @GetMapping("/my-addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my addresses", description = "Retrieves all addresses for the authenticated customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required")
    })
    public ResponseEntity<List<AddressResponse>> getMyAddresses(Authentication authentication) {

        log.info("Retrieving addresses for customer: {}", authentication.getName());

        List<AddressResponse> addresses = addressService.getAddressesForAuthenticatedCustomer(authentication);

        log.info("Retrieved {} addresses for customer: {}", addresses.size(), authentication.getName());

        return ResponseEntity.ok(addresses);
    }

    /**
     * Retrieves a specific address by ID.
     * 
     * Authorization: Customer can view own addresses, staff can view any address
     * 
     * @param addressId      the address ID
     * @param authentication the authentication context
     * @return ResponseEntity with address details
     */
    @GetMapping("/{addressId}")
    @PreAuthorize("@resourceOwnership.isAddressOwnerOrStaff(authentication, #addressId)")
    @Operation(summary = "Get address by ID", description = "Retrieves a specific address by ID. Customers can access their own addresses, staff can access any address.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - not address owner or staff"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<AddressResponse> getAddressById(
            @Parameter(description = "Address ID") @PathVariable UUID addressId,
            Authentication authentication) {

        log.info("Retrieving address: {} for user: {}", addressId, authentication.getName());

        AddressResponse response = addressService.getAddressById(addressId);

        log.info("Successfully retrieved address: {}", addressId);

        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing address.
     * 
     * Authorization: Customer can update own addresses, staff can update any
     * address
     * 
     * @param addressId      the address ID
     * @param request        the address update request
     * @param authentication the authentication context
     * @return ResponseEntity with updated address
     */
    @PutMapping("/{addressId}")
    @PreAuthorize("@resourceOwnership.isAddressOwnerOrStaff(authentication, #addressId)")
    @Operation(summary = "Update address", description = "Updates an existing address. Customers can update their own addresses, staff can update any address.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation errors"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - not address owner or staff"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<AddressResponse> updateAddress(
            @Parameter(description = "Address ID") @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request,
            Authentication authentication) {

        log.info("Updating address: {} for user: {}", addressId, authentication.getName());

        AddressResponse response = addressService.updateAddress(addressId, request);

        log.info("Successfully updated address: {}", addressId);

        return ResponseEntity.ok(response);
    }

    /**
     * Sets an address as the default address for the customer.
     * 
     * Authorization: Customer can set own addresses as default, staff can set any
     * address as default
     * 
     * @param addressId      the address ID
     * @param authentication the authentication context
     * @return ResponseEntity with updated address
     */
    @PatchMapping("/{addressId}/set-default")
    @PreAuthorize("@resourceOwnership.isAddressOwnerOrStaff(authentication, #addressId)")
    @Operation(summary = "Set default address", description = "Sets an address as the default address for the customer. Only one address can be default per customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address set as default successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - not address owner or staff"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<AddressResponse> setAsDefaultAddress(
            @Parameter(description = "Address ID") @PathVariable UUID addressId,
            Authentication authentication) {

        log.info("Setting address: {} as default for user: {}", addressId, authentication.getName());

        AddressResponse response = addressService.setAsDefaultAddress(addressId);

        log.info("Successfully set address: {} as default", addressId);

        return ResponseEntity.ok(response);
    }

    /**
     * Soft deletes an address.
     * 
     * Authorization: Customer can delete own addresses, staff can delete any
     * address
     * 
     * @param addressId      the address ID
     * @param authentication the authentication context
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{addressId}")
    @PreAuthorize("@resourceOwnership.isAddressOwnerOrStaff(authentication, #addressId)")
    @Operation(summary = "Delete address", description = "Soft deletes an address. Customers can delete their own addresses, staff can delete any address.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - not address owner or staff"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "Address ID") @PathVariable UUID addressId,
            Authentication authentication) {

        log.info("Deleting address: {} for user: {}", addressId, authentication.getName());

        addressService.deleteAddress(addressId);

        log.info("Successfully deleted address: {}", addressId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a soft-deleted address.
     * 
     * Authorization: Customer can reactivate own addresses, staff can reactivate
     * any address
     * 
     * @param addressId      the address ID
     * @param authentication the authentication context
     * @return ResponseEntity with reactivated address
     */
    @PatchMapping("/{addressId}/reactivate")
    @PreAuthorize("@resourceOwnership.isAddressOwnerOrStaff(authentication, #addressId)")
    public ResponseEntity<AddressResponse> reactivateAddress(
            @PathVariable UUID addressId,
            Authentication authentication) {

        log.info("Reactivating address: {} for user: {}", addressId, authentication.getName());

        AddressResponse response = addressService.reactivateAddress(addressId);

        log.info("Successfully reactivated address: {}", addressId);

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // Customer-Specific Address Management
    // ================================================================

    /**
     * Retrieves all addresses for a specific customer.
     * 
     * Authorization: Staff and above can view customer addresses
     * 
     * @param customerId the customer ID
     * @param pageable   pagination information
     * @return ResponseEntity with paginated customer addresses
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<AddressResponse>> getCustomerAddresses(
            @PathVariable UUID customerId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Retrieving addresses for customer: {} with pagination: {}", customerId, pageable);

        Page<AddressResponse> addresses = addressService.getAddressesByCustomerId(customerId, pageable);

        log.info("Retrieved {} addresses for customer: {}", addresses.getTotalElements(), customerId);

        return ResponseEntity.ok(addresses);
    }

    /**
     * Retrieves addresses for a customer filtered by type.
     * 
     * Authorization: Staff and above can view customer addresses
     * 
     * @param customerId the customer ID
     * @param type       the address type filter
     * @return ResponseEntity with filtered customer addresses
     */
    @GetMapping("/customer/{customerId}/type/{type}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<AddressResponse>> getCustomerAddressesByType(
            @PathVariable UUID customerId,
            @PathVariable AddressType type) {

        log.info("Retrieving {} addresses for customer: {}", type, customerId);

        List<AddressResponse> addresses = addressService.getAddressesByCustomerIdAndType(customerId, type);

        log.info("Retrieved {} {} addresses for customer: {}", addresses.size(), type, customerId);

        return ResponseEntity.ok(addresses);
    }

    /**
     * Gets the default address for a specific customer.
     * 
     * Authorization: Staff and above can view customer default addresses
     * 
     * @param customerId the customer ID
     * @return ResponseEntity with default address or 404 if not found
     */
    @GetMapping("/customer/{customerId}/default")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<AddressResponse> getCustomerDefaultAddress(@PathVariable UUID customerId) {

        log.info("Retrieving default address for customer: {}", customerId);

        AddressResponse defaultAddress = addressService.getDefaultAddressByCustomerId(customerId);

        log.info("Retrieved default address for customer: {}", customerId);

        return ResponseEntity.ok(defaultAddress);
    }

    // ================================================================
    // Geographic and Search Operations
    // ================================================================

    /**
     * Searches addresses by city.
     * 
     * Authorization: Manager and above can search addresses geographically
     * 
     * @param city     the city to search for
     * @param pageable pagination information
     * @return ResponseEntity with addresses in the specified city
     */
    @GetMapping("/search/city")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<AddressResponse>> getAddressesByCity(
            @RequestParam String city,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Searching addresses in city: {} with pagination: {}", city, pageable);

        Page<AddressResponse> addresses = addressService.getAddressesByCity(city, pageable);

        log.info("Found {} addresses in city: {}", addresses.getTotalElements(), city);

        return ResponseEntity.ok(addresses);
    }

    /**
     * Searches addresses by state.
     * 
     * Authorization: Manager and above can search addresses geographically
     * 
     * @param state    the state to search for
     * @param pageable pagination information
     * @return ResponseEntity with addresses in the specified state
     */
    @GetMapping("/search/state")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<AddressResponse>> getAddressesByState(
            @RequestParam String state,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Searching addresses in state: {} with pagination: {}", state, pageable);

        Page<AddressResponse> addresses = addressService.getAddressesByState(state, pageable);

        log.info("Found {} addresses in state: {}", addresses.getTotalElements(), state);

        return ResponseEntity.ok(addresses);
    }

    /**
     * Searches addresses by postal code.
     * 
     * Authorization: Manager and above can search addresses by postal code
     * 
     * @param postalCode the postal code to search for
     * @return ResponseEntity with addresses in the specified postal code
     */
    @GetMapping("/search/postal-code")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<AddressResponse>> getAddressesByPostalCode(@RequestParam String postalCode) {

        log.info("Searching addresses with postal code: {}", postalCode);

        List<AddressResponse> addresses = addressService.getAddressesByPostalCode(postalCode);

        log.info("Found {} addresses with postal code: {}", addresses.size(), postalCode);

        return ResponseEntity.ok(addresses);
    }

    /**
     * Searches addresses by street address (partial matching).
     * 
     * Authorization: Manager and above can search addresses by street
     * 
     * @param streetAddress the street address to search for
     * @param pageable      pagination information
     * @return ResponseEntity with matching addresses
     */
    @GetMapping("/search/street")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<AddressResponse>> searchAddressesByStreet(
            @RequestParam String streetAddress,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Searching addresses by street: {} with pagination: {}", streetAddress, pageable);

        Page<AddressResponse> addresses = addressService.searchAddressesByStreet(streetAddress, pageable);

        log.info("Found {} addresses matching street: {}", addresses.getTotalElements(), streetAddress);

        return ResponseEntity.ok(addresses);
    }

    // ================================================================
    // Administrative Operations
    // ================================================================

    /**
     * Retrieves all addresses with pagination.
     * 
     * Authorization: Admin only - full address access
     * 
     * @param pageable pagination information
     * @return ResponseEntity with paginated addresses
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AddressResponse>> getAllAddresses(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Admin retrieving all addresses with pagination: {}", pageable);

        Page<AddressResponse> addresses = addressService.getAllAddresses(pageable);

        log.info("Retrieved {} total addresses", addresses.getTotalElements());

        return ResponseEntity.ok(addresses);
    }

    /**
     * Retrieves addresses with delivery instructions.
     * 
     * Authorization: Staff and above can view addresses with special delivery
     * requirements
     * 
     * @param pageable pagination information
     * @return ResponseEntity with addresses that have delivery instructions
     */
    @GetMapping("/with-delivery-instructions")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<AddressResponse>> getAddressesWithDeliveryInstructions(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Retrieving addresses with delivery instructions, pagination: {}", pageable);

        Page<AddressResponse> addresses = addressService.getAddressesWithDeliveryInstructions(pageable);

        log.info("Found {} addresses with delivery instructions", addresses.getTotalElements());

        return ResponseEntity.ok(addresses);
    }

    /**
     * Gets address statistics by city.
     * 
     * Authorization: Manager and above can view address analytics
     * 
     * @return ResponseEntity with city-wise address statistics
     */
    @GetMapping("/analytics/by-city")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<Object[]>> getAddressStatisticsByCity() {

        log.info("Retrieving address statistics by city");

        List<Object[]> statistics = addressService.getAddressStatisticsByCity();

        log.info("Retrieved address statistics for {} cities", statistics.size());

        return ResponseEntity.ok(statistics);
    }

    /**
     * Gets address statistics by state.
     * 
     * Authorization: Manager and above can view address analytics
     * 
     * @return ResponseEntity with state-wise address statistics
     */
    @GetMapping("/analytics/by-state")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<Object[]>> getAddressStatisticsByState() {

        log.info("Retrieving address statistics by state");

        List<Object[]> statistics = addressService.getAddressStatisticsByState();

        log.info("Retrieved address statistics for {} states", statistics.size());

        return ResponseEntity.ok(statistics);
    }

    // ================================================================
    // Validation and Business Logic Endpoints
    // ================================================================

    /**
     * Validates if a customer has any addresses.
     * 
     * Authorization: Staff and above can validate customer address status
     * 
     * @param customerId the customer ID
     * @return ResponseEntity with boolean indicating if customer has addresses
     */
    @GetMapping("/customer/{customerId}/has-addresses")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Boolean> customerHasAddresses(@PathVariable UUID customerId) {

        log.info("Checking if customer has addresses: {}", customerId);

        boolean hasAddresses = addressService.customerHasAddresses(customerId);

        log.info("Customer {} has addresses: {}", customerId, hasAddresses);

        return ResponseEntity.ok(hasAddresses);
    }

    /**
     * Validates if a customer has a default address.
     * 
     * Authorization: Staff and above can validate customer default address status
     * 
     * @param customerId the customer ID
     * @return ResponseEntity with boolean indicating if customer has a default
     *         address
     */
    @GetMapping("/customer/{customerId}/has-default")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Boolean> customerHasDefaultAddress(@PathVariable UUID customerId) {

        log.info("Checking if customer has default address: {}", customerId);

        boolean hasDefault = addressService.customerHasDefaultAddress(customerId);

        log.info("Customer {} has default address: {}", customerId, hasDefault);

        return ResponseEntity.ok(hasDefault);
    }

    /**
     * Gets the count of active addresses for a customer.
     * 
     * Authorization: Staff and above can view customer address counts
     * 
     * @param customerId the customer ID
     * @return ResponseEntity with address count
     */
    @GetMapping("/customer/{customerId}/count")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Long> getCustomerAddressCount(@PathVariable UUID customerId) {

        log.info("Getting address count for customer: {}", customerId);

        long count = addressService.getCustomerAddressCount(customerId);

        log.info("Customer {} has {} addresses", customerId, count);

        return ResponseEntity.ok(count);
    }
}
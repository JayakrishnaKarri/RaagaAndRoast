package com.raagaandroast.customer.service;

import com.raagaandroast.customer.dto.*;
import com.raagaandroast.customer.entity.Address;
import com.raagaandroast.customer.entity.AddressType;
import com.raagaandroast.customer.entity.Customer;
import com.raagaandroast.customer.mapper.AddressMapper;
import com.raagaandroast.customer.repository.AddressRepository;
import com.raagaandroast.customer.repository.CustomerRepository;
import com.raagaandroast.common.exception.*;
import com.raagaandroast.security.authentication.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service class for Address business logic.
 * 
 * This service handles all address-related business operations including
 * address creation, updates, default address management, and business rule
 * enforcement. It demonstrates proper transaction management and complex
 * business logic for address lifecycle management.
 * 
 * Design Decisions:
 * - Transactional service layer for data consistency
 * - Complex business logic for default address management
 * - Soft delete support for data preservation
 * - Comprehensive validation and error handling
 * - Performance-optimized queries
 * 
 * Business Rules:
 * - Only one default address per customer
 * - Cannot delete the only address if it's default
 * - Address validation for delivery suitability
 * - Duplicate address prevention
 * 
 * Interview Points:
 * - Why complex default address logic? Business requirements, user experience
 * - Why soft delete? Data preservation, audit trail, referential integrity
 * - Why transactional boundaries? Consistency, rollback scenarios
 * - How to handle concurrent updates? Optimistic locking, version checking
 * 
 * @author RaagaAndRoast Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final AddressMapper addressMapper;

    // ================================================================
    // Address Creation Operations
    // ================================================================

    /**
     * Creates a new address for a customer.
     * 
     * Business Rules:
     * - Customer must exist
     * - Duplicate addresses are prevented
     * - First address becomes default automatically
     * - If marked as default, removes default from other addresses
     * 
     * @param customerId the customer ID
     * @param request    the address creation request
     * @return the created address response
     * @throws IllegalArgumentException if customer not found
     * @throws IllegalStateException    if duplicate address exists
     */
    @Transactional
    public AddressResponse createAddress(UUID customerId, CreateAddressRequest request) {
        log.info("Creating address for customer: {}", customerId);

        // Validate customer exists
        Customer customer = customerRepository.findByIdWithAllRelationships(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        // Check for duplicate addresses
        List<Address> duplicates = addressRepository.findDuplicateAddresses(
                customerId, request.streetAddress(), request.city(),
                request.state(), request.postalCode());

        if (!duplicates.isEmpty()) {
            throw DuplicateAddressException.forCustomer(customerId.toString());
        }

        // Determine if this should be the default address
        boolean shouldBeDefault = request.getIsDefaultOrDefault();
        boolean isFirstAddress = !addressRepository.existsByCustomerIdAndIsActiveTrue(customerId);

        if (isFirstAddress) {
            shouldBeDefault = true; // First address is always default
        }

        // Create address entity
        Address address = addressMapper.toEntity(request, customer);
        address.setIsDefault(shouldBeDefault);

        // If setting as default, remove default flag from other addresses
        if (shouldBeDefault) {
            addressRepository.removeDefaultFlagForCustomer(customerId);
        }

        Address savedAddress = addressRepository.save(address);

        log.info("Successfully created address: {} for customer: {}", savedAddress.getId(), customerId);
        return addressMapper.toResponse(savedAddress);
    }

    // ================================================================
    // Address Retrieval Operations
    // ================================================================

    /**
     * Finds an address by ID.
     * 
     * @param addressId the address ID
     * @return the address response
     * @throws IllegalArgumentException if address not found
     */
    public AddressResponse findById(UUID addressId) {
        log.debug("Finding address by ID: {}", addressId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        return addressMapper.toResponse(address);
    }

    /**
     * Finds all addresses for a customer.
     * 
     * @param customerId the customer ID
     * @return list of address responses
     */
    public List<AddressResponse> findByCustomerId(UUID customerId) {
        log.debug("Finding addresses for customer: {}", customerId);

        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        return addressMapper.toResponseList(addresses);
    }

    /**
     * Finds active addresses for a customer.
     * 
     * @param customerId the customer ID
     * @return list of active address responses
     */
    public List<AddressResponse> findActiveAddressesByCustomerId(UUID customerId) {
        log.debug("Finding active addresses for customer: {}", customerId);

        List<Address> addresses = addressRepository.findByCustomerIdAndIsActiveTrue(customerId);
        return addressMapper.toResponseList(addresses);
    }

    /**
     * Finds addresses by customer ID and type.
     * 
     * @param customerId the customer ID
     * @param type       the address type
     * @return list of address responses
     */
    public List<AddressResponse> findByCustomerIdAndType(UUID customerId, AddressType type) {
        log.debug("Finding addresses for customer: {} and type: {}", customerId, type);

        List<Address> addresses = addressRepository.findByCustomerIdAndTypeAndIsActiveTrue(customerId, type);
        return addressMapper.toResponseList(addresses);
    }

    /**
     * Gets the default address for a customer.
     * 
     * @param customerId the customer ID
     * @return the default address response
     * @throws IllegalArgumentException if no default address found
     */
    public AddressResponse getDefaultAddress(UUID customerId) {
        log.debug("Getting default address for customer: {}", customerId);

        Address address = addressRepository.findByCustomerIdAndIsDefaultTrueAndIsActiveTrue(customerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Default address for customer", customerId));

        return addressMapper.toResponse(address);
    }

    // ================================================================
    // Address Update Operations
    // ================================================================

    /**
     * Updates an existing address.
     * 
     * Business Rules:
     * - Address must exist and be active
     * - Version must match for optimistic locking
     * - If setting as default, removes default from other addresses
     * - Cannot remove default flag if it's the only address
     * 
     * @param addressId the address ID
     * @param request   the update request
     * @return the updated address response
     * @throws IllegalArgumentException if address not found
     * @throws IllegalStateException    if business rules violated
     */
    @Transactional
    public AddressResponse updateAddress(UUID addressId, UpdateAddressRequest request) {
        log.info("Updating address: {}", addressId);

        // Find existing address with customer loaded
        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getIsActive()) {
            throw InactiveAddressException.forUpdate(addressId.toString());
        }

        // Validate version for optimistic locking
        if (!address.getVersion().equals(request.version())) {
            throw new BusinessRuleViolationException(
                    "Address has been modified by another user. Please refresh and try again.");
        }

        UUID customerId = address.getCustomer().getId();

        // Handle default address logic
        if (request.isBeingSetAsDefault()) {
            // Remove default flag from other addresses
            addressRepository.removeDefaultFlagForCustomer(customerId);
        } else if (request.hasDefaultUpdate() && !request.isDefault() && address.getIsDefault()) {
            // Trying to remove default flag - check if it's the only address
            long activeAddressCount = addressRepository.countByCustomerIdAndIsActiveTrue(customerId);
            if (activeAddressCount == 1) {
                throw LastActiveAddressException.forDefaultFlag();
            }
        }

        // Update address fields
        addressMapper.updateEntity(address, request);
        Address updatedAddress = addressRepository.save(address);

        log.info("Successfully updated address: {}", addressId);
        return addressMapper.toResponse(updatedAddress);
    }

    /**
     * Sets an address as the default address for a customer.
     * 
     * @param addressId  the address ID
     * @param customerId the customer ID (for security validation)
     * @return the updated address response
     * @throws IllegalArgumentException if address not found or doesn't belong to
     *                                  customer
     */
    @Transactional
    public AddressResponse setAsDefault(UUID addressId, UUID customerId) {
        log.info("Setting address {} as default for customer: {}", addressId, customerId);

        // Validate address exists and belongs to customer
        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw AddressOwnershipException.forCustomer(addressId.toString(), customerId.toString());
        }

        if (!address.getIsActive()) {
            throw InactiveAddressException.forSetDefault(addressId.toString());
        }

        // Remove default flag from other addresses and set this one as default
        addressRepository.removeDefaultFlagForCustomer(customerId);
        addressRepository.setAsDefault(addressId, customerId);

        // Refresh the address to get updated data
        Address updatedAddress = addressRepository.findById(addressId).orElseThrow();

        log.info("Successfully set address {} as default for customer: {}", addressId, customerId);
        return addressMapper.toResponse(updatedAddress);
    }

    // ================================================================
    // Address Deletion Operations
    // ================================================================

    /**
     * Soft deletes an address.
     * 
     * Business Rules:
     * - Cannot delete the only active address
     * - If deleting default address, sets another address as default
     * - Soft delete preserves data for audit purposes
     * 
     * @param addressId  the address ID
     * @param customerId the customer ID (for security validation)
     * @throws IllegalArgumentException if address not found or doesn't belong to
     *                                  customer
     * @throws IllegalStateException    if trying to delete the only address
     */
    @Transactional
    public void deleteAddress(UUID addressId, UUID customerId) {
        log.info("Deleting address: {} for customer: {}", addressId, customerId);

        // Validate address exists and belongs to customer
        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw AddressOwnershipException.forCustomer(addressId.toString(), customerId.toString());
        }

        // Check if this is the only active address
        long activeAddressCount = addressRepository.countByCustomerIdAndIsActiveTrue(customerId);
        if (activeAddressCount == 1) {
            throw LastActiveAddressException.forCustomer(customerId.toString());
        }

        boolean wasDefault = address.getIsDefault();

        // Soft delete the address
        addressRepository.softDeleteAddress(addressId, customerId);

        // If deleted address was default, set another address as default
        if (wasDefault) {
            setAnotherAddressAsDefault(customerId);
        }

        log.info("Successfully deleted address: {} for customer: {}", addressId, customerId);
    }

    /**
     * Reactivates a soft-deleted address.
     * 
     * @param addressId  the address ID
     * @param customerId the customer ID (for security validation)
     * @return the reactivated address response
     */
    @Transactional
    public AddressResponse reactivateAddress(UUID addressId, UUID customerId) {
        log.info("Reactivating address: {} for customer: {}", addressId, customerId);

        // Validate address exists and belongs to customer
        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw AddressOwnershipException.forCustomer(addressId.toString(), customerId.toString());
        }

        if (address.getIsActive()) {
            throw InactiveAddressException.alreadyActive(addressId.toString());
        }

        // Reactivate the address
        addressRepository.reactivateAddress(addressId, customerId);

        // If customer has no default address, make this one default
        if (!addressRepository.existsByCustomerIdAndIsDefaultTrueAndIsActiveTrue(customerId)) {
            addressRepository.setAsDefault(addressId, customerId);
        }

        // Refresh the address to get updated data
        Address updatedAddress = addressRepository.findById(addressId).orElseThrow();

        log.info("Successfully reactivated address: {} for customer: {}", addressId, customerId);
        return addressMapper.toResponse(updatedAddress);
    }

    // ================================================================
    // Address Search and Analytics
    // ================================================================

    /**
     * Finds addresses by city with pagination.
     * 
     * @param city     the city name
     * @param pageable pagination information
     * @return page of address responses
     */
    public Page<AddressResponse> findByCity(String city, Pageable pageable) {
        log.debug("Finding addresses by city: {}", city);

        Page<Address> addresses = addressRepository.findByCity(city, pageable);
        return addresses.map(addressMapper::toResponse);
    }

    /**
     * Finds addresses by state with pagination.
     * 
     * @param state    the state name
     * @param pageable pagination information
     * @return page of address responses
     */
    public Page<AddressResponse> findByState(String state, Pageable pageable) {
        log.debug("Finding addresses by state: {}", state);

        Page<Address> addresses = addressRepository.findByState(state, pageable);
        return addresses.map(addressMapper::toResponse);
    }

    /**
     * Finds addresses by postal code.
     * 
     * @param postalCode the postal code
     * @return list of address responses
     */
    public List<AddressResponse> findByPostalCode(String postalCode) {
        log.debug("Finding addresses by postal code: {}", postalCode);

        List<Address> addresses = addressRepository.findByPostalCodeAndIsActiveTrue(postalCode);
        return addressMapper.toResponseList(addresses);
    }

    /**
     * Finds addresses with delivery instructions.
     * 
     * @param pageable pagination information
     * @return page of addresses with delivery instructions
     */
    public Page<AddressResponse> findAddressesWithDeliveryInstructions(Pageable pageable) {
        log.debug("Finding addresses with delivery instructions");

        Page<Address> addresses = addressRepository.findAddressesWithDeliveryInstructions(pageable);
        return addresses.map(addressMapper::toResponse);
    }

    // ================================================================
    // Business Logic Helpers
    // ================================================================

    /**
     * Sets another address as default when the current default is deleted.
     * 
     * @param customerId the customer ID
     */
    private void setAnotherAddressAsDefault(UUID customerId) {
        List<Address> activeAddresses = addressRepository.findByCustomerIdAndIsActiveTrue(customerId);

        if (!activeAddresses.isEmpty()) {
            Address newDefault = activeAddresses.get(0); // Take the first active address
            addressRepository.setAsDefault(newDefault.getId(), customerId);
            log.info("Set address {} as new default for customer: {}", newDefault.getId(), customerId);
        }
    }

    /**
     * Validates if an address is suitable for delivery.
     * 
     * @param addressId the address ID
     * @return true if suitable for delivery
     */
    public boolean isSuitableForDelivery(UUID addressId) {
        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        return address.isSuitableForDelivery();
    }

    /**
     * Gets delivery-optimized address information.
     * 
     * @param addressId the address ID
     * @return delivery-optimized address response
     */
    public AddressResponse getDeliveryAddress(UUID addressId) {
        log.debug("Getting delivery address for: {}", addressId);

        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (!address.isSuitableForDelivery()) {
            throw new BusinessRuleViolationException("Address is not suitable for delivery: " + addressId);
        }

        return addressMapper.toDeliveryResponse(address);
    }

    /**
     * Counts addresses by type for a customer.
     * 
     * @param customerId the customer ID
     * @param type       the address type
     * @return count of addresses
     */
    public long countByCustomerIdAndType(UUID customerId, AddressType type) {
        return addressRepository.countByCustomerIdAndTypeAndIsActiveTrue(customerId, type);
    }

    /**
     * Checks if a customer has any addresses.
     * 
     * @param customerId the customer ID
     * @return true if customer has addresses
     */
    public boolean hasAddresses(UUID customerId) {
        return addressRepository.existsByCustomerIdAndIsActiveTrue(customerId);
    }

    // ================================================================
    // Authentication-based Methods for Controllers
    // ================================================================

    /**
     * Creates an address for the authenticated customer.
     *
     * @param request        the address creation request
     * @param authentication the authentication context
     * @return the created address response
     */
    @Transactional
    public AddressResponse createAddressForAuthenticatedCustomer(CreateAddressRequest request,
            Authentication authentication) {
        log.info("Creating address for authenticated customer: {}", authentication.getName());

        // Get customer ID from authentication
        UUID customerId = getCustomerIdFromAuthentication(authentication);

        return createAddress(customerId, request);
    }

    /**
     * Gets addresses for the authenticated customer.
     *
     * @param authentication the authentication context
     * @return list of customer addresses
     */
    public List<AddressResponse> getAddressesForAuthenticatedCustomer(Authentication authentication) {
        log.debug("Getting addresses for authenticated customer: {}", authentication.getName());

        UUID customerId = getCustomerIdFromAuthentication(authentication);
        return findByCustomerId(customerId);
    }

    /**
     * Gets an address by ID.
     *
     * @param addressId the address ID
     * @return the address response
     */
    public AddressResponse getAddressById(UUID addressId) {
        return findById(addressId);
    }

    /**
     * Sets an address as default (simplified version for controller).
     *
     * @param addressId the address ID
     * @return the updated address response
     */
    @Transactional
    public AddressResponse setAsDefaultAddress(UUID addressId) {
        log.info("Setting address as default: {}", addressId);

        // Find the address to get customer ID
        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        UUID customerId = address.getCustomer().getId();
        return setAsDefault(addressId, customerId);
    }

    /**
     * Deletes an address (simplified version for controller).
     *
     * @param addressId the address ID
     */
    @Transactional
    public void deleteAddress(UUID addressId) {
        log.info("Deleting address: {}", addressId);

        // Find the address to get customer ID
        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        UUID customerId = address.getCustomer().getId();
        deleteAddress(addressId, customerId);
    }

    /**
     * Reactivates an address (simplified version for controller).
     *
     * @param addressId the address ID
     * @return the reactivated address response
     */
    @Transactional
    public AddressResponse reactivateAddress(UUID addressId) {
        log.info("Reactivating address: {}", addressId);

        // Find the address to get customer ID
        Address address = addressRepository.findByIdWithCustomer(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        UUID customerId = address.getCustomer().getId();
        return reactivateAddress(addressId, customerId);
    }

    // ================================================================
    // Paginated and Search Methods
    // ================================================================

    /**
     * Gets addresses by customer ID with pagination.
     *
     * @param customerId the customer ID
     * @param pageable   pagination information
     * @return page of addresses
     */
    public Page<AddressResponse> getAddressesByCustomerId(UUID customerId, Pageable pageable) {
        log.debug("Getting addresses for customer: {} with pagination", customerId);

        Page<Address> addresses = addressRepository.findByCustomerId(customerId, pageable);
        return addresses.map(addressMapper::toResponse);
    }

    /**
     * Gets addresses by customer ID and type.
     *
     * @param customerId the customer ID
     * @param type       the address type
     * @return list of addresses
     */
    public List<AddressResponse> getAddressesByCustomerIdAndType(UUID customerId, AddressType type) {
        return findByCustomerIdAndType(customerId, type);
    }

    /**
     * Gets the default address for a customer.
     *
     * @param customerId the customer ID
     * @return the default address response
     */
    public AddressResponse getDefaultAddressByCustomerId(UUID customerId) {
        return getDefaultAddress(customerId);
    }

    /**
     * Gets addresses by city with pagination.
     *
     * @param city     the city name
     * @param pageable pagination information
     * @return page of addresses
     */
    public Page<AddressResponse> getAddressesByCity(String city, Pageable pageable) {
        return findByCity(city, pageable);
    }

    /**
     * Gets addresses by state with pagination.
     *
     * @param state    the state name
     * @param pageable pagination information
     * @return page of addresses
     */
    public Page<AddressResponse> getAddressesByState(String state, Pageable pageable) {
        return findByState(state, pageable);
    }

    /**
     * Gets addresses by postal code.
     *
     * @param postalCode the postal code
     * @return list of addresses
     */
    public List<AddressResponse> getAddressesByPostalCode(String postalCode) {
        return findByPostalCode(postalCode);
    }

    /**
     * Searches addresses by street address.
     *
     * @param streetAddress the street address to search
     * @param pageable      pagination information
     * @return page of matching addresses
     */
    public Page<AddressResponse> searchAddressesByStreet(String streetAddress, Pageable pageable) {
        log.debug("Searching addresses by street: {}", streetAddress);

        Page<Address> addresses = addressRepository.findByStreetAddressContainingIgnoreCase(streetAddress, pageable);
        return addresses.map(addressMapper::toResponse);
    }

    /**
     * Gets all addresses with pagination.
     *
     * @param pageable pagination information
     * @return page of all addresses
     */
    public Page<AddressResponse> getAllAddresses(Pageable pageable) {
        log.debug("Getting all addresses with pagination");

        Page<Address> addresses = addressRepository.findAll(pageable);
        return addresses.map(addressMapper::toResponse);
    }

    /**
     * Gets addresses with delivery instructions.
     *
     * @param pageable pagination information
     * @return page of addresses with delivery instructions
     */
    public Page<AddressResponse> getAddressesWithDeliveryInstructions(Pageable pageable) {
        return findAddressesWithDeliveryInstructions(pageable);
    }

    // ================================================================
    // Analytics and Statistics Methods
    // ================================================================

    /**
     * Gets address statistics by city.
     *
     * @return list of city statistics
     */
    public List<Object[]> getAddressStatisticsByCity() {
        log.debug("Getting address statistics by city");
        return addressRepository.countAddressesByCity();
    }

    /**
     * Gets address statistics by state.
     *
     * @return list of state statistics
     */
    public List<Object[]> getAddressStatisticsByState() {
        log.debug("Getting address statistics by state");
        return addressRepository.countAddressesByState();
    }

    // ================================================================
    // Validation Methods
    // ================================================================

    /**
     * Checks if a customer has any addresses.
     *
     * @param customerId the customer ID
     * @return true if customer has addresses
     */
    public boolean customerHasAddresses(UUID customerId) {
        return hasAddresses(customerId);
    }

    /**
     * Checks if a customer has a default address.
     *
     * @param customerId the customer ID
     * @return true if customer has a default address
     */
    public boolean customerHasDefaultAddress(UUID customerId) {
        return addressRepository.existsByCustomerIdAndIsDefaultTrueAndIsActiveTrue(customerId);
    }

    /**
     * Gets the count of active addresses for a customer.
     *
     * @param customerId the customer ID
     * @return count of active addresses
     */
    public long getCustomerAddressCount(UUID customerId) {
        return addressRepository.countByCustomerIdAndIsActiveTrue(customerId);
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Extracts customer ID from authentication context.
     *
     * @param authentication the authentication context
     * @return the customer ID
     * @throws IllegalArgumentException if customer not found
     */
    private UUID getCustomerIdFromAuthentication(Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getId();

        // Find customer by user ID
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer for user", userId));

        return customer.getId();
    }
}
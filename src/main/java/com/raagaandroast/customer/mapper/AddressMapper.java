package com.raagaandroast.customer.mapper;

import com.raagaandroast.customer.dto.AddressResponse;
import com.raagaandroast.customer.dto.CreateAddressRequest;
import com.raagaandroast.customer.dto.UpdateAddressRequest;
import com.raagaandroast.customer.entity.Address;
import com.raagaandroast.customer.entity.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Address entities and DTOs.
 * 
 * This mapper handles the conversion between Address entities and their
 * corresponding DTOs, ensuring proper data transformation and security
 * by excluding sensitive information from responses.
 * 
 * Design Decisions:
 * - Manual mapping for full control over data transformation
 * - Separate methods for different mapping scenarios
 * - Security-conscious mapping (includes customer context)
 * - Null-safe operations throughout
 * - Efficient collection processing
 * 
 * Performance Considerations:
 * - Efficient stream operations for collections
 * - Minimal object creation
 * - Lazy evaluation where possible
 * 
 * Interview Points:
 * - Why manual mapping? Control, security, performance
 * - Why separate mapper class? Single Responsibility Principle
 * - Why null checks? Defensive programming, robustness
 * - How to handle customer relationship? Security and context
 * 
 * @author RaagaAndRoast Development Team
 */
@Component
public class AddressMapper {

    // ================================================================
    // Entity to DTO Mapping
    // ================================================================

    /**
     * Converts an Address entity to AddressResponse DTO.
     * 
     * @param address the address entity
     * @return AddressResponse DTO
     */
    public AddressResponse toResponse(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressResponse(
                address.getId(),
                address.getCustomerId(),
                address.getType() != null ? address.getType().name() : null,
                address.getStreetAddress(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getDeliveryInstructions(),
                address.getIsDefault(),
                address.getIsActive(),
                address.getCreatedAt(),
                address.getUpdatedAt(),
                address.getVersion());
    }

    /**
     * Converts a list of Address entities to AddressResponse DTOs.
     * 
     * @param addresses the list of address entities
     * @return list of AddressResponse DTOs
     */
    public List<AddressResponse> toResponseList(List<Address> addresses) {
        if (addresses == null) {
            return List.of();
        }

        return addresses.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts an Address entity to AddressResponse DTO with minimal information.
     * Useful for summary views where full address details are not needed.
     * 
     * @param address the address entity
     * @return minimal AddressResponse DTO
     */
    public AddressResponse toSummaryResponse(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressResponse(
                address.getId(),
                address.getCustomerId(),
                address.getType() != null ? address.getType().name() : null,
                address.getStreetAddress(),
                null, // No address line 2 in summary
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                null, // No delivery instructions in summary
                address.getIsDefault(),
                address.getIsActive(),
                address.getCreatedAt(),
                address.getUpdatedAt(),
                address.getVersion());
    }

    // ================================================================
    // DTO to Entity Mapping
    // ================================================================

    /**
     * Converts CreateAddressRequest DTO to Address entity.
     * 
     * @param request  the create address request
     * @param customer the associated customer entity
     * @return Address entity
     */
    public Address toEntity(CreateAddressRequest request, Customer customer) {
        if (request == null) {
            return null;
        }

        return Address.builder()
                .customer(customer)
                .type(request.type())
                .streetAddress(request.streetAddress())
                .addressLine2(request.addressLine2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.getCountryOrDefault())
                .deliveryInstructions(request.deliveryInstructions())
                .isDefault(request.getIsDefaultOrDefault())
                .isActive(true) // New addresses are always active
                .build();
    }

    /**
     * Updates an existing Address entity with data from UpdateAddressRequest.
     * Only updates fields that are provided in the request.
     * 
     * @param address the existing address entity
     * @param request the update address request
     */
    public void updateEntity(Address address, UpdateAddressRequest request) {
        if (address == null || request == null) {
            return;
        }

        if (request.type() != null) {
            address.setType(request.type());
        }

        if (request.streetAddress() != null) {
            address.setStreetAddress(request.streetAddress());
        }

        if (request.addressLine2() != null) {
            address.setAddressLine2(request.addressLine2());
        }

        if (request.city() != null) {
            address.setCity(request.city());
        }

        if (request.state() != null) {
            address.setState(request.state());
        }

        if (request.postalCode() != null) {
            address.setPostalCode(request.postalCode());
        }

        if (request.country() != null) {
            address.setCountry(request.country());
        }

        if (request.deliveryInstructions() != null) {
            address.setDeliveryInstructions(request.deliveryInstructions());
        }

        if (request.isDefault() != null) {
            address.setIsDefault(request.isDefault());
        }

        if (request.isActive() != null) {
            address.setIsActive(request.isActive());
        }

        // Version is handled by JPA optimistic locking
    }

    // ================================================================
    // Utility Methods
    // ================================================================

    /**
     * Checks if an address entity has all required fields for response mapping.
     * 
     * @param address the address entity
     * @return true if address is valid for mapping
     */
    public boolean isValidForMapping(Address address) {
        return address != null &&
                address.getId() != null &&
                address.getStreetAddress() != null &&
                address.getCity() != null &&
                address.getState() != null &&
                address.getPostalCode() != null &&
                address.getCustomer() != null;
    }

    /**
     * Creates an AddressResponse with only public information.
     * Excludes sensitive data like delivery instructions.
     * 
     * @param address the address entity
     * @return public AddressResponse DTO
     */
    public AddressResponse toPublicResponse(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressResponse(
                address.getId(),
                address.getCustomerId(),
                address.getType() != null ? address.getType().name() : null,
                address.getStreetAddress(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                null, // No delivery instructions in public view
                address.getIsDefault(),
                address.getIsActive(),
                address.getCreatedAt(),
                null, // No update time in public view
                null // No version in public view
        );
    }

    /**
     * Maps addresses with specific filtering criteria.
     * 
     * @param addresses  the list of address entities
     * @param activeOnly whether to include only active addresses
     * @return filtered list of AddressResponse DTOs
     */
    public List<AddressResponse> toResponseList(List<Address> addresses, boolean activeOnly) {
        if (addresses == null) {
            return List.of();
        }

        return addresses.stream()
                .filter(address -> !activeOnly || (address.getIsActive() != null && address.getIsActive()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets the default address from a list of addresses.
     * 
     * @param addresses the list of address entities
     * @return the default address response or null if none found
     */
    public AddressResponse getDefaultAddress(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        return addresses.stream()
                .filter(address -> address.getIsDefault() != null && address.getIsDefault())
                .filter(address -> address.getIsActive() != null && address.getIsActive())
                .findFirst()
                .map(this::toResponse)
                .orElse(null);
    }

    /**
     * Filters addresses by type and converts to DTOs.
     * 
     * @param addresses the list of address entities
     * @param type      the address type to filter by
     * @return filtered list of AddressResponse DTOs
     */
    public List<AddressResponse> getAddressesByType(List<Address> addresses, String type) {
        if (addresses == null || type == null) {
            return List.of();
        }

        return addresses.stream()
                .filter(address -> address.getType() != null &&
                        address.getType().name().equalsIgnoreCase(type))
                .filter(address -> address.getIsActive() != null && address.getIsActive())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a delivery-optimized address response.
     * Includes only information needed for delivery.
     * 
     * @param address the address entity
     * @return delivery-optimized AddressResponse DTO
     */
    public AddressResponse toDeliveryResponse(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressResponse(
                address.getId(),
                address.getCustomerId(),
                address.getType() != null ? address.getType().name() : null,
                address.getStreetAddress(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getDeliveryInstructions(), // Include for delivery
                address.getIsDefault(),
                address.getIsActive(),
                null, // No creation time for delivery
                null, // No update time for delivery
                null // No version for delivery
        );
    }
}
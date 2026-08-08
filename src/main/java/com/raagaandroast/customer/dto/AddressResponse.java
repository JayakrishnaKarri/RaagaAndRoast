package com.raagaandroast.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for address information.
 * 
 * This DTO represents address data returned by the API.
 * It includes all address information that should be visible
 * to customers and authorized users.
 * 
 * Design Decisions:
 * - Immutable record for thread safety
 * - Includes all address fields for complete view
 * - JSON formatting for consistent API responses
 * - Customer ID included for context
 * 
 * Security Considerations:
 * - Only returns data the customer should see
 * - Customer ID helps with authorization checks
 * - Version included for optimistic locking
 * 
 * Interview Points:
 * - Why record? Immutability, less boilerplate, value semantics
 * - Why include customer ID? Authorization context, API convenience
 * - Why full address? Complete address management functionality
 * 
 * @author RaagaAndRoast Development Team
 */
public record AddressResponse(
        UUID id,
        UUID customerId,
        String type,
        String streetAddress,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        String deliveryInstructions,
        Boolean isDefault,
        Boolean isActive,
        String fullAddress,
        String shortAddress,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt,

        Long version) {

    /**
     * Creates an AddressResponse with calculated fields.
     * This constructor calculates derived fields like fullAddress and shortAddress.
     * 
     * @param id                   address ID
     * @param customerId           customer ID
     * @param type                 address type
     * @param streetAddress        street address
     * @param addressLine2         address line 2
     * @param city                 city
     * @param state                state
     * @param postalCode           postal code
     * @param country              country
     * @param deliveryInstructions delivery instructions
     * @param isDefault            default flag
     * @param isActive             active flag
     * @param createdAt            creation timestamp
     * @param updatedAt            last update timestamp
     * @param version              optimistic locking version
     */
    public AddressResponse(UUID id, UUID customerId, String type, String streetAddress,
            String addressLine2, String city, String state, String postalCode,
            String country, String deliveryInstructions, Boolean isDefault,
            Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt,
            Long version) {
        this(
                id,
                customerId,
                type,
                streetAddress,
                addressLine2,
                city,
                state,
                postalCode,
                country,
                deliveryInstructions,
                isDefault,
                isActive,
                calculateFullAddress(streetAddress, addressLine2, city, state, postalCode, country),
                calculateShortAddress(streetAddress, city, state, postalCode),
                createdAt,
                updatedAt,
                version);
    }

    /**
     * Calculates the full address as a formatted string.
     * 
     * @param streetAddress street address
     * @param addressLine2  address line 2
     * @param city          city
     * @param state         state
     * @param postalCode    postal code
     * @param country       country
     * @return formatted full address
     */
    private static String calculateFullAddress(String streetAddress, String addressLine2,
            String city, String state, String postalCode,
            String country) {
        StringBuilder address = new StringBuilder();

        if (streetAddress != null) {
            address.append(streetAddress);
        }

        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            address.append(", ").append(addressLine2);
        }

        if (city != null) {
            address.append(", ").append(city);
        }

        if (state != null) {
            address.append(", ").append(state);
        }

        if (postalCode != null) {
            address.append(" ").append(postalCode);
        }

        if (country != null && !country.trim().isEmpty()) {
            address.append(", ").append(country);
        }

        return address.toString();
    }

    /**
     * Calculates the short address format.
     * 
     * @param streetAddress street address
     * @param city          city
     * @param state         state
     * @param postalCode    postal code
     * @return formatted short address
     */
    private static String calculateShortAddress(String streetAddress, String city,
            String state, String postalCode) {
        StringBuilder address = new StringBuilder();

        if (streetAddress != null) {
            address.append(streetAddress);
        }

        if (city != null) {
            address.append(", ").append(city);
        }

        if (state != null) {
            address.append(", ").append(state);
        }

        if (postalCode != null) {
            address.append(" ").append(postalCode);
        }

        return address.toString();
    }

    /**
     * Checks if this address has delivery instructions.
     * 
     * @return true if delivery instructions are present
     */
    public boolean hasDeliveryInstructions() {
        return deliveryInstructions != null && !deliveryInstructions.trim().isEmpty();
    }

    /**
     * Checks if this address is the default address.
     *
     * @return true if this is the default address
     */
    public boolean isDefaultAddress() {
        return isDefault != null && isDefault;
    }

    /**
     * Checks if this address is active.
     *
     * @return true if this address is active
     */
    public boolean isActiveAddress() {
        return isActive != null && isActive;
    }

    /**
     * Checks if this address is suitable for delivery.
     *
     * @return true if the address is active and has required fields
     */
    public boolean isSuitableForDelivery() {
        return isActiveAddress() &&
                streetAddress != null && !streetAddress.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                state != null && !state.trim().isEmpty() &&
                postalCode != null && !postalCode.trim().isEmpty();
    }

    /**
     * Gets the address type as an enum.
     * 
     * @return the address type enum or null if invalid
     */
    public com.raagaandroast.customer.entity.AddressType getAddressTypeEnum() {
        try {
            return com.raagaandroast.customer.entity.AddressType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
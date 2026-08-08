package com.raagaandroast.customer.dto;

import com.raagaandroast.customer.entity.AddressType;
import jakarta.validation.constraints.*;

/**
 * Request DTO for updating an existing address.
 * 
 * This DTO allows customers to update their address information.
 * All fields are optional to support partial updates, but validation
 * is applied when fields are provided.
 * 
 * Design Decisions:
 * - All fields optional for partial updates
 * - Same validation rules as creation when fields are provided
 * - Version field for optimistic locking
 * - No customer ID (derived from authentication and address ownership)
 * 
 * Security Considerations:
 * - Address ownership verified through authorization
 * - Validation prevents malicious input
 * - Version field prevents concurrent modification issues
 * 
 * Interview Points:
 * - Why separate update DTO? Different validation rules, optional fields
 * - Why version field? Optimistic locking, concurrent update prevention
 * - Why no customer ID? Security, ownership verification through authorization
 * 
 * @author RaagaAndRoast Development Team
 */
public record UpdateAddressRequest(

        AddressType type,

        @Size(min = 5, max = 100, message = "Street address must be between 5 and 100 characters") String streetAddress,

        @Size(max = 100, message = "Address line 2 must not exceed 100 characters") String addressLine2,

        @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters") String city,

        @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters") String state,

        @Size(min = 3, max = 20, message = "Postal code must be between 3 and 20 characters") String postalCode,

        @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters") String country,

        @Size(max = 500, message = "Delivery instructions must not exceed 500 characters") String deliveryInstructions,

        Boolean isDefault,

        Boolean isActive,

        @NotNull(message = "Version is required for optimistic locking") @Min(value = 0, message = "Version must be non-negative") Long version) {

    /**
     * Checks if any field is provided for update.
     * 
     * @return true if at least one field is provided
     */
    public boolean hasAnyUpdate() {
        return type != null || streetAddress != null || addressLine2 != null ||
                city != null || state != null || postalCode != null ||
                country != null || deliveryInstructions != null ||
                isDefault != null || isActive != null;
    }

    /**
     * Checks if address location fields are being updated.
     * 
     * @return true if any location field is provided
     */
    public boolean hasLocationUpdate() {
        return streetAddress != null || addressLine2 != null ||
                city != null || state != null || postalCode != null || country != null;
    }

    /**
     * Checks if address type is being updated.
     * 
     * @return true if address type is provided
     */
    public boolean hasTypeUpdate() {
        return type != null;
    }

    /**
     * Checks if delivery instructions are being updated.
     * 
     * @return true if delivery instructions are provided
     */
    public boolean hasDeliveryInstructionsUpdate() {
        return deliveryInstructions != null;
    }

    /**
     * Checks if default flag is being updated.
     * 
     * @return true if default flag is provided
     */
    public boolean hasDefaultUpdate() {
        return isDefault != null;
    }

    /**
     * Checks if active flag is being updated.
     * 
     * @return true if active flag is provided
     */
    public boolean hasActiveUpdate() {
        return isActive != null;
    }

    /**
     * Gets the address type as a string.
     * 
     * @return address type string or null if not provided
     */
    public String getTypeAsString() {
        return type != null ? type.name() : null;
    }

    /**
     * Checks if the address is being set as default.
     * 
     * @return true if being set as default
     */
    public boolean isBeingSetAsDefault() {
        return isDefault != null && isDefault;
    }

    /**
     * Checks if the address is being deactivated.
     * 
     * @return true if being deactivated
     */
    public boolean isBeingDeactivated() {
        return isActive != null && !isActive;
    }
}
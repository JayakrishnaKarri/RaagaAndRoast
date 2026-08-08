package com.raagaandroast.customer.dto;

import com.raagaandroast.customer.entity.AddressType;
import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a new address.
 * 
 * This DTO is used when customers want to add a new address
 * to their profile. It contains all the necessary information
 * to create a complete address record.
 * 
 * Design Decisions:
 * - All required fields have validation
 * - Address type is required and validated
 * - Optional fields for enhanced functionality
 * - Default values provided where appropriate
 * 
 * Security Considerations:
 * - No customer ID in request (derived from authentication)
 * - Validation prevents malicious input
 * - Size limits prevent DoS attacks
 * 
 * Interview Points:
 * - Why no customer ID? Security, derived from authentication context
 * - Why validation annotations? Input sanitization, business rules
 * - Why optional delivery instructions? User experience, flexibility
 * 
 * @author RaagaAndRoast Development Team
 */
public record CreateAddressRequest(

        @NotNull(message = "Address type is required") AddressType type,

        @NotBlank(message = "Street address is required") @Size(min = 5, max = 100, message = "Street address must be between 5 and 100 characters") String streetAddress,

        @Size(max = 100, message = "Address line 2 must not exceed 100 characters") String addressLine2,

        @NotBlank(message = "City is required") @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters") String city,

        @NotBlank(message = "State is required") @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters") String state,

        @NotBlank(message = "Postal code is required") @Size(min = 3, max = 20, message = "Postal code must be between 3 and 20 characters") String postalCode,

        @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters") String country,

        @Size(max = 500, message = "Delivery instructions must not exceed 500 characters") String deliveryInstructions,

        Boolean isDefault) {

    /**
     * Gets the country with a default value.
     * 
     * @return country, defaulting to "India" if null or empty
     */
    public String getCountryOrDefault() {
        return (country != null && !country.trim().isEmpty()) ? country : "India";
    }

    /**
     * Gets the default flag with a default value.
     * 
     * @return default flag, defaulting to false if null
     */
    public boolean getIsDefaultOrDefault() {
        return isDefault != null ? isDefault : false;
    }

    /**
     * Checks if delivery instructions are provided.
     * 
     * @return true if delivery instructions are present
     */
    public boolean hasDeliveryInstructions() {
        return deliveryInstructions != null && !deliveryInstructions.trim().isEmpty();
    }

    /**
     * Gets the address type as a string.
     * 
     * @return address type string
     */
    public String getTypeAsString() {
        return type != null ? type.name() : null;
    }
}
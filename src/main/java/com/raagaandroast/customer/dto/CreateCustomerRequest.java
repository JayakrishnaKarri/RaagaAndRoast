package com.raagaandroast.customer.dto;

import com.raagaandroast.customer.entity.AddressType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request DTO for creating a new customer profile.
 * 
 * This DTO is used when a user wants to create their customer profile
 * after registration. It contains all the necessary information to
 * create a complete customer record with optional address information.
 * 
 * Design Decisions:
 * - Separate from user registration to follow single responsibility
 * - Comprehensive validation for all fields
 * - Optional address creation in the same request
 * - Phone number validation with international support
 * - Date validation for reasonable birth dates
 * 
 * Security Considerations:
 * - No sensitive information exposed
 * - Validation prevents malicious input
 * - Size limits prevent DoS attacks
 * 
 * Interview Points:
 * - Why separate DTO? API contract stability, validation separation
 * - Why validation annotations? Input sanitization, business rules
 * - Why optional address? User experience, progressive data collection
 * 
 * @author RaagaAndRoast Development Team
 */
public record CreateCustomerRequest(

        @NotBlank(message = "First name is required") @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes") String firstName,

        @NotBlank(message = "Last name is required") @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes") String lastName,

        @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4,6}$", message = "Phone number must be valid (e.g., +1-555-123-4567 or 555-123-4567)") String phoneNumber,

        @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,

        @Size(max = 1000, message = "Preferences must not exceed 1000 characters") String preferences,

        Boolean marketingConsent,

        // Optional address information
        AddressType addressType,

        @Size(min = 5, max = 100, message = "Street address must be between 5 and 100 characters") String streetAddress,

        @Size(max = 100, message = "Address line 2 must not exceed 100 characters") String addressLine2,

        @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters") String city,

        @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters") String state,

        @Size(min = 3, max = 20, message = "Postal code must be between 3 and 20 characters") String postalCode,

        @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters") String country,

        @Size(max = 500, message = "Delivery instructions must not exceed 500 characters") String deliveryInstructions) {

    /**
     * Checks if address information is provided.
     * 
     * @return true if at least street address and city are provided
     */
    public boolean hasAddressInfo() {
        return streetAddress != null && !streetAddress.trim().isEmpty() &&
                city != null && !city.trim().isEmpty();
    }

    /**
     * Gets the marketing consent with a default value.
     * 
     * @return marketing consent, defaulting to false if null
     */
    public boolean getMarketingConsentOrDefault() {
        return marketingConsent != null ? marketingConsent : false;
    }

    /**
     * Gets the address type with a default value.
     * 
     * @return address type, defaulting to HOME if null
     */
    public AddressType getAddressTypeOrDefault() {
        return addressType != null ? addressType : AddressType.HOME;
    }

    /**
     * Gets the country with a default value.
     * 
     * @return country, defaulting to "India" if null or empty
     */
    public String getCountryOrDefault() {
        return (country != null && !country.trim().isEmpty()) ? country : "India";
    }
}
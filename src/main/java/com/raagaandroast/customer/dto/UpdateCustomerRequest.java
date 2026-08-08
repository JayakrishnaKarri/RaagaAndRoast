package com.raagaandroast.customer.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing customer profile.
 * 
 * This DTO allows customers to update their profile information.
 * All fields are optional to support partial updates, but validation
 * is applied when fields are provided.
 * 
 * Design Decisions:
 * - All fields optional for partial updates
 * - Same validation rules as creation when fields are provided
 * - No user relationship updates (handled separately)
 * - Version field for optimistic locking
 * 
 * Security Considerations:
 * - Customers can only update their own profiles
 * - No sensitive information exposed
 * - Validation prevents malicious input
 * 
 * Interview Points:
 * - Why separate update DTO? Different validation rules, optional fields
 * - Why version field? Optimistic locking, concurrent update prevention
 * - Why no user updates? Separation of concerns, different security rules
 * 
 * @author RaagaAndRoast Development Team
 */
public record UpdateCustomerRequest(

        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes") String firstName,

        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes") String lastName,

        @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4,6}$", message = "Phone number must be valid (e.g., +1-555-123-4567 or 555-123-4567)") String phoneNumber,

        @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,

        @Size(max = 1000, message = "Preferences must not exceed 1000 characters") String preferences,

        Boolean marketingConsent,

        @NotNull(message = "Version is required for optimistic locking") @Min(value = 0, message = "Version must be non-negative") Long version) {

    /**
     * Checks if any field is provided for update.
     * 
     * @return true if at least one field is provided
     */
    public boolean hasAnyUpdate() {
        return firstName != null || lastName != null || phoneNumber != null ||
                dateOfBirth != null || preferences != null || marketingConsent != null;
    }

    /**
     * Checks if name fields are being updated.
     * 
     * @return true if first name or last name is provided
     */
    public boolean hasNameUpdate() {
        return firstName != null || lastName != null;
    }

    /**
     * Checks if contact information is being updated.
     * 
     * @return true if phone number is provided
     */
    public boolean hasContactUpdate() {
        return phoneNumber != null;
    }

    /**
     * Checks if preferences are being updated.
     * 
     * @return true if preferences or marketing consent is provided
     */
    public boolean hasPreferencesUpdate() {
        return preferences != null || marketingConsent != null;
    }
}
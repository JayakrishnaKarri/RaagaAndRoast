package com.raagaandroast.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for customer information.
 * 
 * This DTO represents customer data returned by the API.
 * It includes customer profile information and basic user details
 * but excludes sensitive information like passwords.
 * 
 * Design Decisions:
 * - Immutable record for thread safety
 * - Includes user information for convenience
 * - Excludes sensitive data (passwords, internal IDs)
 * - Includes addresses for complete customer view
 * - JSON formatting for consistent API responses
 * 
 * Security Considerations:
 * - No password or sensitive user data exposed
 * - Only returns data the customer should see
 * - Version included for optimistic locking
 * 
 * Interview Points:
 * - Why record? Immutability, less boilerplate, value semantics
 * - Why include user data? API convenience, reduce round trips
 * - Why exclude passwords? Security, principle of least privilege
 * - Why include addresses? Complete customer context
 * 
 * @author RaagaAndRoast Development Team
 */
public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String phoneNumber,

        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth,

        String preferences,
        Boolean marketingConsent,

        // User information (non-sensitive)
        UUID userId,
        String username,
        String email,
        Boolean userEnabled,

        // Address information
        List<AddressResponse> addresses,
        Integer addressCount,
        AddressResponse primaryAddress,

        // Audit information
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt,

        Long version) {

    /**
     * Creates a CustomerResponse with calculated fields.
     * This constructor calculates derived fields like fullName and addressCount.
     * 
     * @param id               customer ID
     * @param firstName        customer first name
     * @param lastName         customer last name
     * @param phoneNumber      customer phone number
     * @param dateOfBirth      customer date of birth
     * @param preferences      customer preferences
     * @param marketingConsent marketing consent status
     * @param userId           associated user ID
     * @param username         associated username
     * @param email            associated email
     * @param userEnabled      user enabled status
     * @param addresses        list of customer addresses
     * @param createdAt        creation timestamp
     * @param updatedAt        last update timestamp
     * @param version          optimistic locking version
     */
    public CustomerResponse(UUID id, String firstName, String lastName, String phoneNumber,
            LocalDate dateOfBirth, String preferences, Boolean marketingConsent,
            UUID userId, String username, String email, Boolean userEnabled,
            List<AddressResponse> addresses, LocalDateTime createdAt,
            LocalDateTime updatedAt, Long version) {
        this(
                id,
                firstName,
                lastName,
                calculateFullName(firstName, lastName),
                phoneNumber,
                dateOfBirth,
                preferences,
                marketingConsent,
                userId,
                username,
                email,
                userEnabled,
                addresses != null ? addresses : List.of(),
                addresses != null ? addresses.size() : 0,
                findPrimaryAddress(addresses),
                createdAt,
                updatedAt,
                version);
    }

    /**
     * Calculates the full name from first and last names.
     * 
     * @param firstName the first name
     * @param lastName  the last name
     * @return the full name
     */
    private static String calculateFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Finds the primary address from the list of addresses.
     * 
     * @param addresses the list of addresses
     * @return the primary address or null if none found
     */
    private static AddressResponse findPrimaryAddress(List<AddressResponse> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        // Look for default address first
        return addresses.stream()
                .filter(AddressResponse::isDefaultAddress)
                .findFirst()
                .orElse(addresses.get(0)); // Fall back to first address
    }

    /**
     * Checks if the customer has any addresses.
     * 
     * @return true if the customer has at least one address
     */
    public boolean hasAddresses() {
        return addressCount > 0;
    }

    /**
     * Checks if the customer has a primary address.
     * 
     * @return true if a primary address exists
     */
    public boolean hasPrimaryAddress() {
        return primaryAddress != null;
    }

    /**
     * Gets addresses by type.
     * 
     * @param type the address type to filter by
     * @return list of addresses of the specified type
     */
    public List<AddressResponse> getAddressesByType(String type) {
        return addresses.stream()
                .filter(address -> type.equalsIgnoreCase(address.type()))
                .toList();
    }

    /**
     * Checks if the customer has marketing consent.
     * 
     * @return true if marketing consent is given
     */
    public boolean hasMarketingConsent() {
        return marketingConsent != null && marketingConsent;
    }

    /**
     * Checks if the customer's user account is enabled.
     * 
     * @return true if the user account is enabled
     */
    public boolean isUserEnabled() {
        return userEnabled != null && userEnabled;
    }

    /**
     * Gets the customer's age based on date of birth.
     * 
     * @return the age in years, or null if date of birth is not set
     */
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Checks if today is the customer's birthday.
     * 
     * @return true if today is the customer's birthday
     */
    public boolean isBirthdayToday() {
        if (dateOfBirth == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return dateOfBirth.getMonth() == today.getMonth() &&
                dateOfBirth.getDayOfMonth() == today.getDayOfMonth();
    }
}
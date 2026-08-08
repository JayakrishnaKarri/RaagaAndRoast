package com.raagaandroast.customer.entity;

/**
 * Enumeration for different types of addresses.
 * 
 * This enum defines the various address types that customers can have
 * in the RaagaAndRoast system. It supports multiple delivery locations
 * and helps organize customer address information.
 * 
 * Design Decisions:
 * - Simple enum for type safety and validation
 * - Common address types for delivery services
 * - Extensible design for future address types
 * - Clear naming convention
 * 
 * Interview Points:
 * - Why enum vs String? Type safety, validation, IDE support
 * - Why not database table? Simple, stable set of values
 * - How does this support business logic? Delivery rules, validation
 * 
 * @author RaagaAndRoast Development Team
 */
public enum AddressType {

    /**
     * Home address - primary residence.
     * Typically used for personal deliveries and default shipping.
     */
    HOME("Home"),

    /**
     * Work address - office or workplace.
     * Used for business deliveries during work hours.
     */
    WORK("Work"),

    /**
     * Other address - any other location.
     * Used for temporary addresses, gifts, or special locations.
     */
    OTHER("Other");

    private final String displayName;

    /**
     * Constructor for AddressType enum.
     * 
     * @param displayName the human-readable name for the address type
     */
    AddressType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name for the address type.
     * Used in UI components and API responses.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the enum value from a string representation.
     * Provides case-insensitive lookup for API flexibility.
     * 
     * @param value the string value
     * @return the corresponding AddressType
     * @throws IllegalArgumentException if the value is not valid
     */
    public static AddressType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Address type cannot be null");
        }

        for (AddressType type : AddressType.values()) {
            if (type.name().equalsIgnoreCase(value) ||
                    type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid address type: " + value);
    }

    /**
     * Checks if the given string is a valid address type.
     * 
     * @param value the string value to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
package com.raagaandroast.menu.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating and updating menu items.
 * 
 * This DTO demonstrates:
 * - Advanced BigDecimal validation for monetary values
 * - Complex business rule validation
 * - Proper handling of optional fields
 * - Custom validation logic for dietary preferences
 * - Production-quality input sanitization
 * 
 * Key Learning Points:
 * - Never use double for money - always BigDecimal
 * - Validate precision and scale for monetary values
 * - Handle null values gracefully in business logic
 * - Separate validation concerns from business logic
 * 
 * @author RaagaAndRoast Development Team
 */
public record MenuItemRequest(

        @NotBlank(message = "Menu item name is required") @Size(min = 2, max = 100, message = "Menu item name must be between 2 and 100 characters") String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters") String description,

        @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be greater than 0") @DecimalMax(value = "999.99", message = "Price cannot exceed 999.99") @Digits(integer = 3, fraction = 2, message = "Price must have at most 3 digits before decimal and 2 after") BigDecimal price,

        @NotNull(message = "Category ID is required") UUID categoryId,

        Boolean available,

        @Min(value = 1, message = "Preparation time must be at least 1 minute") @Max(value = 120, message = "Preparation time cannot exceed 120 minutes") Integer preparationTimeMinutes,

        // Dietary preference flags
        Boolean vegetarian,
        Boolean vegan,
        Boolean glutenFree,
        Boolean spicy,

        @Min(value = 0, message = "Spice level cannot be negative") @Max(value = 5, message = "Spice level cannot exceed 5") Integer spiceLevel

) {

    /**
     * Constructor with default values for optional fields.
     * Demonstrates proper handling of optional parameters with business defaults.
     */
    public MenuItemRequest {
        // Set default values for optional fields
        if (available == null) {
            available = true;
        }
        if (preparationTimeMinutes == null) {
            preparationTimeMinutes = 15; // Default 15 minutes
        }
        if (vegetarian == null) {
            vegetarian = false;
        }
        if (vegan == null) {
            vegan = false;
        }
        if (glutenFree == null) {
            glutenFree = false;
        }
        if (spicy == null) {
            spicy = false;
        }
        if (spiceLevel == null) {
            spiceLevel = 0; // Not spicy by default
        }

        // Business rule: vegan items are automatically vegetarian
        if (vegan != null && vegan && (vegetarian == null || !vegetarian)) {
            vegetarian = true;
        }

        // Business rule: if spice level > 0, item should be marked as spicy
        if (spiceLevel != null && spiceLevel > 0 && (spicy == null || !spicy)) {
            spicy = true;
        }
    }

    /**
     * Factory method for creating a basic menu item request.
     */
    public static MenuItemRequest of(String name, String description, BigDecimal price, UUID categoryId) {
        return new MenuItemRequest(name, description, price, categoryId,
                true, 15, false, false, false, false, 0);
    }

    /**
     * Factory method for creating a menu item with dietary preferences.
     */
    public static MenuItemRequest withDietaryInfo(String name, String description, BigDecimal price,
            UUID categoryId, boolean vegetarian, boolean vegan,
            boolean glutenFree, boolean spicy, int spiceLevel) {
        return new MenuItemRequest(name, description, price, categoryId, true, 15,
                vegetarian, vegan, glutenFree, spicy, spiceLevel);
    }

    /**
     * Validates complex business rules beyond basic validation annotations.
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
                price != null && price.compareTo(BigDecimal.ZERO) > 0 &&
                categoryId != null &&
                (preparationTimeMinutes == null || preparationTimeMinutes > 0) &&
                (spiceLevel == null || (spiceLevel >= 0 && spiceLevel <= 5)) &&
                isDietaryInfoConsistent();
    }

    /**
     * Validates dietary information consistency.
     * Business rule: vegan items must be vegetarian.
     */
    public boolean isDietaryInfoConsistent() {
        // If vegan, must be vegetarian
        if (Boolean.TRUE.equals(vegan) && !Boolean.TRUE.equals(vegetarian)) {
            return false;
        }

        // If spice level > 0, should be marked as spicy
        if (spiceLevel != null && spiceLevel > 0 && !Boolean.TRUE.equals(spicy)) {
            return false;
        }

        return true;
    }

    /**
     * Returns a cleaned version of the name.
     */
    public String getCleanName() {
        return name != null ? name.trim() : null;
    }

    /**
     * Returns a cleaned version of the description.
     */
    public String getCleanDescription() {
        return description != null && !description.trim().isEmpty() ? description.trim() : null;
    }

    /**
     * Returns the price in cents for precise calculations.
     * Useful for payment processing and avoiding floating-point errors.
     */
    public long getPriceInCents() {
        return price != null ? price.multiply(new BigDecimal("100")).longValue() : 0L;
    }

    /**
     * Checks if this menu item has any dietary restrictions.
     */
    public boolean hasDietaryRestrictions() {
        return Boolean.TRUE.equals(vegetarian) ||
                Boolean.TRUE.equals(vegan) ||
                Boolean.TRUE.equals(glutenFree);
    }

    /**
     * Returns a summary of dietary preferences as a string.
     */
    public String getDietarySummary() {
        var summary = new StringBuilder();

        if (Boolean.TRUE.equals(vegan)) {
            summary.append("Vegan, ");
        } else if (Boolean.TRUE.equals(vegetarian)) {
            summary.append("Vegetarian, ");
        }

        if (Boolean.TRUE.equals(glutenFree)) {
            summary.append("Gluten-Free, ");
        }

        if (Boolean.TRUE.equals(spicy)) {
            summary.append("Spicy (Level ").append(spiceLevel != null ? spiceLevel : 1).append("), ");
        }

        String result = summary.toString();
        return result.isEmpty() ? "No special dietary notes" : result.substring(0, result.length() - 2); // Remove
                                                                                                         // trailing
                                                                                                         // comma
    }
}
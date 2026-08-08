package com.raagaandroast.menu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for MenuItem entities.
 * 
 * This DTO demonstrates:
 * - Proper BigDecimal serialization for monetary values
 * - Clean separation of entity structure from API responses
 * - Nested category information for convenience
 * - Computed fields for enhanced API usability
 * - Conditional field inclusion for payload optimization
 * - Comprehensive dietary information presentation
 * 
 * Key Learning Points:
 * - Always use BigDecimal for monetary values in APIs
 * - Include related entity summaries to reduce API calls
 * - Provide computed convenience fields
 * - Handle null values gracefully in response DTOs
 * 
 * @author RaagaAndRoast Development Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuItemResponse(

        UUID id,

        String name,

        String description,

        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal price,

        Boolean available,

        Integer preparationTimeMinutes,

        // Dietary information
        Boolean vegetarian,
        Boolean vegan,
        Boolean glutenFree,
        Boolean spicy,
        Integer spiceLevel,

        // Category information (nested for convenience)
        CategoryResponse.CategorySummary category,

        // Audit information
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt,

        Long version,

        // Computed convenience fields
        String formattedPrice,
        String dietarySummary,
        Boolean hasSpecialDiet,
        String preparationTimeDisplay

) {

    /**
     * Factory method for creating a basic menu item response.
     */
    public static MenuItemResponse of(UUID id, String name, String description, BigDecimal price,
            Boolean available, Integer preparationTimeMinutes,
            Boolean vegetarian, Boolean vegan, Boolean glutenFree,
            Boolean spicy, Integer spiceLevel,
            CategoryResponse.CategorySummary category,
            LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {

        // Compute convenience fields
        String formattedPrice = formatPrice(price);
        String dietarySummary = computeDietarySummary(vegetarian, vegan, glutenFree, spicy, spiceLevel);
        Boolean hasSpecialDiet = computeHasSpecialDiet(vegetarian, vegan, glutenFree);
        String preparationTimeDisplay = formatPreparationTime(preparationTimeMinutes);

        return new MenuItemResponse(id, name, description, price, available, preparationTimeMinutes,
                vegetarian, vegan, glutenFree, spicy, spiceLevel, category,
                createdAt, updatedAt, version,
                formattedPrice, dietarySummary, hasSpecialDiet, preparationTimeDisplay);
    }

    /**
     * Factory method for creating a menu item response without computed fields.
     * Useful when computed fields are not needed or will be calculated separately.
     */
    public static MenuItemResponse basic(UUID id, String name, String description, BigDecimal price,
            Boolean available, Integer preparationTimeMinutes,
            Boolean vegetarian, Boolean vegan, Boolean glutenFree,
            Boolean spicy, Integer spiceLevel,
            CategoryResponse.CategorySummary category,
            LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {

        return new MenuItemResponse(id, name, description, price, available, preparationTimeMinutes,
                vegetarian, vegan, glutenFree, spicy, spiceLevel, category,
                createdAt, updatedAt, version,
                null, null, null, null);
    }

    /**
     * Returns a summary version of this menu item for use in nested responses.
     */
    public MenuItemSummary toSummary() {
        return new MenuItemSummary(id, name, price, available,
                Boolean.TRUE.equals(vegetarian),
                Boolean.TRUE.equals(vegan),
                Boolean.TRUE.equals(glutenFree));
    }

    /**
     * Checks if this menu item is orderable (available and has valid data).
     */
    public boolean isOrderable() {
        return Boolean.TRUE.equals(available) &&
                name != null && !name.trim().isEmpty() &&
                price != null && price.compareTo(BigDecimal.ZERO) > 0 &&
                category != null && Boolean.TRUE.equals(category.active());
    }

    /**
     * Returns the price in cents for precise calculations.
     */
    public long getPriceInCents() {
        return price != null ? price.multiply(new BigDecimal("100")).longValue() : 0L;
    }

    /**
     * Formats the price as a currency string.
     */
    private static String formatPrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        return String.format("₹%.2f", price);
    }

    /**
     * Computes a human-readable dietary summary.
     */
    private static String computeDietarySummary(Boolean vegetarian, Boolean vegan, Boolean glutenFree,
            Boolean spicy, Integer spiceLevel) {
        var summary = new StringBuilder();

        if (Boolean.TRUE.equals(vegan)) {
            summary.append("Vegan");
        } else if (Boolean.TRUE.equals(vegetarian)) {
            summary.append("Vegetarian");
        }

        if (Boolean.TRUE.equals(glutenFree)) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("Gluten-Free");
        }

        if (Boolean.TRUE.equals(spicy)) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("Spicy");
            if (spiceLevel != null && spiceLevel > 0) {
                summary.append(" (Level ").append(spiceLevel).append(")");
            }
        }

        return summary.length() > 0 ? summary.toString() : "No special dietary notes";
    }

    /**
     * Determines if the item has any special dietary considerations.
     */
    private static Boolean computeHasSpecialDiet(Boolean vegetarian, Boolean vegan, Boolean glutenFree) {
        return Boolean.TRUE.equals(vegetarian) ||
                Boolean.TRUE.equals(vegan) ||
                Boolean.TRUE.equals(glutenFree);
    }

    /**
     * Formats preparation time for display.
     */
    private static String formatPreparationTime(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return null;
        }

        if (minutes == 1) {
            return "1 minute";
        } else if (minutes < 60) {
            return minutes + " minutes";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours == 1 ? "1 hour" : hours + " hours";
            } else {
                return String.format("%d hour%s %d minute%s",
                        hours, hours == 1 ? "" : "s",
                        remainingMinutes, remainingMinutes == 1 ? "" : "s");
            }
        }
    }

    /**
     * Nested record for menu item summary information.
     * Used when full menu item details are not needed.
     */
    public record MenuItemSummary(
            UUID id,
            String name,
            BigDecimal price,
            Boolean available,
            Boolean vegetarian,
            Boolean vegan,
            Boolean glutenFree) {

        public static MenuItemSummary of(UUID id, String name, BigDecimal price, Boolean available,
                Boolean vegetarian, Boolean vegan, Boolean glutenFree) {
            return new MenuItemSummary(id, name, price, available, vegetarian, vegan, glutenFree);
        }

        public boolean isAvailable() {
            return Boolean.TRUE.equals(available);
        }

        public boolean hasSpecialDiet() {
            return Boolean.TRUE.equals(vegetarian) ||
                    Boolean.TRUE.equals(vegan) ||
                    Boolean.TRUE.equals(glutenFree);
        }

        public String getFormattedPrice() {
            return price != null ? String.format("₹%.2f", price) : "Price not available";
        }
    }
}
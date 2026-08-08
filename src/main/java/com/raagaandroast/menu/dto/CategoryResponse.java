package com.raagaandroast.menu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Category entities.
 * 
 * This DTO demonstrates:
 * - Clean separation between internal entity structure and API responses
 * - Proper JSON serialization configuration
 * - Inclusion of audit information for transparency
 * - Conditional field inclusion to reduce payload size
 * - Immutable record design for thread safety
 * 
 * @author RaagaAndRoast Development Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponse(

        UUID id,

        String name,

        String description,

        Boolean active,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt,

        Long version,

        // Additional computed fields for API convenience
        Integer menuItemCount

) {

    /**
     * Factory method for creating a basic category response without menu item
     * count.
     * Useful when menu item count is not needed or available.
     */
    public static CategoryResponse of(UUID id, String name, String description,
            Boolean active, LocalDateTime createdAt,
            LocalDateTime updatedAt, Long version) {
        return new CategoryResponse(id, name, description, active,
                createdAt, updatedAt, version, null);
    }

    /**
     * Factory method for creating a complete category response with menu item
     * count.
     * Useful for category listing where item counts are relevant.
     */
    public static CategoryResponse withItemCount(UUID id, String name, String description,
            Boolean active, LocalDateTime createdAt,
            LocalDateTime updatedAt, Long version,
            Integer menuItemCount) {
        return new CategoryResponse(id, name, description, active,
                createdAt, updatedAt, version, menuItemCount);
    }

    /**
     * Returns a summary version of this category response with minimal fields.
     * Useful for nested responses where full details are not needed.
     */
    public CategorySummary toSummary() {
        return new CategorySummary(id, name, active);
    }

    /**
     * Checks if this category is available for use (active and has a valid name).
     */
    public boolean isAvailable() {
        return active != null && active &&
                name != null && !name.trim().isEmpty();
    }

    /**
     * Returns a display name for this category, handling null cases gracefully.
     */
    public String getDisplayName() {
        return name != null ? name : "Unnamed Category";
    }

    /**
     * Nested record for category summary information.
     * Used when full category details are not needed.
     */
    public record CategorySummary(
            UUID id,
            String name,
            Boolean active) {

        public static CategorySummary of(UUID id, String name, Boolean active) {
            return new CategorySummary(id, name, active);
        }

        public boolean isActive() {
            return active != null && active;
        }
    }
}
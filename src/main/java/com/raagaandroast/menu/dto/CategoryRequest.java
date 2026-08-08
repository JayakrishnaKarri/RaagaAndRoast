package com.raagaandroast.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating and updating categories.
 * 
 * This DTO demonstrates:
 * - Jakarta Bean Validation for input validation
 * - Proper separation of concerns (no entity exposure)
 * - Clean API design with meaningful validation messages
 * 
 * @author RaagaAndRoast Development Team
 */
public record CategoryRequest(

        @NotBlank(message = "Category name is required") @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters") String name,

        @Size(max = 500, message = "Category description cannot exceed 500 characters") String description,

        Boolean active) {

    /**
     * Constructor with default values for optional fields.
     * Demonstrates proper handling of optional parameters.
     */
    public CategoryRequest {
        // Set default value for active if not provided
        if (active == null) {
            active = true;
        }
    }

    /**
     * Factory method for creating a new category request.
     * Useful for testing and programmatic creation.
     */
    public static CategoryRequest of(String name, String description) {
        return new CategoryRequest(name, description, true);
    }

    /**
     * Factory method for creating a category request with all fields.
     */
    public static CategoryRequest of(String name, String description, Boolean active) {
        return new CategoryRequest(name, description, active);
    }

    /**
     * Validates business rules beyond basic validation annotations.
     * This method can be called in service layer for additional validation.
     */
    public boolean isValid() {
        return name != null &&
                !name.trim().isEmpty() &&
                name.trim().length() >= 2 &&
                (description == null || description.length() <= 500);
    }

    /**
     * Returns a cleaned version of the name (trimmed and normalized).
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
}
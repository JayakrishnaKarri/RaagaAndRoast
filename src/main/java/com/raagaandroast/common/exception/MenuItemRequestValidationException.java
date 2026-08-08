package com.raagaandroast.common.exception;

/**
 * Exception thrown when menu item request validation fails.
 * 
 * This exception is thrown when:
 * - Menu item creation request is invalid
 * - Menu item update request is invalid
 * - Required fields are missing or malformed
 * - Business validation rules are violated
 * - Price range validation fails
 * 
 * Design Decisions:
 * - Extends BusinessException for consistent error handling
 * - Maps to HTTP 400 BAD REQUEST status code
 * - Separates validation errors from business rule violations
 * - Provides specific factory methods for common validation scenarios
 * 
 * Interview Points:
 * - Why separate from other validation exceptions? Domain-specific concerns
 * - Why 400 BAD REQUEST? Client sent malformed request
 * - How does this improve maintainability? Clear separation of concerns
 * - Why factory methods? Consistent error messages and easier testing
 * 
 * @author RaagaAndRoast Development Team
 */
public class MenuItemRequestValidationException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "MENU_ITEM_REQUEST_VALIDATION_FAILED";

    /**
     * Creates a menu item request validation exception.
     *
     * @param message the error message
     */
    public MenuItemRequestValidationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a menu item request validation exception with cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public MenuItemRequestValidationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    // Factory methods for common validation scenarios

    /**
     * Factory method for invalid menu item creation request.
     */
    public static MenuItemRequestValidationException invalidCreationRequest() {
        return new MenuItemRequestValidationException("Invalid menu item creation request");
    }

    /**
     * Factory method for invalid menu item update request.
     */
    public static MenuItemRequestValidationException invalidUpdateRequest() {
        return new MenuItemRequestValidationException("Invalid menu item update request");
    }

    /**
     * Factory method for inactive category assignment.
     */
    public static MenuItemRequestValidationException inactiveCategoryAssignment() {
        return new MenuItemRequestValidationException("Cannot create menu item in inactive category");
    }

    /**
     * Factory method for moving to inactive category.
     */
    public static MenuItemRequestValidationException moveToInactiveCategory() {
        return new MenuItemRequestValidationException("Cannot move menu item to inactive category");
    }

    /**
     * Factory method for invalid price range.
     */
    public static MenuItemRequestValidationException invalidPriceRange() {
        return new MenuItemRequestValidationException("Minimum price cannot be greater than maximum price");
    }

    /**
     * Factory method for missing required fields.
     */
    public static MenuItemRequestValidationException missingRequiredFields() {
        return new MenuItemRequestValidationException("Required fields are missing or empty");
    }

    /**
     * Factory method for invalid field values.
     */
    public static MenuItemRequestValidationException invalidFieldValues() {
        return new MenuItemRequestValidationException("One or more field values are invalid");
    }

    /**
     * Factory method for custom validation message.
     */
    public static MenuItemRequestValidationException withMessage(String message) {
        return new MenuItemRequestValidationException(message);
    }
}
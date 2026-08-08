package com.raagaandroast.common.exception;

/**
 * Exception thrown when category request validation fails.
 * 
 * This exception is thrown when:
 * - Category creation request is invalid
 * - Category update request is invalid
 * - Required fields are missing or malformed
 * - Business validation rules are violated
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
public class CategoryRequestValidationException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "CATEGORY_REQUEST_VALIDATION_FAILED";

    /**
     * Creates a category request validation exception.
     *
     * @param message the error message
     */
    public CategoryRequestValidationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a category request validation exception with cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public CategoryRequestValidationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    // Factory methods for common validation scenarios

    /**
     * Factory method for invalid category creation request.
     */
    public static CategoryRequestValidationException invalidCreationRequest() {
        return new CategoryRequestValidationException("Invalid category creation request");
    }

    /**
     * Factory method for invalid category update request.
     */
    public static CategoryRequestValidationException invalidUpdateRequest() {
        return new CategoryRequestValidationException("Invalid category update request");
    }

    /**
     * Factory method for missing required fields.
     */
    public static CategoryRequestValidationException missingRequiredFields() {
        return new CategoryRequestValidationException("Required fields are missing or empty");
    }

    /**
     * Factory method for invalid field values.
     */
    public static CategoryRequestValidationException invalidFieldValues() {
        return new CategoryRequestValidationException("One or more field values are invalid");
    }

    /**
     * Factory method for custom validation message.
     */
    public static CategoryRequestValidationException withMessage(String message) {
        return new CategoryRequestValidationException(message);
    }
}
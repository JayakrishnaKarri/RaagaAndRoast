package com.raagaandroast.common.exception;

/**
 * Exception thrown when an order status update request is invalid.
 * 
 * This exception is thrown when:
 * - The status update request is null
 * - Required fields for specific status transitions are missing
 * - The request contains invalid data for the requested status
 * 
 * Design Decisions:
 * - Extends BusinessException for consistent error handling
 * - Maps to HTTP 400 BAD REQUEST status code
 * - Provides clear messaging about what's wrong with the request
 * - Separates validation errors from business rule violations
 * 
 * Interview Points:
 * - Why separate from BusinessRuleViolationException? Different concerns
 * - Why 400 BAD REQUEST? Client sent malformed/incomplete request
 * - How does this differ from InvalidOrderStatusTransitionException?
 * This is about request format, that's about business rules
 * 
 * @author RaagaAndRoast Development Team
 */
public class InvalidOrderStatusUpdateRequestException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "INVALID_ORDER_STATUS_UPDATE_REQUEST";

    /**
     * Creates an invalid order status update request exception.
     *
     * @param message the error message
     */
    public InvalidOrderStatusUpdateRequestException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates an invalid order status update request exception with cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public InvalidOrderStatusUpdateRequestException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Factory method for null request.
     *
     * @return configured exception instance
     */
    public static InvalidOrderStatusUpdateRequestException nullRequest() {
        return new InvalidOrderStatusUpdateRequestException(
                "Status update request cannot be null");
    }

    /**
     * Factory method for missing required fields.
     *
     * @param fieldName the name of the missing field
     * @return configured exception instance
     */
    public static InvalidOrderStatusUpdateRequestException missingField(String fieldName) {
        return new InvalidOrderStatusUpdateRequestException(
                String.format("Required field '%s' is missing for this status update", fieldName));
    }

    /**
     * Factory method for validation failures.
     *
     * @param validationMessage the validation error message
     * @return configured exception instance
     */
    public static InvalidOrderStatusUpdateRequestException validationFailed(String validationMessage) {
        return new InvalidOrderStatusUpdateRequestException(validationMessage);
    }
}
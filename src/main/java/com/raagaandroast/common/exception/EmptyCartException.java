package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to perform an operation on an empty cart.
 * This typically results in a 422 UNPROCESSABLE ENTITY HTTP response.
 * 
 * Examples:
 * - Cannot checkout with empty cart
 * - Cannot apply discount to empty cart
 * - Cannot calculate shipping for empty cart
 * 
 * @author RaagaAndRoast Development Team
 */
public class EmptyCartException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "EMPTY_CART";

    /**
     * Creates a new EmptyCartException with a specific message.
     * 
     * @param message Description of the empty cart operation that failed
     */
    public EmptyCartException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new EmptyCartException with a message and underlying cause.
     * 
     * @param message Description of the empty cart operation that failed
     * @param cause   The underlying cause of this exception
     */
    public EmptyCartException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
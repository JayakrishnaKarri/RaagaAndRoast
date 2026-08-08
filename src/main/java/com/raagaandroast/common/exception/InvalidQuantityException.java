package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to perform an operation with invalid
 * quantity.
 * This typically results in a 422 UNPROCESSABLE ENTITY HTTP response.
 * 
 * Examples:
 * - Cannot add zero or negative quantity to cart
 * - Cannot update cart item to invalid quantity
 * - Cannot create order with invalid item quantities
 * 
 * @author RaagaAndRoast Development Team
 */
public class InvalidQuantityException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "INVALID_QUANTITY";

    /**
     * Creates a new InvalidQuantityException with a specific message.
     * 
     * @param message Description of the invalid quantity operation that failed
     */
    public InvalidQuantityException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new InvalidQuantityException for a specific item and quantity.
     * 
     * @param itemName The name of the item with invalid quantity
     * @param quantity The invalid quantity value
     */
    public InvalidQuantityException(String itemName, int quantity) {
        super(ERROR_CODE,
                String.format("Invalid quantity %d for item: %s. Quantity must be greater than 0", quantity, itemName));
    }

    /**
     * Creates a new InvalidQuantityException with a message and underlying cause.
     * 
     * @param message Description of the invalid quantity operation that failed
     * @param cause   The underlying cause of this exception
     */
    public InvalidQuantityException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
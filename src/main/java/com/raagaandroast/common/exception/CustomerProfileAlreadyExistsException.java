package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to create a customer profile for a user who
 * already has one.
 * This typically results in a 409 CONFLICT HTTP response.
 * 
 * Examples:
 * - User already has a customer profile
 * - Attempting to create duplicate customer profile
 * 
 * @author RaagaAndRoast Development Team
 */
public class CustomerProfileAlreadyExistsException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "CUSTOMER_PROFILE_ALREADY_EXISTS";

    /**
     * Creates a new CustomerProfileAlreadyExistsException with a specific message.
     * 
     * @param message Description of the duplicate customer profile issue
     */
    public CustomerProfileAlreadyExistsException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new CustomerProfileAlreadyExistsException for a specific user.
     * 
     * @param userId The user ID that already has a customer profile
     */
    public CustomerProfileAlreadyExistsException(Object userId) {
        super(ERROR_CODE, String.format("User with ID '%s' already has a customer profile", userId));
    }

    /**
     * Creates a new CustomerProfileAlreadyExistsException with a message and
     * underlying cause.
     * 
     * @param message Description of the duplicate customer profile issue
     * @param cause   The underlying cause of this exception
     */
    public CustomerProfileAlreadyExistsException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
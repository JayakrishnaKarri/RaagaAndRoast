package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to create a customer profile for a disabled
 * user.
 * Business rule: Only enabled users can have customer profiles.
 * 
 * Returns HTTP 422 UNPROCESSABLE ENTITY status code.
 */
public class DisabledUserException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Cannot create customer profile for disabled user";
    private static final String ERROR_CODE = "DISABLED_USER";

    public DisabledUserException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public DisabledUserException(String message) {
        super(message, ERROR_CODE);
    }

    public DisabledUserException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public static DisabledUserException forUser(String userId) {
        return new DisabledUserException(
                String.format("Cannot create customer profile for disabled user: %s", userId));
    }
}
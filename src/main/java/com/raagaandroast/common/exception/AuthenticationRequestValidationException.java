package com.raagaandroast.common.exception;

/**
 * Exception thrown when authentication request validation fails.
 * 
 * This exception is thrown when:
 * - Registration or login request is null
 * - Required fields are missing or empty
 * - Field values don't meet minimum requirements
 * - Request format is invalid
 * 
 * Design Decisions:
 * - Extends BusinessException for consistent error handling
 * - Maps to HTTP 400 BAD REQUEST status code
 * - Separates validation errors from authentication failures
 * - Provides specific factory methods for common validation scenarios
 * 
 * Interview Points:
 * - Why separate from AuthenticationException? Different concerns
 * - Why 400 BAD REQUEST? Client sent malformed request
 * - How does this improve security? Clear separation of validation vs auth
 * failures
 * - Why factory methods? Consistent error messages and easier testing
 * 
 * @author RaagaAndRoast Development Team
 */
public class AuthenticationRequestValidationException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "AUTHENTICATION_REQUEST_VALIDATION_FAILED";

    /**
     * Creates an authentication request validation exception.
     *
     * @param message the error message
     */
    public AuthenticationRequestValidationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates an authentication request validation exception with cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public AuthenticationRequestValidationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    // Registration validation factory methods

    /**
     * Factory method for null registration request.
     */
    public static AuthenticationRequestValidationException nullRegistrationRequest() {
        return new AuthenticationRequestValidationException("Registration request cannot be null");
    }

    /**
     * Factory method for missing username.
     */
    public static AuthenticationRequestValidationException missingUsername() {
        return new AuthenticationRequestValidationException("Username is required");
    }

    /**
     * Factory method for missing email.
     */
    public static AuthenticationRequestValidationException missingEmail() {
        return new AuthenticationRequestValidationException("Email is required");
    }

    /**
     * Factory method for missing password.
     */
    public static AuthenticationRequestValidationException missingPassword() {
        return new AuthenticationRequestValidationException("Password is required");
    }

    /**
     * Factory method for username too short.
     */
    public static AuthenticationRequestValidationException usernameTooShort() {
        return new AuthenticationRequestValidationException("Username must be at least 3 characters");
    }

    /**
     * Factory method for password too short.
     */
    public static AuthenticationRequestValidationException passwordTooShort() {
        return new AuthenticationRequestValidationException("Password must be at least 8 characters");
    }

    // Login validation factory methods

    /**
     * Factory method for null login request.
     */
    public static AuthenticationRequestValidationException nullLoginRequest() {
        return new AuthenticationRequestValidationException("Login request cannot be null");
    }

    /**
     * Factory method for missing username or email.
     */
    public static AuthenticationRequestValidationException missingUsernameOrEmail() {
        return new AuthenticationRequestValidationException("Username or email is required");
    }

    /**
     * Factory method for missing login password.
     */
    public static AuthenticationRequestValidationException missingLoginPassword() {
        return new AuthenticationRequestValidationException("Password is required");
    }
}
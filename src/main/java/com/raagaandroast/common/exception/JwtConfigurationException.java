package com.raagaandroast.common.exception;

/**
 * Exception thrown when JWT configuration is invalid.
 * 
 * This exception is thrown when:
 * - JWT secret key is too short for HS256 algorithm
 * - JWT configuration properties are missing or invalid
 * - JWT service initialization fails due to configuration issues
 * 
 * Design Decisions:
 * - Extends BusinessException for consistent error handling
 * - Maps to HTTP 500 INTERNAL SERVER ERROR status code (configuration issue)
 * - Provides specific factory methods for common configuration scenarios
 * - Used during application startup for configuration validation
 * 
 * Interview Points:
 * - Why 500 INTERNAL SERVER ERROR? Server misconfiguration, not client error
 * - Why validate at startup? Fail fast principle - catch config issues early
 * - How does this improve security? Prevents weak keys from being used
 * - Why factory methods? Consistent error messages and easier testing
 * 
 * @author RaagaAndRoast Development Team
 */
public class JwtConfigurationException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "JWT_CONFIGURATION_ERROR";

    /**
     * Creates a JWT configuration exception.
     *
     * @param message the error message
     */
    public JwtConfigurationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a JWT configuration exception with cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public JwtConfigurationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    // Factory methods for common configuration scenarios

    /**
     * Factory method for JWT secret key too short.
     */
    public static JwtConfigurationException secretKeyTooShort(int currentLength, int requiredLength) {
        return new JwtConfigurationException(
                String.format("JWT secret key must be at least %d characters for HS256. Current length: %d",
                        requiredLength, currentLength));
    }

    /**
     * Factory method for missing JWT secret.
     */
    public static JwtConfigurationException missingSecret() {
        return new JwtConfigurationException("JWT secret key is required but not configured");
    }

    /**
     * Factory method for invalid JWT expiration.
     */
    public static JwtConfigurationException invalidExpiration() {
        return new JwtConfigurationException("JWT expiration time must be positive");
    }

    /**
     * Factory method for invalid refresh token expiration.
     */
    public static JwtConfigurationException invalidRefreshExpiration() {
        return new JwtConfigurationException("JWT refresh token expiration time must be positive");
    }

    /**
     * Factory method for custom configuration message.
     */
    public static JwtConfigurationException withMessage(String message) {
        return new JwtConfigurationException(message);
    }
}
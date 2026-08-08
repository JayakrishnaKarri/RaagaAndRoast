package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to use an invalid or expired refresh token.
 * This typically occurs during token refresh operations.
 * 
 * Returns HTTP 401 UNAUTHORIZED status code.
 */
public class InvalidRefreshTokenException extends BusinessException {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MESSAGE = "Invalid refresh token";
    private static final String ERROR_CODE = "INVALID_REFRESH_TOKEN";

    public InvalidRefreshTokenException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidRefreshTokenException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    /**
     * Factory method for invalid refresh token scenarios.
     */
    public static InvalidRefreshTokenException invalidToken() {
        return new InvalidRefreshTokenException("Invalid or expired refresh token");
    }
}
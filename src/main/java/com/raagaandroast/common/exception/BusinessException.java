package com.raagaandroast.common.exception;

/**
 * Base class for all business-related exceptions in the application.
 * This provides a common structure for domain-specific errors that should
 * be handled gracefully and returned to clients with appropriate HTTP status
 * codes.
 * 
 * Business exceptions represent expected error conditions in the application
 * flow,
 * such as validation failures, resource not found, or business rule violations.
 * 
 * @author RaagaAndRoast Development Team
 */
public abstract class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
	private final String errorCode;
    private final Object[] args;

    /**
     * Creates a new business exception with an error code and message.
     * 
     * @param errorCode A unique identifier for this type of error
     * @param message   A human-readable description of the error
     */
    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * Creates a new business exception with an error code, message, and arguments.
     * This constructor is useful for parameterized error messages.
     * 
     * @param errorCode A unique identifier for this type of error
     * @param message   A human-readable description of the error
     * @param args      Arguments to be used in message formatting
     */
    protected BusinessException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * Creates a new business exception with an error code, message, and cause.
     * 
     * @param errorCode A unique identifier for this type of error
     * @param message   A human-readable description of the error
     * @param cause     The underlying cause of this exception
     */
    protected BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * Gets the error code for this exception.
     * Error codes should be unique across the application and can be used
     * by clients to programmatically handle specific error conditions.
     * 
     * @return The error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the arguments used in message formatting.
     * 
     * @return The message arguments, or null if none were provided
     */
    public Object[] getArgs() {
        return args;
    }
}
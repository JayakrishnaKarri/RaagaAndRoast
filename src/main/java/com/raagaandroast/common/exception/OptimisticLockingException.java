package com.raagaandroast.common.exception;

/**
 * Exception thrown when an optimistic locking conflict occurs.
 *
 * This exception is thrown when an entity has been modified by another user
 * between the time it was read and the time an update was attempted.
 * This is a common scenario in concurrent applications where multiple users
 * might be editing the same data simultaneously.
 *
 * Design Decisions:
 * - Extends BusinessException for consistent error handling
 * - Maps to HTTP 409 CONFLICT status code
 * - Provides clear messaging about concurrent modification
 * - Includes entity information for better debugging
 *
 * Interview Points:
 * - What is optimistic locking? Version-based concurrency control
 * - Why 409 CONFLICT? Indicates resource state conflict
 * - How to handle? Client should refresh and retry
 * - Alternative? Pessimistic locking (but reduces concurrency)
 *
 * @author RaagaAndRoast Development Team
 */
public class OptimisticLockingException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "OPTIMISTIC_LOCKING_FAILURE";
    private static final String DEFAULT_MESSAGE = "Resource has been modified by another user";

    /**
     * Creates an optimistic locking exception with default message.
     */
    public OptimisticLockingException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    /**
     * Creates an optimistic locking exception with custom message.
     *
     * @param message the error message
     */
    public OptimisticLockingException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates an optimistic locking exception with message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public OptimisticLockingException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Factory method for entity-specific optimistic locking conflicts.
     *
     * @param entityType the type of entity that had the conflict
     * @param entityId   the ID of the entity
     * @return configured exception instance
     */
    public static OptimisticLockingException forEntity(String entityType, Object entityId) {
        return new OptimisticLockingException(
                String.format("%s with ID %s has been modified by another user. Please refresh and try again.",
                        entityType, entityId));
    }

    /**
     * Factory method for general optimistic locking conflicts.
     *
     * @param entityType the type of entity that had the conflict
     * @return configured exception instance
     */
    public static OptimisticLockingException forEntityType(String entityType) {
        return new OptimisticLockingException(
                String.format("%s has been modified by another user. Please refresh and try again.",
                        entityType));
    }
}
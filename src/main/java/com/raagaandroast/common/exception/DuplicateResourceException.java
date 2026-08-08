package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * This typically results in a 409 CONFLICT HTTP response.
 * 
 * Examples:
 * - User with email already exists
 * - Category with name already exists
 * - Username already taken
 * 
 * @author RaagaAndRoast Development Team
 */
public class DuplicateResourceException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "DUPLICATE_RESOURCE";

    /**
     * Creates a new DuplicateResourceException with a specific message.
     * 
     * @param message Description of what resource already exists
     */
    public DuplicateResourceException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new DuplicateResourceException for a specific resource type and
     * identifier.
     * 
     * @param resourceType    The type of resource (e.g., "User", "Category",
     *                        "MenuItem")
     * @param identifier      The identifier that already exists (e.g., email,
     *                        username, name)
     * @param identifierValue The value of the identifier
     */
    public DuplicateResourceException(String resourceType, String identifier, Object identifierValue) {
        super(ERROR_CODE, String.format("%s with %s '%s' already exists", resourceType, identifier, identifierValue));
    }

    /**
     * Creates a new DuplicateResourceException with a message and underlying cause.
     * 
     * @param message Description of what resource already exists
     * @param cause   The underlying cause of this exception
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
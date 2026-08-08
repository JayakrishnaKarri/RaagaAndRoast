package com.raagaandroast.common.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 * This typically results in a 404 NOT FOUND HTTP response.
 * 
 * Examples:
 * - User with ID 123 not found
 * - Menu item with ID 456 not found
 * - Order with ID 789 not found
 * 
 * @author RaagaAndRoast Development Team
 */
public class ResourceNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * Creates a new ResourceNotFoundException with a specific message.
     * 
     * @param message Description of what resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new ResourceNotFoundException for a specific resource type and ID.
     * 
     * @param resourceType The type of resource (e.g., "User", "Order", "MenuItem")
     * @param resourceId   The ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(ERROR_CODE, String.format("%s with ID '%s' not found", resourceType, resourceId));
    }

    /**
     * Creates a new ResourceNotFoundException with a message and underlying cause.
     * 
     * @param message Description of what resource was not found
     * @param cause   The underlying cause of this exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
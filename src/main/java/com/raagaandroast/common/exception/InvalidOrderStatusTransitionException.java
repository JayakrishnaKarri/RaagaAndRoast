package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting an invalid order status transition.
 * This typically results in a 422 UNPROCESSABLE ENTITY HTTP response.
 * 
 * Examples:
 * - Cannot transition from COMPLETED to PENDING
 * - Cannot transition from CANCELLED to PREPARING
 * - Cannot cancel an already completed order
 * 
 * @author RaagaAndRoast Development Team
 */
public class InvalidOrderStatusTransitionException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "INVALID_ORDER_STATUS_TRANSITION";

    /**
     * Creates a new InvalidOrderStatusTransitionException with a specific message.
     * 
     * @param message Description of the invalid status transition
     */
    public InvalidOrderStatusTransitionException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new InvalidOrderStatusTransitionException for specific status
     * transition.
     * 
     * @param currentStatus   The current order status
     * @param requestedStatus The requested status to transition to
     */
    public InvalidOrderStatusTransitionException(String currentStatus, String requestedStatus) {
        super(ERROR_CODE, String.format("Cannot transition order from %s to %s", currentStatus, requestedStatus));
    }

    /**
     * Creates a new InvalidOrderStatusTransitionException with a message and
     * underlying cause.
     * 
     * @param message Description of the invalid status transition
     * @param cause   The underlying cause of this exception
     */
    public InvalidOrderStatusTransitionException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
package com.raagaandroast.common.exception;

/**
 * Exception thrown when a business rule is violated.
 * This typically results in a 422 UNPROCESSABLE ENTITY HTTP response.
 * 
 * Examples:
 * - Cannot add unavailable menu item to cart
 * - Cannot transition order from COMPLETED to PENDING
 * - Cannot delete category that has menu items
 * - Cart is empty, cannot create order
 * 
 * @author RaagaAndRoast Development Team
 */
public class BusinessRuleViolationException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    /**
     * Creates a new BusinessRuleViolationException with a specific message.
     * 
     * @param message Description of the business rule that was violated
     */
    public BusinessRuleViolationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new BusinessRuleViolationException with a message and underlying
     * cause.
     * 
     * @param message Description of the business rule that was violated
     * @param cause   The underlying cause of this exception
     */
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
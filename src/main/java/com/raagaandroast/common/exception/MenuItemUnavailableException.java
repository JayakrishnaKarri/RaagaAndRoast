package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to perform an operation on an unavailable
 * menu item.
 * This typically results in a 422 UNPROCESSABLE ENTITY HTTP response.
 * 
 * Examples:
 * - Cannot add unavailable menu item to cart
 * - Cannot order unavailable menu item
 * - Cannot update price of unavailable menu item
 * 
 * @author RaagaAndRoast Development Team
 */
public class MenuItemUnavailableException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "MENU_ITEM_UNAVAILABLE";

    /**
     * Creates a new MenuItemUnavailableException with a specific message.
     * 
     * @param message Description of the unavailable menu item operation that failed
     */
    public MenuItemUnavailableException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new MenuItemUnavailableException for a specific menu item.
     * 
     * @param menuItemName The name of the unavailable menu item
     */
    public MenuItemUnavailableException(String menuItemName, String operation) {
        super(ERROR_CODE, String.format("Cannot %s unavailable menu item: %s", operation, menuItemName));
    }

    /**
     * Creates a new MenuItemUnavailableException with a message and underlying
     * cause.
     * 
     * @param message Description of the unavailable menu item operation that failed
     * @param cause   The underlying cause of this exception
     */
    public MenuItemUnavailableException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
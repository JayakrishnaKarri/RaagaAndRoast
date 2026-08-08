package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to delete a category that has associated
 * menu items.
 * This typically results in a 422 UNPROCESSABLE ENTITY HTTP response.
 * 
 * Examples:
 * - Cannot delete category that has menu items
 * - Cannot deactivate category with active menu items
 * - Category has dependencies that prevent deletion
 * 
 * @author RaagaAndRoast Development Team
 */
public class CategoryHasMenuItemsException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "CATEGORY_HAS_MENU_ITEMS";

    /**
     * Creates a new CategoryHasMenuItemsException with a specific message.
     * 
     * @param message Description of the category dependency issue
     */
    public CategoryHasMenuItemsException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Creates a new CategoryHasMenuItemsException for a specific category.
     * 
     * @param categoryId The category ID that has menu items
     * @param itemCount  The number of menu items in the category
     */
    public CategoryHasMenuItemsException(Object categoryId, int itemCount) {
        super(ERROR_CODE, String.format(
                "Cannot delete category with ID '%s' as it has %d menu items. Please remove or reassign menu items first.",
                categoryId, itemCount));
    }

    /**
     * Creates a new CategoryHasMenuItemsException with a message and underlying
     * cause.
     * 
     * @param message Description of the category dependency issue
     * @param cause   The underlying cause of this exception
     */
    public CategoryHasMenuItemsException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
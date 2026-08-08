package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to create menu items in an inactive category
 * or move menu items to an inactive category.
 * Business rule: Menu items can only belong to active categories.
 * 
 * Returns HTTP 422 UNPROCESSABLE ENTITY status code.
 */
public class InactiveCategoryException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Cannot perform operation with inactive category";
    private static final String ERROR_CODE = "INACTIVE_CATEGORY";

    public InactiveCategoryException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InactiveCategoryException(String message) {
        super(message, ERROR_CODE);
    }

    public InactiveCategoryException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public static InactiveCategoryException forMenuItemCreation() {
        return new InactiveCategoryException(
                "Cannot create menu item in inactive category");
    }

    public static InactiveCategoryException forMenuItemMove() {
        return new InactiveCategoryException(
                "Cannot move menu item to inactive category");
    }
}
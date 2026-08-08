package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to create a duplicate address for a
 * customer.
 * This typically occurs when trying to add an address that already exists
 * with the same details for the same customer.
 * 
 * Returns HTTP 409 CONFLICT status code.
 */
public class DuplicateAddressException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Duplicate address already exists for customer";
    private static final String ERROR_CODE = "DUPLICATE_ADDRESS";

    public DuplicateAddressException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public DuplicateAddressException(String message) {
        super(message, ERROR_CODE);
    }

    public DuplicateAddressException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public static DuplicateAddressException forCustomer(String customerId) {
        return new DuplicateAddressException(
                String.format("Duplicate address already exists for customer: %s", customerId));
    }
}
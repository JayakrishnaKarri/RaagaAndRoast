package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to delete the last active address for a
 * customer.
 * Business rule: A customer must have at least one active address.
 * 
 * Returns HTTP 422 UNPROCESSABLE ENTITY status code.
 */
public class LastActiveAddressException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Cannot delete the only active address for customer";
    private static final String ERROR_CODE = "LAST_ACTIVE_ADDRESS";

    public LastActiveAddressException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public LastActiveAddressException(String message) {
        super(message, ERROR_CODE);
    }

    public LastActiveAddressException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public static LastActiveAddressException forCustomer(String customerId) {
        return new LastActiveAddressException(
                String.format("Cannot delete the only active address for customer: %s", customerId));
    }

    public static LastActiveAddressException forDefaultFlag() {
        return new LastActiveAddressException(
                "Cannot remove default flag from the only active address");
    }
}
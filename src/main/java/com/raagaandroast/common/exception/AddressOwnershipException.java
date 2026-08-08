package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to perform operations that violate address
 * ownership rules.
 * This typically occurs when trying to access or modify an address that doesn't
 * belong to the customer.
 * 
 * Returns HTTP 403 FORBIDDEN status code.
 */
public class AddressOwnershipException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Address does not belong to customer";
    private static final String ERROR_CODE = "ADDRESS_OWNERSHIP_VIOLATION";

    public AddressOwnershipException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public AddressOwnershipException(String message) {
        super(message, ERROR_CODE);
    }

    public AddressOwnershipException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public static AddressOwnershipException forCustomer(String addressId, String customerId) {
        return new AddressOwnershipException(
                String.format("Address %s does not belong to customer: %s", addressId, customerId));
    }
}
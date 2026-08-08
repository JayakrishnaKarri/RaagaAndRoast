package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to perform operations on an inactive
 * address.
 * This typically occurs when trying to update, set as default, or use an
 * inactive address.
 * 
 * Returns HTTP 422 UNPROCESSABLE ENTITY status code.
 */
public class InactiveAddressException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Cannot perform operation on inactive address";
    private static final String ERROR_CODE = "INACTIVE_ADDRESS";

    public InactiveAddressException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InactiveAddressException(String message) {
        super(message, ERROR_CODE);
    }

    public InactiveAddressException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public static InactiveAddressException forUpdate(String addressId) {
        return new InactiveAddressException(
                String.format("Cannot update inactive address: %s", addressId));
    }

    public static InactiveAddressException forSetDefault(String addressId) {
        return new InactiveAddressException(
                String.format("Cannot set inactive address as default: %s", addressId));
    }

    public static InactiveAddressException alreadyActive(String addressId) {
        return new InactiveAddressException(
                String.format("Address is already active: %s", addressId));
    }
}
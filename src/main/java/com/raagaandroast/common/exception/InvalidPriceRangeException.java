package com.raagaandroast.common.exception;

/**
 * Exception thrown when attempting to perform operations with invalid price
 * ranges.
 * This typically occurs when minimum price is greater than maximum price in
 * filtering.
 * 
 * Returns HTTP 400 BAD REQUEST status code.
 */
public class InvalidPriceRangeException extends BusinessException {

    private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Invalid price range specified";
    private static final String ERROR_CODE = "INVALID_PRICE_RANGE";

    public InvalidPriceRangeException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidPriceRangeException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidPriceRangeException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public static InvalidPriceRangeException minGreaterThanMax() {
        return new InvalidPriceRangeException(
                "Minimum price cannot be greater than maximum price");
    }
}
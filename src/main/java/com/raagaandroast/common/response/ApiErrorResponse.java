package com.raagaandroast.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized API error response format for all error conditions.
 *
 * <p>
 * Provides a consistent structure for error responses across the
 * entire application.
 * </p>
 *
 * <p>
 * Response fields:
 * </p>
 *
 * <ul>
 * <li>Timestamp - time when the error occurred</li>
 * <li>Status - HTTP status code</li>
 * <li>Error - HTTP error category</li>
 * <li>Message - human-readable error message</li>
 * <li>Path - request path</li>
 * <li>Error code - application-specific error code</li>
 * <li>Validation errors - optional field-level validation details</li>
 * </ul>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * {
 *   "timestamp": "2026-08-08T10:30:00",
 *   "status": 404,
 *   "error": "NOT_FOUND",
 *   "message": "Menu item not found",
 *   "path": "/api/menu-items/123",
 *   "errorCode": "RESOURCE_NOT_FOUND"
 * }
 * </pre>
 *
 * @author RaagaAndRoast Development Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * HTTP error category.
     *
     * Example:
     * NOT_FOUND, BAD_REQUEST, CONFLICT.
     */
    private String error;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * Request path where the error occurred.
     */
    private String path;

    /**
     * Application-specific error code.
     */
    private String errorCode;

    /**
     * Field-level validation errors.
     */
    private List<ValidationError> validationErrors;

    /**
     * Default constructor required by Jackson.
     */
    public ApiErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a basic API error response.
     *
     * @param status  HTTP status code
     * @param error   error category
     * @param message human-readable error message
     * @param path    request path
     */
    public ApiErrorResponse(
            int status,
            String error,
            String message,
            String path) {

        this();

        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Creates an API error response with an application error code.
     *
     * @param status    HTTP status code
     * @param error     error category
     * @param message   human-readable error message
     * @param path      request path
     * @param errorCode application-specific error code
     */
    public ApiErrorResponse(
            int status,
            String error,
            String message,
            String path,
            String errorCode) {

        this(
                status,
                error,
                message,
                path);

        this.errorCode = errorCode;
    }

    /**
     * Creates an API error response containing validation errors.
     *
     * @param status           HTTP status code
     * @param error            error category
     * @param message          human-readable error message
     * @param path             request path
     * @param validationErrors field-level validation errors
     */
    public ApiErrorResponse(
            int status,
            String error,
            String message,
            String path,
            List<ValidationError> validationErrors) {

        this(
                status,
                error,
                message,
                path);

        this.validationErrors = validationErrors;
    }

    /**
     * Returns the timestamp.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns HTTP status code.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets HTTP status code.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Returns error category.
     */
    public String getError() {
        return error;
    }

    /**
     * Sets error category.
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Returns human-readable error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets human-readable error message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns request path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets request path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns application-specific error code.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets application-specific error code.
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Returns field-level validation errors.
     */
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Sets field-level validation errors.
     */
    public void setValidationErrors(
            List<ValidationError> validationErrors) {

        this.validationErrors = validationErrors;
    }

    /**
     * Represents a field-level validation error.
     *
     * <p>
     * Sensitive rejected values are intentionally not included
     * in the API response.
     * </p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationError {

        /**
         * Name of the field that failed validation.
         */
        private String field;

        /**
         * Validation failure message.
         */
        private String message;

        /**
         * Default constructor required by Jackson.
         */
        public ValidationError() {
        }

        /**
         * Creates a validation error.
         *
         * @param field   field name
         * @param message validation message
         */
        public ValidationError(
                String field,
                String message) {

            this.field = field;
            this.message = message;
        }

        /**
         * Returns field name.
         */
        public String getField() {
            return field;
        }

        /**
         * Sets field name.
         */
        public void setField(String field) {
            this.field = field;
        }

        /**
         * Returns validation message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Sets validation message.
         */
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
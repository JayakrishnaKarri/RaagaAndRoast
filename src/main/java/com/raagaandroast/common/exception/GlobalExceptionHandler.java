package com.raagaandroast.common.exception;

import com.raagaandroast.common.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Global exception handler for the entire application.
 *
 * <p>
 * Provides centralized exception handling across all controllers
 * and ensures a consistent API error response format.
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 *
 * <ul>
 * <li>Handle business exceptions</li>
 * <li>Handle validation exceptions</li>
 * <li>Handle authentication and authorization failures</li>
 * <li>Handle HTTP request errors</li>
 * <li>Handle database constraint violations</li>
 * <li>Handle unexpected system exceptions</li>
 * <li>Prevent sensitive internal information from being exposed</li>
 * </ul>
 *
 * <p>
 * HTTP status codes:
 * </p>
 *
 * <ul>
 * <li>400 - Bad Request</li>
 * <li>401 - Unauthorized</li>
 * <li>403 - Forbidden</li>
 * <li>404 - Not Found</li>
 * <li>405 - Method Not Allowed</li>
 * <li>409 - Conflict</li>
 * <li>422 - Unprocessable Content</li>
 * <li>500 - Internal Server Error</li>
 * </ul>
 *
 * @author RaagaAndRoast Development Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // ================================================================
        // Business Exception Handlers
        // ================================================================

        /**
         * Handles ResourceNotFoundException.
         *
         * @return 404 NOT FOUND
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Resource not found: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "NOT_FOUND",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(errorResponse);
        }

        /**
         * Handles DuplicateResourceException.
         *
         * @return 409 CONFLICT
         */
        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(
                        DuplicateResourceException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Duplicate resource: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "CONFLICT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(errorResponse);
        }

        /**
         * Handles BusinessRuleViolationException.
         *
         * @return 422 UNPROCESSABLE CONTENT
         */
        @ExceptionHandler(BusinessRuleViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleBusinessRuleViolationException(
                        BusinessRuleViolationException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Business rule violation: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                                "UNPROCESSABLE_CONTENT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                                .body(errorResponse);
        }

        /**
         * Handles EmptyCartException.
         *
         * @return 422 UNPROCESSABLE CONTENT
         */
        @ExceptionHandler(EmptyCartException.class)
        public ResponseEntity<ApiErrorResponse> handleEmptyCartException(
                        EmptyCartException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Empty cart operation: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                                "UNPROCESSABLE_CONTENT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                                .body(errorResponse);
        }

        /**
         * Handles MenuItemUnavailableException.
         *
         * @return 422 UNPROCESSABLE CONTENT
         */
        @ExceptionHandler(MenuItemUnavailableException.class)
        public ResponseEntity<ApiErrorResponse> handleMenuItemUnavailableException(
                        MenuItemUnavailableException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Menu item unavailable: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                                "UNPROCESSABLE_CONTENT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                                .body(errorResponse);
        }

        /**
         * Handles InvalidQuantityException.
         *
         * @return 422 UNPROCESSABLE CONTENT
         */
        @ExceptionHandler(InvalidQuantityException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidQuantityException(
                        InvalidQuantityException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Invalid quantity: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                                "UNPROCESSABLE_CONTENT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                                .body(errorResponse);
        }

        /**
         * Handles InvalidOrderStatusTransitionException.
         *
         * @return 422 UNPROCESSABLE CONTENT
         */
        @ExceptionHandler(InvalidOrderStatusTransitionException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidOrderStatusTransitionException(
                        InvalidOrderStatusTransitionException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Invalid order status transition: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                                "UNPROCESSABLE_CONTENT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                                .body(errorResponse);
        }

        /**
         * Handles CustomerProfileAlreadyExistsException.
         *
         * @return 409 CONFLICT
         */
        @ExceptionHandler(CustomerProfileAlreadyExistsException.class)
        public ResponseEntity<ApiErrorResponse> handleCustomerProfileAlreadyExistsException(
                        CustomerProfileAlreadyExistsException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Customer profile already exists: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "CONFLICT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(errorResponse);
        }

        /**
         * Handles CategoryHasMenuItemsException.
         *
         * @return 422 UNPROCESSABLE CONTENT
         */
        @ExceptionHandler(CategoryHasMenuItemsException.class)
        public ResponseEntity<ApiErrorResponse> handleCategoryHasMenuItemsException(
                        CategoryHasMenuItemsException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Category has menu items: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                                "UNPROCESSABLE_CONTENT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                                .body(errorResponse);
        }

        /**
         * Handles OptimisticLockingException.
         *
         * @return 409 CONFLICT
         */
        @ExceptionHandler(OptimisticLockingException.class)
        public ResponseEntity<ApiErrorResponse> handleOptimisticLockingException(
                        OptimisticLockingException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Optimistic locking conflict: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "CONFLICT",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(errorResponse);
        }

        /**
         * Handles InvalidOrderStatusUpdateRequestException.
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(InvalidOrderStatusUpdateRequestException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidOrderStatusUpdateRequestException(
                        InvalidOrderStatusUpdateRequestException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Invalid order status update request: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        /**
         * Handles AuthenticationRequestValidationException.
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(AuthenticationRequestValidationException.class)
        public ResponseEntity<ApiErrorResponse> handleAuthenticationRequestValidationException(
                        AuthenticationRequestValidationException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Authentication request validation failed: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        /**
         * Handles CategoryRequestValidationException.
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(CategoryRequestValidationException.class)
        public ResponseEntity<ApiErrorResponse> handleCategoryRequestValidationException(
                        CategoryRequestValidationException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Category request validation failed: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        /**
         * Handles MenuItemRequestValidationException.
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(MenuItemRequestValidationException.class)
        public ResponseEntity<ApiErrorResponse> handleMenuItemRequestValidationException(
                        MenuItemRequestValidationException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Menu item request validation failed: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                ex.getMessage(),
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        /**
         * Handles JwtConfigurationException.
         *
         * @return 500 INTERNAL SERVER ERROR
         */
        @ExceptionHandler(JwtConfigurationException.class)
        public ResponseEntity<ApiErrorResponse> handleJwtConfigurationException(
                        JwtConfigurationException ex,
                        HttpServletRequest request) {

                logger.error(
                                "JWT configuration error: {} at path: {}",
                                ex.getMessage(),
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "INTERNAL_SERVER_ERROR",
                                "JWT configuration error - please contact system administrator",
                                request.getRequestURI(),
                                ex.getErrorCode());

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(errorResponse);
        }

        // ================================================================
        // Validation Exception Handlers
        // ================================================================

        /**
         * Handles MethodArgumentNotValidException.
         *
         * <p>
         * Occurs when @Valid request body validation fails.
         * </p>
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Validation failed for request at path: {} with {} errors",
                                request.getRequestURI(),
                                ex.getBindingResult().getErrorCount());

                List<ApiErrorResponse.ValidationError> validationErrors = new ArrayList<>();

                for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {

                        validationErrors.add(
                                        new ApiErrorResponse.ValidationError(
                                                        fieldError.getField(),
                                                        fieldError.getDefaultMessage()));
                }

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                "Validation failed for one or more fields",
                                request.getRequestURI(),
                                validationErrors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        /**
         * Handles ConstraintViolationException.
         *
         * <p>
         * Occurs when method parameter validation fails.
         * </p>
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
                        ConstraintViolationException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Constraint violation at path: {} with {} violations",
                                request.getRequestURI(),
                                ex.getConstraintViolations().size());

                List<ApiErrorResponse.ValidationError> validationErrors = new ArrayList<>();

                Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

                for (ConstraintViolation<?> violation : violations) {

                        String fieldName = violation.getPropertyPath().toString();

                        validationErrors.add(
                                        new ApiErrorResponse.ValidationError(
                                                        fieldName,
                                                        violation.getMessage()));
                }

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                "Constraint validation failed",
                                request.getRequestURI(),
                                validationErrors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        // ================================================================
        // Security Exception Handlers
        // ================================================================

        /**
         * Handles authentication failures.
         *
         * @return 401 UNAUTHORIZED
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
                        AuthenticationException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Authentication failed at path: {} - {}",
                                request.getRequestURI(),
                                ex.getMessage());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "UNAUTHORIZED",
                                "Authentication failed",
                                request.getRequestURI(),
                                "AUTHENTICATION_FAILED");

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(errorResponse);
        }

        /**
         * Handles invalid credentials.
         *
         * @return 401 UNAUTHORIZED
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Bad credentials at path: {}",
                                request.getRequestURI());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "UNAUTHORIZED",
                                "Invalid username or password",
                                request.getRequestURI(),
                                "INVALID_CREDENTIALS");

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(errorResponse);
        }

        /**
         * Handles authorization failures.
         *
         * @return 403 FORBIDDEN
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Access denied at path: {} - {}",
                                request.getRequestURI(),
                                ex.getMessage());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "FORBIDDEN",
                                "Access denied - insufficient permissions",
                                request.getRequestURI(),
                                "ACCESS_DENIED");

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(errorResponse);
        }

        // ================================================================
        // HTTP / Request Exception Handlers
        // ================================================================

        /**
         * Handles unsupported HTTP methods.
         *
         * @return 405 METHOD NOT ALLOWED
         */
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Method not supported: {} at path: {}",
                                ex.getMethod(),
                                request.getRequestURI());

                String supportedMethods = ex.getSupportedMethods() != null
                                ? String.join(", ", ex.getSupportedMethods())
                                : "Not specified";

                String message = String.format(
                                "HTTP method '%s' is not supported. Supported methods: %s",
                                ex.getMethod(),
                                supportedMethods);

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.METHOD_NOT_ALLOWED.value(),
                                "METHOD_NOT_ALLOWED",
                                message,
                                request.getRequestURI(),
                                "METHOD_NOT_SUPPORTED");

                return ResponseEntity
                                .status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(errorResponse);
        }

        /**
         * Handles requests for endpoints that do not exist.
         *
         * @return 404 NOT FOUND
         */
        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(
                        NoHandlerFoundException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "No handler found for {} {}",
                                ex.getHttpMethod(),
                                ex.getRequestURL());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "NOT_FOUND",
                                "The requested endpoint was not found",
                                request.getRequestURI(),
                                "ENDPOINT_NOT_FOUND");

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(errorResponse);
        }

        /**
         * Handles malformed JSON and unreadable request bodies.
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Malformed JSON request at path: {} - {}",
                                request.getRequestURI(),
                                ex.getMessage());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                "Malformed JSON request",
                                request.getRequestURI(),
                                "MALFORMED_JSON");

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        /**
         * Handles missing required request parameters.
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(
                        MissingServletRequestParameterException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Missing request parameter: {} at path: {}",
                                ex.getParameterName(),
                                request.getRequestURI());

                String message = String.format(
                                "Required request parameter '%s' is missing",
                                ex.getParameterName());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                message,
                                request.getRequestURI(),
                                "MISSING_PARAMETER");

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        /**
         * Handles request parameter type conversion failures.
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Type mismatch for parameter: {} at path: {}",
                                ex.getName(),
                                request.getRequestURI());

                String expectedType = ex.getRequiredType() != null
                                ? ex.getRequiredType().getSimpleName()
                                : "unknown";

                String message = String.format(
                                "Invalid value for parameter '%s'. Expected type: %s",
                                ex.getName(),
                                expectedType);

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                message,
                                request.getRequestURI(),
                                "TYPE_MISMATCH");

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        // ================================================================
        // Database Exception Handlers
        // ================================================================

        /**
         * Handles database integrity violations.
         *
         * <p>
         * Database implementation details are intentionally not exposed
         * to API clients.
         * </p>
         *
         * @return 409 CONFLICT
         */
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request) {

                logger.error(
                                "Data integrity violation at path: {} - {}",
                                request.getRequestURI(),
                                ex.getMessage());

                String message = "Data integrity constraint violation. "
                                + "The operation could not be completed.";

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "CONFLICT",
                                message,
                                request.getRequestURI(),
                                "DATA_INTEGRITY_VIOLATION");

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(errorResponse);
        }

        // ================================================================
        // Generic Exception Handlers
        // ================================================================

        /**
         * Handles IllegalArgumentException.
         *
         * @return 400 BAD REQUEST
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {

                logger.warn(
                                "Illegal argument at path: {} - {}",
                                request.getRequestURI(),
                                ex.getMessage());

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "BAD_REQUEST",
                                ex.getMessage(),
                                request.getRequestURI(),
                                "ILLEGAL_ARGUMENT");

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse);
        }

        /**
         * Handles unexpected exceptions.
         *
         * <p>
         * Full exception details are logged server-side.
         * A generic message is returned to the client.
         * </p>
         *
         * @return 500 INTERNAL SERVER ERROR
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {

                logger.error(
                                "Unexpected error at path: {}",
                                request.getRequestURI(),
                                ex);

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "INTERNAL_SERVER_ERROR",
                                "An unexpected error occurred. Please try again later.",
                                request.getRequestURI(),
                                "INTERNAL_ERROR");

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(errorResponse);
        }
}
package com.raagaandroast.order.controller;

import com.raagaandroast.customer.entity.Customer;
import com.raagaandroast.customer.repository.CustomerRepository;
import com.raagaandroast.order.dto.CreateOrderRequest;
import com.raagaandroast.order.dto.OrderResponse;
import com.raagaandroast.order.dto.UpdateOrderStatusRequest;
import com.raagaandroast.order.entity.OrderStatus;
import com.raagaandroast.order.service.OrderService;
import com.raagaandroast.user.entity.User;
import com.raagaandroast.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Order management operations.
 * 
 * This controller demonstrates:
 * - Comprehensive REST API design for order management
 * - Role-based and permission-based authorization
 * - Resource ownership validation for security
 * - Proper HTTP status codes and response handling
 * - Pagination support for large datasets
 * - Business workflow management through APIs
 * 
 * Design Decisions:
 * - RESTful endpoint design following HTTP conventions
 * - @PreAuthorize for method-level security
 * - Pagination for scalable order listing
 * - Separate endpoints for different user roles
 * - Comprehensive validation and error handling
 * - Audit logging for business operations
 * 
 * Interview Points:
 * - Why @PreAuthorize? Method-level security with role/permission checks
 * - Why pagination? Handle large order datasets efficiently
 * - Why separate customer/staff endpoints? Different authorization requirements
 * - Why resource ownership validation? Prevent unauthorized access
 * - Why comprehensive logging? Audit trail and debugging support
 * 
 * Security Architecture:
 * - Customers can only access their own orders
 * - Staff can view orders by status for operations
 * - Managers can access all orders and analytics
 * - Admins have full order management capabilities
 * - Order status updates require appropriate permissions
 * 
 * @author RaagaAndRoast Development Team
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "Order creation, tracking, and management operations")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    // ================================================================
    // Security Helper Methods
    // ================================================================

    /**
     * Gets the current authenticated user's customer ID.
     *
     * @return customer ID
     */
    private UUID getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found for user: " + username));

        return customer.getId();
    }

    /**
     * Gets the current authenticated username.
     *
     * @return username
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // ================================================================
    // Customer Order Operations
    // ================================================================

    /**
     * Creates a new order for the authenticated customer.
     * 
     * POST /api/orders
     * 
     * @param request the order creation request
     * @return created order response
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create new order", description = "Creates a new order for the authenticated customer from cart or direct items. Captures current prices and validates availability.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or items not available"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        UUID customerId = getCurrentCustomerId();
        log.info("Creating order for customer: {} with {} items", customerId, request.getItems().size());

        OrderResponse order = orderService.createOrder(customerId, request);

        log.info("Order created successfully: {} for customer: {}", order.getId(), customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Gets orders for the authenticated customer with pagination.
     * 
     * GET /api/orders
     * 
     * @param pageable pagination parameters
     * @return page of customer orders
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get customer orders", description = "Retrieves paginated list of orders for the authenticated customer, sorted by creation date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required")
    })
    public ResponseEntity<Page<OrderResponse>> getCustomerOrders(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        UUID customerId = getCurrentCustomerId();
        log.debug("Getting orders for customer: {}", customerId);

        Page<OrderResponse> orders = orderService.getCustomerOrders(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Gets a specific order by ID for the authenticated customer.
     *
     * GET /api/orders/{orderId}
     *
     * @param orderId the order ID
     * @return order details
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get specific order", description = "Retrieves a specific order by ID for the authenticated customer. Customers can only access their own orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - not order owner"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getCustomerOrder(
            @Parameter(description = "Order ID") @PathVariable UUID orderId) {
        UUID customerId = getCurrentCustomerId();
        log.debug("Getting order: {} for customer: {}", orderId, customerId);

        OrderResponse order = orderService.findOrderByIdAndCustomerId(orderId, customerId);
        return ResponseEntity.ok(order);
    }

    /**
     * Cancels an order for the authenticated customer.
     *
     * PATCH /api/orders/{orderId}/cancel
     *
     * @param orderId the order ID
     * @param reason  the cancellation reason
     * @return updated order response
     */
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel order", description = "Cancels an order for the authenticated customer. Only orders in certain statuses can be cancelled.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled in current status"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - not order owner"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Parameter(description = "Cancellation reason") @RequestParam String reason) {
        UUID customerId = getCurrentCustomerId();
        log.info("Cancelling order: {} for customer: {} with reason: {}", orderId, customerId, reason);

        OrderResponse order = orderService.cancelOrder(orderId, customerId, reason);

        log.info("Order cancelled successfully: {} for customer: {}", orderId, customerId);
        return ResponseEntity.ok(order);
    }

    // ================================================================
    // Staff Order Operations
    // ================================================================

    /**
     * Gets orders by status for staff operations.
     * 
     * GET /api/orders/staff/status/{status}
     * 
     * @param status   the order status
     * @param pageable pagination parameters
     * @return page of orders with the specified status
     */
    @GetMapping("/staff/status/{status}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get orders by status", description = "Retrieves paginated orders filtered by status. Staff operations endpoint for order management.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - staff role or higher required")
    })
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable OrderStatus status,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        log.debug("Getting orders with status: {} for staff", status);

        Page<OrderResponse> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Gets orders ready for preparation (kitchen view).
     * 
     * GET /api/orders/staff/kitchen
     * 
     * @return list of orders ready for preparation
     */
    @GetMapping("/staff/kitchen")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersForKitchen() {
        log.debug("Getting orders ready for preparation");

        List<OrderResponse> orders = orderService.getOrdersReadyForPreparation();
        return ResponseEntity.ok(orders);
    }

    /**
     * Gets orders ready for delivery.
     * 
     * GET /api/orders/staff/delivery
     * 
     * @return list of orders ready for delivery
     */
    @GetMapping("/staff/delivery")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersForDelivery() {
        log.debug("Getting orders ready for delivery");

        List<OrderResponse> orders = orderService.getOrdersReadyForDelivery();
        return ResponseEntity.ok(orders);
    }

    /**
     * Gets overdue orders for monitoring.
     * 
     * GET /api/orders/staff/overdue
     * 
     * @return list of overdue orders
     */
    @GetMapping("/staff/overdue")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOverdueOrders() {
        log.debug("Getting overdue orders");

        List<OrderResponse> orders = orderService.getOverdueOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Updates order status (staff operation).
     * 
     * PATCH /api/orders/{orderId}/status
     * 
     * @param orderId the order ID
     * @param request the status update request
     * @return updated order response
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Update order status", description = "Updates the status of an order. Only staff, managers, and admins can update order status. Validates status transitions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition or request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - staff role or higher required"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("Updating order status: {} to {} by user: {}",
                orderId, request.getStatus(), getCurrentUsername());

        OrderResponse order = orderService.updateOrderStatus(orderId, request);

        log.info("Order status updated successfully: {} to {}", orderId, request.getStatus());
        return ResponseEntity.ok(order);
    }

    // ================================================================
    // Manager/Admin Order Operations
    // ================================================================

    /**
     * Gets any order by ID (manager/admin operation).
     * 
     * GET /api/orders/admin/{orderId}
     * 
     * @param orderId the order ID
     * @return order details
     */
    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        log.debug("Getting order: {} for admin/manager", orderId);

        OrderResponse order = orderService.findOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Gets all orders with pagination (admin operation).
     * 
     * GET /api/orders/admin
     * 
     * @param status   optional status filter
     * @param pageable pagination parameters
     * @return page of orders
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        log.debug("Getting all orders for admin with status filter: {}", status);

        Page<OrderResponse> orders;
        if (status != null) {
            orders = orderService.getOrdersByStatus(status, pageable);
        } else {
            // This would need to be implemented in the service
            orders = orderService.getOrdersByStatus(null, pageable);
        }

        return ResponseEntity.ok(orders);
    }

    // ================================================================
    // Analytics and Reporting
    // ================================================================

    /**
     * Gets order statistics for dashboard.
     * 
     * GET /api/orders/analytics/statistics
     * 
     * @return order statistics
     */
    @GetMapping("/analytics/statistics")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<Object[]>> getOrderStatistics() {
        log.debug("Getting order statistics for analytics");

        List<Object[]> statistics = orderService.getOrderStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Calculates revenue for a date range.
     * 
     * GET /api/orders/analytics/revenue
     * 
     * @param startDate start of date range
     * @param endDate   end of date range
     * @return total revenue
     */
    @GetMapping("/analytics/revenue")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<BigDecimal> getRevenue(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        log.debug("Calculating revenue from {} to {}", startDate, endDate);

        BigDecimal revenue = orderService.calculateRevenueForPeriod(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    /**
     * Gets average order value.
     * 
     * GET /api/orders/analytics/average-value
     * 
     * @return average order value
     */
    @GetMapping("/analytics/average-value")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<BigDecimal> getAverageOrderValue() {
        log.debug("Getting average order value");

        BigDecimal averageValue = orderService.calculateAverageOrderValue();
        return ResponseEntity.ok(averageValue);
    }

    // ================================================================
    // Utility Endpoints
    // ================================================================

    /**
     * Checks if customer owns an order.
     * 
     * GET /api/orders/{orderId}/ownership
     * 
     * @param orderId the order ID
     * @return ownership status
     */
    @GetMapping("/{orderId}/ownership")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Boolean> checkOrderOwnership(@PathVariable UUID orderId) {
        UUID customerId = getCurrentCustomerId();

        boolean isOwner = orderService.isOrderOwnedByCustomer(orderId, customerId);
        return ResponseEntity.ok(isOwner);
    }

    /**
     * Gets valid order statuses for reference.
     * 
     * GET /api/orders/statuses
     * 
     * @return list of valid order statuses
     */
    @GetMapping("/statuses")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<OrderStatus[]> getOrderStatuses() {
        return ResponseEntity.ok(OrderStatus.values());
    }

    // ================================================================
    // Exception Handling
    // ================================================================

    /**
     * Handles IllegalStateException for business rule violations.
     * 
     * @param ex the exception
     * @return error response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Handles IllegalArgumentException for invalid requests.
     * 
     * @param ex the exception
     * @return error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
package com.raagaandroast.order.repository;

import com.raagaandroast.order.entity.Order;
import com.raagaandroast.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Order entity operations.
 * 
 * This repository demonstrates advanced JPA patterns:
 * - Custom JPQL queries with JOIN FETCH for performance
 * - @EntityGraph for solving N+1 problems
 * - Complex filtering and search capabilities
 * - Pagination support for large datasets
 * - Business-specific query methods
 * - Performance-optimized queries
 * 
 * Design Decisions:
 * - Extends JpaRepository for full CRUD operations
 * - Custom queries for business-specific operations
 * - JOIN FETCH to avoid N+1 problems
 * - @EntityGraph for complex relationship loading
 * - Pagination for scalable order listing
 * - Security-aware queries (customer ownership)
 * 
 * Interview Points:
 * - Why JOIN FETCH? Prevents N+1 when loading orders with items
 * - Why @EntityGraph? Declarative fetch strategy definition
 * - Why custom queries? Business logic requires complex filtering
 * - Why pagination? Orders can grow to millions of records
 * - Why Optional? Null-safe order retrieval
 * 
 * Performance Considerations:
 * - Use JOIN FETCH for orders with items
 * - Index on customer_id and status for fast filtering
 * - Pagination prevents memory issues with large result sets
 * - @EntityGraph reduces query count for complex relationships
 * 
 * @author RaagaAndRoast Development Team
 */
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // ================================================================
    // Basic Finder Methods
    // ================================================================

    /**
     * Find orders by customer ID with pagination.
     * 
     * Uses derived query method for simple customer filtering.
     * Supports pagination for large order histories.
     * 
     * @param customerId the customer ID
     * @param pageable   pagination information
     * @return page of orders for the customer
     */
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    /**
     * Find orders by status with pagination.
     * 
     * Used by staff to view orders in specific states.
     * 
     * @param status   the order status
     * @param pageable pagination information
     * @return page of orders with the specified status
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Find orders by customer and status.
     * 
     * Allows customers to filter their orders by status.
     * 
     * @param customerId the customer ID
     * @param status     the order status
     * @param pageable   pagination information
     * @return page of customer orders with the specified status
     */
    Page<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(
            UUID customerId, OrderStatus status, Pageable pageable);

    /**
     * Find orders created within a date range.
     * 
     * Used for reporting and analytics.
     * 
     * @param startDate start of date range
     * @param endDate   end of date range
     * @param pageable  pagination information
     * @return page of orders created within the date range
     */
    Page<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // ================================================================
    // Performance-Optimized Queries with JOIN FETCH
    // ================================================================

    /**
     * Find order by ID with order items loaded (JOIN FETCH).
     * 
     * This query demonstrates N+1 problem prevention:
     * - Without JOIN FETCH: 1 query for order + N queries for items
     * - With JOIN FETCH: 1 query for order and all items
     * 
     * Critical for order details display where items are always needed.
     * 
     * @param orderId the order ID
     * @return optional order with items loaded
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") UUID orderId);

    /**
     * Find order by ID with complete relationship graph.
     * 
     * Loads order with:
     * - Order items
     * - Menu items (for current details)
     * - Customer (for order context)
     * - Delivery address (for delivery info)
     * 
     * Use when complete order context is needed.
     * 
     * @param orderId the order ID
     * @return optional order with complete relationship graph
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem mi " +
            "LEFT JOIN FETCH mi.category " +
            "LEFT JOIN FETCH o.customer c " +
            "LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH o.deliveryAddress " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithCompleteGraph(@Param("orderId") UUID orderId);

    /**
     * Find customer orders with items (JOIN FETCH).
     * 
     * Optimized query for customer order history.
     * Loads orders with items to avoid N+1 problems.
     * 
     * @param customerId the customer ID
     * @param pageable   pagination information
     * @return page of orders with items loaded
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.customer.id = :customerId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdWithItems(@Param("customerId") UUID customerId);

    /**
     * Find orders by status with items (for staff operations).
     * 
     * Used by kitchen staff to see order details.
     * Loads items to avoid additional queries.
     * 
     * @param status the order status
     * @return list of orders with items loaded
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "WHERE o.status = :status " +
            "ORDER BY o.createdAt ASC")
    List<Order> findByStatusWithItems(@Param("status") OrderStatus status);

    // ================================================================
    // EntityGraph Examples
    // ================================================================

    /**
     * Find order using @EntityGraph for relationship loading.
     * 
     * Alternative to JOIN FETCH using declarative approach.
     * EntityGraph defines which relationships to load eagerly.
     * 
     * @param orderId the order ID
     * @return optional order with relationships loaded per EntityGraph
     */
    @EntityGraph(attributePaths = { "orderItems", "orderItems.menuItem", "customer", "deliveryAddress" })
    Optional<Order> findWithGraphById(UUID orderId);

    /**
     * Find customer orders using EntityGraph.
     * 
     * Demonstrates EntityGraph with derived query methods.
     * 
     * @param customerId the customer ID
     * @param pageable   pagination information
     * @return page of orders with relationships loaded
     */
    @EntityGraph(attributePaths = { "orderItems", "orderItems.menuItem" })
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    // ================================================================
    // Business Logic Queries
    // ================================================================

    /**
     * Find orders that can be cancelled.
     * 
     * Business rule: Only PENDING and CONFIRMED orders can be cancelled.
     * 
     * @param customerId the customer ID
     * @return list of cancellable orders
     */
    @Query("SELECT o FROM Order o " +
            "WHERE o.customer.id = :customerId " +
            "AND o.status IN ('PENDING', 'CONFIRMED') " +
            "ORDER BY o.createdAt DESC")
    List<Order> findCancellableOrdersByCustomer(@Param("customerId") UUID customerId);

    /**
     * Find orders ready for preparation.
     * 
     * Business query for kitchen operations.
     * 
     * @return list of confirmed orders ready for preparation
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "WHERE o.status = 'CONFIRMED' " +
            "ORDER BY o.confirmedAt ASC")
    List<Order> findOrdersReadyForPreparation();

    /**
     * Find orders ready for pickup/delivery.
     * 
     * Business query for order fulfillment.
     * 
     * @return list of ready orders
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.customer c " +
            "LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH o.deliveryAddress " +
            "WHERE o.status = 'READY' " +
            "ORDER BY o.readyAt ASC")
    List<Order> findOrdersReadyForDelivery();

    /**
     * Find overdue orders (taking longer than estimated).
     * 
     * Business query for monitoring order performance.
     * 
     * @return list of overdue orders
     */
    @Query("SELECT o FROM Order o " +
            "WHERE o.status IN ('CONFIRMED', 'PREPARING') " +
            "AND o.estimatedPrepTime IS NOT NULL " +
            "AND TIMESTAMPDIFF(MINUTE, o.confirmedAt, CURRENT_TIMESTAMP) > o.estimatedPrepTime " +
            "ORDER BY o.confirmedAt ASC")
    List<Order> findOverdueOrders();

    // ================================================================
    // Analytics and Reporting Queries
    // ================================================================

    /**
     * Count orders by status for dashboard.
     * 
     * @return count of orders by status
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    /**
     * Calculate total revenue for date range.
     * 
     * @param startDate start of date range
     * @param endDate   end of date range
     * @return total revenue
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueForPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find top customers by order count.
     * 
     * @param limit maximum number of customers to return
     * @return list of customer IDs and order counts
     */
    @Query("SELECT o.customer.id, COUNT(o) as orderCount " +
            "FROM Order o " +
            "WHERE o.status = 'COMPLETED' " +
            "GROUP BY o.customer.id " +
            "ORDER BY orderCount DESC")
    List<Object[]> findTopCustomersByOrderCount(Pageable pageable);

    /**
     * Calculate average order value.
     * 
     * @return average order value
     */
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal calculateAverageOrderValue();

    /**
     * Find orders with total amount above threshold.
     * 
     * @param threshold minimum order amount
     * @param pageable  pagination information
     * @return page of high-value orders
     */
    Page<Order> findByTotalAmountGreaterThanOrderByTotalAmountDesc(
            BigDecimal threshold, Pageable pageable);

    // ================================================================
    // Security and Ownership Queries
    // ================================================================

    /**
     * Check if order belongs to customer.
     * 
     * Security query for ownership verification.
     * 
     * @param orderId    the order ID
     * @param customerId the customer ID
     * @return true if order belongs to customer
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o " +
            "WHERE o.id = :orderId AND o.customer.id = :customerId")
    boolean existsByIdAndCustomerId(@Param("orderId") UUID orderId, @Param("customerId") UUID customerId);

    /**
     * Find order by ID and customer ID (security-aware).
     * 
     * Ensures customer can only access their own orders.
     * 
     * @param orderId    the order ID
     * @param customerId the customer ID
     * @return optional order if owned by customer
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "WHERE o.id = :orderId AND o.customer.id = :customerId")
    Optional<Order> findByIdAndCustomerIdWithItems(
            @Param("orderId") UUID orderId, @Param("customerId") UUID customerId);

    // ================================================================
    // Performance Monitoring Queries
    // ================================================================

    /**
     * Find orders with long preparation times.
     * 
     * @param thresholdMinutes minimum preparation time in minutes
     * @return list of slow orders
     */
    @Query("SELECT o FROM Order o " +
            "WHERE o.actualPrepTime > :thresholdMinutes " +
            "ORDER BY o.actualPrepTime DESC")
    List<Order> findOrdersWithLongPrepTime(@Param("thresholdMinutes") int thresholdMinutes);

    /**
     * Calculate average preparation time by status.
     * 
     * @return preparation time statistics
     */
    @Query("SELECT o.status, AVG(o.actualPrepTime) " +
            "FROM Order o " +
            "WHERE o.actualPrepTime IS NOT NULL " +
            "GROUP BY o.status")
    List<Object[]> calculateAveragePrepTimeByStatus();

    // ================================================================
    // Custom Update Operations
    // ================================================================

    /**
     * Update order status with timestamp.
     * 
     * Custom update query for status transitions.
     * Note: In practice, this would be handled by service layer
     * to ensure business rule validation.
     * 
     * @param orderId   the order ID
     * @param status    new status
     * @param timestamp status change timestamp
     * @return number of updated records
     */
    @Query("UPDATE Order o SET o.status = :status, o.updatedAt = :timestamp " +
            "WHERE o.id = :orderId")
    int updateOrderStatus(
            @Param("orderId") UUID orderId,
            @Param("status") OrderStatus status,
            @Param("timestamp") LocalDateTime timestamp);
}
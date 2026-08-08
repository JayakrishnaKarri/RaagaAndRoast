package com.raagaandroast.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.raagaandroast.cart.entity.Cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Cart entity operations.
 * 
 * This repository demonstrates advanced JPA patterns:
 * - Custom JPQL queries with JOIN FETCH for performance
 * - Named parameters for security and readability
 * - @Modifying queries for bulk operations
 * - Complex queries with aggregations
 * - Performance-optimized queries to prevent N+1 problems
 * 
 * Design Decisions:
 * - JOIN FETCH for loading cart with items in single query
 * - Custom queries for business-specific operations
 * - Bulk operations for maintenance tasks
 * - Aggregation queries for analytics
 * 
 * Interview Points:
 * - Why JOIN FETCH? Prevents N+1 query problem when loading cart items
 * - Why custom queries? JPA derived queries have limitations for complex
 * operations
 * - Why @Modifying? For UPDATE/DELETE operations that don't return entities
 * - Why named parameters? Security (prevents SQL injection) and readability
 * 
 * Performance Considerations:
 * - Use JOIN FETCH when cart items are needed
 * - Use simple findById when only cart metadata is needed
 * - Bulk operations for maintenance tasks
 * - Pagination for large result sets
 * 
 * @author RaagaAndRoast Development Team
 */

public interface CartRepository extends JpaRepository<Cart, UUID> {

    // ================================================================
    // Basic Cart Operations
    // ================================================================

    /**
     * Finds a cart by customer ID.
     * 
     * Uses simple query without JOIN FETCH for cases where
     * cart items are not needed immediately.
     * 
     * @param customerId the customer ID
     * @return optional cart
     */
    @Query("SELECT c FROM Cart c WHERE c.customer.id = :customerId")
    Optional<Cart> findByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Finds a cart by customer ID with cart items loaded.
     * 
     * Uses JOIN FETCH to load cart items in single query,
     * preventing N+1 query problem.
     * 
     * This is the preferred method when cart items will be accessed.
     * 
     * @param customerId the customer ID
     * @return optional cart with items loaded
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.menuItem WHERE c.customer.id = :customerId")
    Optional<Cart> findByCustomerIdWithItems(@Param("customerId") UUID customerId);

    /**
     * Finds a cart by ID with cart items loaded.
     * 
     * Uses JOIN FETCH to load cart items and menu items in single query.
     * Essential for preventing N+1 queries when displaying cart contents.
     * 
     * @param cartId the cart ID
     * @return optional cart with items loaded
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.menuItem WHERE c.id = :cartId")
    Optional<Cart> findByIdWithItems(@Param("cartId") UUID cartId);

    /**
     * Checks if a cart exists for a customer.
     * 
     * Efficient existence check without loading the entire entity.
     * 
     * @param customerId the customer ID
     * @return true if cart exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Cart c WHERE c.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") UUID customerId);

    // ================================================================
    // Cart Analytics and Reporting
    // ================================================================

    /**
     * Finds all non-empty carts.
     * 
     * Useful for analytics and reporting.
     * Returns carts that have items or positive total amount.
     * 
     * @return list of non-empty carts
     */
    @Query("SELECT c FROM Cart c WHERE c.totalAmount > 0 OR SIZE(c.cartItems) > 0")
    List<Cart> findNonEmptyCarts();

    /**
     * Finds carts with total amount greater than specified value.
     * 
     * Useful for identifying high-value carts for marketing purposes.
     * 
     * @param minAmount minimum cart total amount
     * @return list of carts with total >= minAmount
     */
    @Query("SELECT c FROM Cart c WHERE c.totalAmount >= :minAmount ORDER BY c.totalAmount DESC")
    List<Cart> findCartsWithTotalGreaterThan(@Param("minAmount") BigDecimal minAmount);

    /**
     * Finds abandoned carts (not updated recently).
     * 
     * Useful for cart abandonment analysis and recovery campaigns.
     * 
     * @param cutoffDate carts not updated since this date are considered abandoned
     * @return list of abandoned carts
     */
    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :cutoffDate AND c.totalAmount > 0 ORDER BY c.updatedAt ASC")
    List<Cart> findAbandonedCarts(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Gets cart statistics.
     * 
     * Returns aggregated data about all carts.
     * Useful for dashboard and analytics.
     * 
     * @return array with [totalCarts, nonEmptyCarts, avgCartValue, maxCartValue]
     */
    @Query("SELECT COUNT(c), " +
            "COUNT(CASE WHEN c.totalAmount > 0 THEN 1 END), " +
            "AVG(c.totalAmount), " +
            "MAX(c.totalAmount) " +
            "FROM Cart c")
    Object[] getCartStatistics();

    // ================================================================
    // Cart Maintenance Operations
    // ================================================================

    /**
     * Updates cart total amount.
     * 
     * Bulk operation to recalculate and update cart totals.
     * Useful for data consistency maintenance.
     * 
     * @param cartId   the cart ID
     * @param newTotal the new total amount
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE Cart c SET c.totalAmount = :newTotal, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :cartId")
    int updateCartTotal(@Param("cartId") UUID cartId, @Param("newTotal") BigDecimal newTotal);

    /**
     * Deletes empty carts older than specified date.
     * 
     * Maintenance operation to clean up old empty carts.
     * 
     * @param cutoffDate delete empty carts created before this date
     * @return number of deleted carts
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.totalAmount = 0 AND SIZE(c.cartItems) = 0 AND c.createdAt < :cutoffDate")
    int deleteEmptyCartsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Recalculates all cart totals.
     * 
     * Maintenance operation to ensure cart totals are accurate.
     * This is a complex query that recalculates totals from cart items.
     * 
     * @return number of updated carts
     */
    @Modifying
    @Query("UPDATE Cart c SET c.totalAmount = " +
            "(SELECT COALESCE(SUM(ci.subtotal), 0) FROM CartItem ci WHERE ci.cart.id = c.id), " +
            "c.updatedAt = CURRENT_TIMESTAMP")
    int recalculateAllCartTotals();

    // ================================================================
    // Advanced Queries for Business Logic
    // ================================================================

    /**
     * Finds carts containing a specific menu item.
     * 
     * Useful for impact analysis when menu items are discontinued
     * or when running promotions.
     * 
     * @param menuItemId the menu item ID
     * @return list of carts containing the menu item
     */
    @Query("SELECT DISTINCT c FROM Cart c JOIN c.cartItems ci WHERE ci.menuItem.id = :menuItemId")
    List<Cart> findCartsContainingMenuItem(@Param("menuItemId") UUID menuItemId);

    /**
     * Finds carts by customer username.
     * 
     * Useful for customer service operations.
     * 
     * @param username the customer's username
     * @return optional cart
     */
    @Query("SELECT c FROM Cart c WHERE c.customer.user.username = :username")
    Optional<Cart> findByCustomerUsername(@Param("username") String username);

    /**
     * Finds recently active carts.
     * 
     * Returns carts that have been updated within the specified time period.
     * Useful for monitoring active shopping sessions.
     * 
     * @param since find carts updated since this date
     * @return list of recently active carts
     */
    @Query("SELECT c FROM Cart c WHERE c.updatedAt >= :since ORDER BY c.updatedAt DESC")
    List<Cart> findRecentlyActiveCarts(@Param("since") LocalDateTime since);

    /**
     * Gets cart count by customer.
     * 
     * Should always return 0 or 1 since each customer has at most one cart.
     * Useful for data integrity checks.
     * 
     * @param customerId the customer ID
     * @return cart count for customer
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") UUID customerId);

    // ================================================================
    // Performance Monitoring Queries
    // ================================================================

    /**
     * Finds carts with many items.
     * 
     * Useful for identifying performance bottlenecks
     * and unusual usage patterns.
     * 
     * @param minItemCount minimum number of items
     * @return list of carts with many items
     */
    @Query("SELECT c FROM Cart c WHERE SIZE(c.cartItems) >= :minItemCount ORDER BY SIZE(c.cartItems) DESC")
    List<Cart> findCartsWithManyItems(@Param("minItemCount") int minItemCount);

    /**
     * Gets average cart item count.
     * 
     * Analytics query for understanding customer behavior.
     * 
     * @return average number of items per cart
     */
    @Query("SELECT AVG(SIZE(c.cartItems)) FROM Cart c WHERE SIZE(c.cartItems) > 0")
    Double getAverageCartItemCount();

    /**
     * Finds top carts by value.
     * 
     * Returns the highest value carts for analysis.
     * 
     * @param limit maximum number of results
     * @return list of top carts by value
     */
    @Query(value = "SELECT c FROM Cart c WHERE c.totalAmount > 0 ORDER BY c.totalAmount DESC")
    List<Cart> findTopCartsByValue(@Param("limit") int limit);
}
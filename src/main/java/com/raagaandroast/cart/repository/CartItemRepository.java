package com.raagaandroast.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.raagaandroast.cart.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for CartItem entity operations.
 * 
 * This repository provides data access methods for cart items with
 * focus on performance and business logic support.
 * 
 * Design Decisions:
 * - Custom queries for complex business operations
 * - JOIN FETCH to prevent N+1 problems
 * - Bulk operations for maintenance
 * - Analytics queries for business insights
 * 
 * Interview Points:
 * - Why separate CartItem repository? Different query patterns than Cart
 * - Why JOIN FETCH? Loading related entities efficiently
 * - Why bulk operations? Performance for maintenance tasks
 * - Why analytics queries? Business intelligence and reporting
 * 
 * @author RaagaAndRoast Development Team
 */

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    // ================================================================
    // Basic CartItem Operations
    // ================================================================

    /**
     * Finds all cart items for a specific cart.
     * 
     * @param cartId the cart ID
     * @return list of cart items
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId ORDER BY ci.createdAt ASC")
    List<CartItem> findByCartId(@Param("cartId") UUID cartId);

    /**
     * Finds all cart items for a specific cart with menu item details loaded.
     * 
     * Uses JOIN FETCH to load menu item in single query.
     * Essential for displaying cart contents without N+1 queries.
     * 
     * @param cartId the cart ID
     * @return list of cart items with menu items loaded
     */
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.menuItem WHERE ci.cart.id = :cartId ORDER BY ci.createdAt ASC")
    List<CartItem> findByCartIdWithMenuItems(@Param("cartId") UUID cartId);

    /**
     * Finds a cart item by cart and menu item.
     * 
     * Used to check if a menu item is already in the cart
     * before adding or to update existing item.
     * 
     * @param cartId     the cart ID
     * @param menuItemId the menu item ID
     * @return optional cart item
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.menuItem.id = :menuItemId")
    Optional<CartItem> findByCartIdAndMenuItemId(@Param("cartId") UUID cartId, @Param("menuItemId") UUID menuItemId);

    /**
     * Checks if a cart item exists for cart and menu item.
     * 
     * Efficient existence check without loading the entity.
     * 
     * @param cartId     the cart ID
     * @param menuItemId the menu item ID
     * @return true if cart item exists
     */
    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.menuItem.id = :menuItemId")
    boolean existsByCartIdAndMenuItemId(@Param("cartId") UUID cartId, @Param("menuItemId") UUID menuItemId);

    // ================================================================
    // Cart Item Analytics
    // ================================================================

    /**
     * Finds all cart items for a specific menu item.
     * 
     * Useful for analyzing menu item popularity and
     * impact analysis when discontinuing items.
     * 
     * @param menuItemId the menu item ID
     * @return list of cart items
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.menuItem.id = :menuItemId")
    List<CartItem> findByMenuItemId(@Param("menuItemId") UUID menuItemId);

    /**
     * Gets total quantity of a menu item across all carts.
     * 
     * Useful for inventory planning and demand analysis.
     * 
     * @param menuItemId the menu item ID
     * @return total quantity in all carts
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.menuItem.id = :menuItemId")
    Long getTotalQuantityForMenuItem(@Param("menuItemId") UUID menuItemId);

    /**
     * Gets cart items with quantity greater than specified value.
     * 
     * Useful for identifying bulk orders or unusual patterns.
     * 
     * @param minQuantity minimum quantity
     * @return list of cart items with high quantities
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.quantity >= :minQuantity ORDER BY ci.quantity DESC")
    List<CartItem> findItemsWithHighQuantity(@Param("minQuantity") int minQuantity);

    /**
     * Gets most popular menu items in carts.
     * 
     * Returns menu items ordered by how frequently they appear in carts.
     * 
     * @param limit maximum number of results
     * @return list of menu item IDs ordered by popularity
     */
    @Query("SELECT ci.menuItem.id, COUNT(ci) as itemCount " +
            "FROM CartItem ci " +
            "GROUP BY ci.menuItem.id " +
            "ORDER BY itemCount DESC")
    List<Object[]> findMostPopularMenuItems(@Param("limit") int limit);

    // ================================================================
    // Bulk Operations
    // ================================================================

    /**
     * Updates cart item quantity.
     * 
     * @param cartItemId  the cart item ID
     * @param newQuantity the new quantity
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :newQuantity, ci.updatedAt = CURRENT_TIMESTAMP WHERE ci.id = :cartItemId")
    int updateQuantity(@Param("cartItemId") UUID cartItemId, @Param("newQuantity") int newQuantity);

    /**
     * Updates cart item unit price.
     * 
     * Might be used for price adjustments or corrections.
     * 
     * @param cartItemId   the cart item ID
     * @param newUnitPrice the new unit price
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.unitPrice = :newUnitPrice, ci.updatedAt = CURRENT_TIMESTAMP WHERE ci.id = :cartItemId")
    int updateUnitPrice(@Param("cartItemId") UUID cartItemId, @Param("newUnitPrice") BigDecimal newUnitPrice);

    /**
     * Recalculates subtotals for all cart items.
     * 
     * Maintenance operation to ensure data consistency.
     * 
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.subtotal = ci.quantity * ci.unitPrice, ci.updatedAt = CURRENT_TIMESTAMP")
    int recalculateAllSubtotals();

    /**
     * Deletes cart items for a specific cart.
     * 
     * Used when clearing a cart.
     * 
     * @param cartId the cart ID
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    int deleteByCartId(@Param("cartId") UUID cartId);

    /**
     * Deletes cart items for a specific menu item.
     * 
     * Used when a menu item is discontinued.
     * 
     * @param menuItemId the menu item ID
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.menuItem.id = :menuItemId")
    int deleteByMenuItemId(@Param("menuItemId") UUID menuItemId);

    // ================================================================
    // Business Logic Support
    // ================================================================

    /**
     * Gets total cart value for a specific cart.
     * 
     * Calculates sum of all subtotals for cart items.
     * 
     * @param cartId the cart ID
     * @return total cart value
     */
    @Query("SELECT COALESCE(SUM(ci.subtotal), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    BigDecimal calculateCartTotal(@Param("cartId") UUID cartId);

    /**
     * Gets total item count for a specific cart.
     * 
     * Calculates sum of all quantities for cart items.
     * 
     * @param cartId the cart ID
     * @return total item count
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long calculateCartItemCount(@Param("cartId") UUID cartId);

    /**
     * Gets unique item count for a specific cart.
     * 
     * Counts number of different menu items in cart.
     * 
     * @param cartId the cart ID
     * @return unique item count
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long calculateUniqueItemCount(@Param("cartId") UUID cartId);

    /**
     * Finds cart items by customer ID.
     * 
     * Useful for customer service operations.
     * 
     * @param customerId the customer ID
     * @return list of cart items for customer
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer.id = :customerId")
    List<CartItem> findByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Finds cart items with subtotal greater than specified amount.
     * 
     * Useful for identifying high-value line items.
     * 
     * @param minSubtotal minimum subtotal amount
     * @return list of high-value cart items
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.subtotal >= :minSubtotal ORDER BY ci.subtotal DESC")
    List<CartItem> findItemsWithHighSubtotal(@Param("minSubtotal") BigDecimal minSubtotal);

    // ================================================================
    // Validation Support
    // ================================================================

    /**
     * Checks if cart has any items.
     * 
     * @param cartId the cart ID
     * @return true if cart has items
     */
    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci WHERE ci.cart.id = :cartId")
    boolean cartHasItems(@Param("cartId") UUID cartId);

    /**
     * Gets maximum quantity for any item in cart.
     * 
     * Useful for validation and business rule enforcement.
     * 
     * @param cartId the cart ID
     * @return maximum quantity in cart
     */
    @Query("SELECT COALESCE(MAX(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer getMaxQuantityInCart(@Param("cartId") UUID cartId);

    /**
     * Gets cart items that exceed quantity limit.
     * 
     * Useful for business rule validation.
     * 
     * @param cartId      the cart ID
     * @param maxQuantity maximum allowed quantity
     * @return list of cart items exceeding limit
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.quantity > :maxQuantity")
    List<CartItem> findItemsExceedingQuantityLimit(@Param("cartId") UUID cartId, @Param("maxQuantity") int maxQuantity);
}
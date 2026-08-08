package com.raagaandroast.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for cart item information.
 * 
 * This DTO represents a cart item in API responses.
 * It includes all information needed for displaying cart contents
 * without exposing internal entity details.
 * 
 * Design Decisions:
 * - Includes menu item details for display
 * - Shows price snapshot (unitPrice) from when item was added
 * - Calculated subtotal for convenience
 * - Audit information for transparency
 * - No sensitive internal data
 * 
 * Interview Points:
 * - Why include menu item details? Avoid additional API calls
 * - Why show unitPrice? Transparency about price at time of adding
 * - Why calculated subtotal? Client convenience and consistency
 * - Why audit fields? User experience and debugging
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    /**
     * Cart item ID.
     */
    private UUID id;

    /**
     * Menu item ID for reference.
     */
    private UUID menuItemId;

    /**
     * Menu item name for display.
     */
    private String menuItemName;

    /**
     * Menu item description for display.
     */
    private String menuItemDescription;

    /**
     * Current menu item price (may differ from unitPrice).
     */
    private BigDecimal currentMenuItemPrice;

    /**
     * Quantity of this item in the cart.
     */
    private Integer quantity;

    /**
     * Unit price when item was added to cart (price snapshot).
     */
    private BigDecimal unitPrice;

    /**
     * Subtotal for this cart item (quantity * unitPrice).
     */
    private BigDecimal subtotal;

    /**
     * Optional notes for this cart item.
     */
    private String notes;

    /**
     * When this item was added to the cart.
     */
    private LocalDateTime createdAt;

    /**
     * When this item was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Version for optimistic locking (useful for concurrent updates).
     */
    private Long version;

    /**
     * Indicates if the current menu item price differs from unit price.
     * Useful for showing price change notifications to users.
     */
    private Boolean priceChanged;

    /**
     * Menu item availability status.
     * Important for showing if item is still available.
     */
    private Boolean menuItemAvailable;

    /**
     * Category name for grouping/display purposes.
     */
    private String categoryName;
}
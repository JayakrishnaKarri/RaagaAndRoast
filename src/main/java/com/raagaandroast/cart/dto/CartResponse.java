package com.raagaandroast.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for cart information.
 * 
 * This DTO represents a complete cart with all its items in API responses.
 * It provides a comprehensive view of the cart state without exposing
 * internal entity details.
 * 
 * Design Decisions:
 * - Includes all cart items with full details
 * - Provides calculated totals and counts
 * - Shows audit information
 * - Includes customer reference for context
 * - No sensitive internal data
 * 
 * Interview Points:
 * - Why include items list? Complete cart view in single response
 * - Why calculated fields? Client convenience and performance
 * - Why audit fields? User experience and debugging
 * - Why customer info? Context and verification
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    /**
     * Cart ID.
     */
    private UUID id;

    /**
     * Customer ID who owns this cart.
     */
    private UUID customerId;

    /**
     * Customer name for display.
     */
    private String customerName;

    /**
     * List of items in the cart.
     */
    private List<CartItemResponse> items;

    /**
     * Total amount of the cart (sum of all item subtotals).
     */
    private BigDecimal totalAmount;

    /**
     * Total number of items in cart (sum of all quantities).
     */
    private Integer totalItemCount;

    /**
     * Number of unique items in cart (number of different menu items).
     */
    private Integer uniqueItemCount;

    /**
     * Indicates if the cart is empty.
     */
    private Boolean isEmpty;

    /**
     * When the cart was created.
     */
    private LocalDateTime createdAt;

    /**
     * When the cart was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Version for optimistic locking.
     */
    private Long version;

    /**
     * Indicates if any items in the cart have price changes.
     */
    private Boolean hasPriceChanges;

    /**
     * Indicates if any items in the cart are no longer available.
     */
    private Boolean hasUnavailableItems;

    /**
     * Summary of any issues with the cart (price changes, unavailable items, etc.).
     */
    private List<String> warnings;
}
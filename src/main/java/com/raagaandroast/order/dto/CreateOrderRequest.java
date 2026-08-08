package com.raagaandroast.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new order.
 * 
 * This DTO demonstrates:
 * - Comprehensive validation for order creation
 * - Nested DTO validation for order items
 * - Business rule validation at DTO level
 * - Clean separation between API and domain models
 * 
 * Design Decisions:
 * - Uses validation annotations for input validation
 * - Nested OrderItemRequest for order items
 * - Optional delivery address (for pickup vs delivery)
 * - Special instructions support
 * - Estimated prep time for kitchen planning
 * 
 * Interview Points:
 * - Why separate request DTO? API contract independence from domain
 * - Why validation here? Fail fast principle and API contract enforcement
 * - Why nested validation? Ensure order items are valid
 * - Why UUID for references? Consistent with entity design
 * 
 * Business Rules:
 * - Order must have at least one item
 * - All items must reference valid menu items
 * - Quantities must be positive
 * - Delivery address optional (pickup orders)
 * - Special instructions limited in length
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /**
     * List of items to include in the order.
     * 
     * Must contain at least one item.
     * Each item is validated individually.
     */
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    /**
     * Delivery address ID (optional for pickup orders).
     * 
     * If null, order is assumed to be for pickup.
     * If provided, must reference a valid address owned by the customer.
     */
    private UUID deliveryAddressId;

    /**
     * Special instructions for the entire order.
     * 
     * Optional field for customer notes.
     */
    @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
    private String specialInstructions;

    /**
     * Estimated preparation time in minutes (optional).
     * 
     * Can be provided by customer for scheduling purposes.
     * Kitchen can override this value.
     */
    @Positive(message = "Estimated prep time must be positive")
    private Integer estimatedPrepTime;

    /**
     * Nested DTO for order item requests.
     * 
     * Represents individual items within the order.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        /**
         * Menu item ID to order.
         * 
         * Must reference an existing and available menu item.
         */
        @NotNull(message = "Menu item ID is required")
        private UUID menuItemId;

        /**
         * Quantity to order.
         * 
         * Must be positive integer.
         */
        @Positive(message = "Quantity must be positive")
        private int quantity;

        /**
         * Special instructions for this specific item.
         * 
         * Optional customization notes.
         */
        @Size(max = 500, message = "Item special instructions cannot exceed 500 characters")
        private String specialInstructions;
    }

    // ================================================================
    // Validation Helper Methods
    // ================================================================

    /**
     * Checks if this is a delivery order.
     * 
     * @return true if delivery address is specified
     */
    public boolean isDeliveryOrder() {
        return deliveryAddressId != null;
    }

    /**
     * Checks if this is a pickup order.
     * 
     * @return true if no delivery address specified
     */
    public boolean isPickupOrder() {
        return deliveryAddressId == null;
    }

    /**
     * Gets total number of items in the order.
     * 
     * @return total item count
     */
    public int getTotalItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Gets total quantity across all items.
     * 
     * @return total quantity
     */
    public int getTotalQuantity() {
        return items != null ? items.stream().mapToInt(OrderItemRequest::getQuantity).sum() : 0;
    }

    /**
     * Checks if order has special instructions.
     * 
     * @return true if special instructions provided
     */
    public boolean hasSpecialInstructions() {
        return specialInstructions != null && !specialInstructions.trim().isEmpty();
    }

    /**
     * Checks if any item has special instructions.
     * 
     * @return true if any item has special instructions
     */
    public boolean hasItemSpecialInstructions() {
        return items != null && items.stream()
                .anyMatch(item -> item.getSpecialInstructions() != null &&
                        !item.getSpecialInstructions().trim().isEmpty());
    }
}
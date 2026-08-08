package com.raagaandroast.order.dto;

import com.raagaandroast.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for order information.
 * 
 * This DTO demonstrates:
 * - Complete order information for API responses
 * - Nested DTOs for order items and related entities
 * - Calculated fields for UI convenience
 * - Security-aware data exposure (no sensitive information)
 * - Comprehensive order lifecycle information
 * 
 * Design Decisions:
 * - Includes all order details needed by frontend
 * - Nested DTOs for order items with menu item details
 * - Customer and address information for context
 * - Status workflow information for UI state management
 * - Calculated totals and metrics for display
 * 
 * Interview Points:
 * - Why comprehensive DTO? Reduce API calls and improve performance
 * - Why nested DTOs? Provide complete context in single response
 * - Why calculated fields? Improve frontend performance and UX
 * - Why status workflow info? Enable proper UI state management
 * 
 * Security Considerations:
 * - No sensitive customer information exposed
 * - Price snapshots preserved for historical accuracy
 * - Order ownership verified at service layer
 * - Status transitions controlled by business rules
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /**
     * Order unique identifier.
     */
    private UUID id;

    /**
     * Order status with workflow information.
     */
    private OrderStatus status;

    /**
     * Customer information (basic details only).
     */
    private CustomerInfo customer;

    /**
     * Delivery address information (if applicable).
     */
    private AddressInfo deliveryAddress;

    /**
     * List of order items with menu item details.
     */
    private List<OrderItemResponse> items;

    /**
     * Order financial information.
     */
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;

    /**
     * Order timing information.
     */
    private Integer estimatedPrepTime;
    private Integer actualPrepTime;

    /**
     * Order lifecycle timestamps.
     */
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime preparationStartedAt;
    private LocalDateTime readyAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    /**
     * Special instructions and notes.
     */
    private String specialInstructions;
    private String cancellationReason;

    /**
     * Optimistic locking version.
     */
    private Long version;

    /**
     * Calculated fields for UI convenience.
     */
    private int totalItems;
    private int totalQuantity;
    private boolean isDelivery;
    private boolean canBeCancelled;
    private boolean canBeModified;
    private List<OrderStatus> validNextStatuses;

    // ================================================================
    // Nested DTOs
    // ================================================================

    /**
     * Customer information DTO (basic details only).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
    }

    /**
     * Address information DTO.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private UUID id;
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String type;
    }

    /**
     * Order item response DTO.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private UUID id;
        private MenuItemInfo menuItem;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String specialInstructions;

        // Historical menu item information (snapshot)
        private String menuItemName;
        private String menuItemDescription;
        private String categoryName;

        // Price comparison information
        private BigDecimal currentPrice;
        private boolean priceChanged;
        private BigDecimal priceDifference;
    }

    /**
     * Menu item information DTO (current details).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemInfo {
        private UUID id;
        private String name;
        private String description;
        private BigDecimal price;
        private boolean available;
        private String categoryName;
    }

    // ================================================================
    // Convenience Methods
    // ================================================================

    /**
     * Checks if this is a delivery order.
     * 
     * @return true if delivery address is present
     */
    public boolean isDeliveryOrder() {
        return deliveryAddress != null;
    }

    /**
     * Checks if this is a pickup order.
     * 
     * @return true if no delivery address
     */
    public boolean isPickupOrder() {
        return deliveryAddress == null;
    }

    /**
     * Gets the order type as string.
     * 
     * @return "DELIVERY" or "PICKUP"
     */
    public String getOrderType() {
        return isDeliveryOrder() ? "DELIVERY" : "PICKUP";
    }

    /**
     * Checks if order is in a terminal state.
     * 
     * @return true if order is completed or cancelled
     */
    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    /**
     * Checks if order is active (not terminal).
     * 
     * @return true if order is still being processed
     */
    public boolean isActive() {
        return !isTerminal();
    }

    /**
     * Gets customer full name.
     * 
     * @return formatted customer name
     */
    public String getCustomerFullName() {
        if (customer == null)
            return null;
        return String.format("%s %s",
                customer.getFirstName() != null ? customer.getFirstName() : "",
                customer.getLastName() != null ? customer.getLastName() : "").trim();
    }

    /**
     * Gets formatted delivery address.
     * 
     * @return formatted address string
     */
    public String getFormattedDeliveryAddress() {
        if (deliveryAddress == null)
            return null;
        return String.format("%s, %s, %s %s",
                deliveryAddress.getStreet(),
                deliveryAddress.getCity(),
                deliveryAddress.getState(),
                deliveryAddress.getZipCode());
    }

    /**
     * Calculates preparation time status.
     * 
     * @return preparation status description
     */
    public String getPreparationStatus() {
        if (actualPrepTime != null && estimatedPrepTime != null) {
            if (actualPrepTime <= estimatedPrepTime) {
                return "ON_TIME";
            } else {
                return "DELAYED";
            }
        } else if (estimatedPrepTime != null && preparationStartedAt != null) {
            long elapsedMinutes = java.time.Duration.between(
                    preparationStartedAt, LocalDateTime.now()).toMinutes();
            if (elapsedMinutes > estimatedPrepTime) {
                return "OVERDUE";
            } else {
                return "IN_PROGRESS";
            }
        }
        return "UNKNOWN";
    }

    /**
     * Gets estimated completion time.
     * 
     * @return estimated completion timestamp
     */
    public LocalDateTime getEstimatedCompletionTime() {
        if (preparationStartedAt != null && estimatedPrepTime != null) {
            return preparationStartedAt.plusMinutes(estimatedPrepTime);
        } else if (confirmedAt != null && estimatedPrepTime != null) {
            return confirmedAt.plusMinutes(estimatedPrepTime);
        }
        return null;
    }

    /**
     * Checks if order has any special instructions.
     * 
     * @return true if order or any item has special instructions
     */
    public boolean hasSpecialInstructions() {
        if (specialInstructions != null && !specialInstructions.trim().isEmpty()) {
            return true;
        }
        return items != null && items.stream()
                .anyMatch(item -> item.getSpecialInstructions() != null &&
                        !item.getSpecialInstructions().trim().isEmpty());
    }

    /**
     * Checks if any item prices have changed since ordering.
     * 
     * @return true if any item price has changed
     */
    public boolean hasItemPriceChanges() {
        return items != null && items.stream()
                .anyMatch(OrderItemResponse::isPriceChanged);
    }

    /**
     * Calculates total savings if prices have increased.
     * 
     * @return total amount saved due to price snapshots
     */
    public BigDecimal getTotalSavings() {
        if (items == null)
            return BigDecimal.ZERO;

        return items.stream()
                .filter(item -> item.isPriceChanged() &&
                        item.getPriceDifference().compareTo(BigDecimal.ZERO) > 0)
                .map(item -> item.getPriceDifference().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Gets order duration in minutes (from creation to completion).
     * 
     * @return order duration in minutes, null if not completed
     */
    public Long getOrderDurationMinutes() {
        if (createdAt != null && completedAt != null) {
            return java.time.Duration.between(createdAt, completedAt).toMinutes();
        }
        return null;
    }

    /**
     * Gets preparation duration in minutes.
     * 
     * @return preparation duration, null if not started or completed
     */
    public Long getPreparationDurationMinutes() {
        if (preparationStartedAt != null && readyAt != null) {
            return java.time.Duration.between(preparationStartedAt, readyAt).toMinutes();
        }
        return null;
    }
}
package com.raagaandroast.order.mapper;

import com.raagaandroast.customer.entity.Address;
import com.raagaandroast.customer.entity.Customer;
import com.raagaandroast.menu.entity.MenuItem;
import com.raagaandroast.order.dto.CreateOrderRequest;
import com.raagaandroast.order.dto.OrderResponse;
import com.raagaandroast.order.entity.Order;
import com.raagaandroast.order.entity.OrderItem;
import com.raagaandroast.order.entity.OrderStatus;
import com.raagaandroast.user.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Order entities and DTOs.
 * 
 * This mapper demonstrates:
 * - Complex entity to DTO mapping with nested relationships
 * - Price snapshot handling for historical accuracy
 * - Calculated fields for UI convenience
 * - Security-aware data mapping (no sensitive information)
 * - Performance-conscious mapping strategies
 * 
 * Design Decisions:
 * - Manual mapping for full control over transformation
 * - Null-safe mapping methods
 * - Calculated fields computed during mapping
 * - Nested DTO creation for complete context
 * - Price comparison logic for business intelligence
 * 
 * Interview Points:
 * - Why manual mapping? Full control over complex transformations
 * - Why calculated fields? Reduce frontend computation and API calls
 * - Why null safety? Prevent NPE in production environments
 * - Why nested DTOs? Provide complete context in single response
 * 
 * Performance Considerations:
 * - Lazy loading awareness (entities should be fetched with JOIN FETCH)
 * - Efficient stream operations for collections
 * - Minimal object creation during mapping
 * - Reusable mapping methods for consistency
 * 
 * @author RaagaAndRoast Development Team
 */
@Component
public class OrderMapper {

    // ================================================================
    // Entity to DTO Mapping
    // ================================================================

    /**
     * Maps Order entity to OrderResponse DTO.
     * 
     * Creates comprehensive response with all order details,
     * calculated fields, and nested information.
     * 
     * @param order the order entity (should be fetched with relationships)
     * @return complete order response DTO
     */
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();

        // Basic order information
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setVersion(order.getVersion());

        // Financial information
        response.setSubtotal(order.getSubtotal());
        response.setTaxAmount(order.getTaxAmount());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotalAmount(order.getTotalAmount());

        // Timing information
        response.setEstimatedPrepTime(order.getEstimatedPrepTime());
        response.setActualPrepTime(order.getActualPrepTime());

        // Lifecycle timestamps
        response.setCreatedAt(order.getCreatedAt());
        response.setConfirmedAt(order.getConfirmedAt());
        response.setPreparationStartedAt(order.getPreparationStartedAt());
        response.setReadyAt(order.getReadyAt());
        response.setCompletedAt(order.getCompletedAt());
        response.setCancelledAt(order.getCancelledAt());

        // Instructions and notes
        response.setSpecialInstructions(order.getSpecialInstructions());
        response.setCancellationReason(order.getCancellationReason());

        // Customer information
        response.setCustomer(mapCustomerInfo(order.getCustomer()));

        // Delivery address information
        response.setDeliveryAddress(mapAddressInfo(order.getDeliveryAddress()));

        // Order items with menu item details
        response.setItems(mapOrderItems(order.getOrderItems()));

        // Calculated fields for UI convenience
        response.setTotalItems(order.getOrderItems() != null ? order.getOrderItems().size() : 0);
        response.setTotalQuantity(calculateTotalQuantity(order.getOrderItems()));
        response.setDelivery(order.getDeliveryAddress() != null);
        response.setCanBeCancelled(canBeCancelled(order.getStatus()));
        response.setCanBeModified(canBeModified(order.getStatus()));
        response.setValidNextStatuses(getValidNextStatuses(order.getStatus()));

        return response;
    }

    /**
     * Maps Customer entity to CustomerInfo DTO.
     * 
     * @param customer the customer entity
     * @return customer info DTO with basic details
     */
    private OrderResponse.CustomerInfo mapCustomerInfo(Customer customer) {
        if (customer == null) {
            return null;
        }

        User user = customer.getUser();
        if (user == null) {
            return null;
        }

        return new OrderResponse.CustomerInfo(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                user.getEmail(),
                customer.getPhoneNumber() // Using correct method name
        );
    }

    /**
     * Maps Address entity to AddressInfo DTO.
     * 
     * @param address the address entity
     * @return address info DTO
     */
    private OrderResponse.AddressInfo mapAddressInfo(Address address) {
        if (address == null) {
            return null;
        }

        return new OrderResponse.AddressInfo(
                address.getId(),
                address.getStreetAddress(), // Using correct method name
                address.getCity(),
                address.getState(),
                address.getPostalCode(), // Using correct method name
                address.getType() != null ? address.getType().name() : null);
    }

    /**
     * Maps list of OrderItem entities to OrderItemResponse DTOs.
     * 
     * @param orderItems the order items
     * @return list of order item response DTOs
     */
    private List<OrderResponse.OrderItemResponse> mapOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return null;
        }

        return orderItems.stream()
                .map(this::mapOrderItem)
                .collect(Collectors.toList());
    }

    /**
     * Maps OrderItem entity to OrderItemResponse DTO.
     * 
     * Includes price comparison logic and menu item details.
     * 
     * @param orderItem the order item entity
     * @return order item response DTO
     */
    private OrderResponse.OrderItemResponse mapOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderResponse.OrderItemResponse response = new OrderResponse.OrderItemResponse();

        // Basic order item information
        response.setId(orderItem.getId());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setSubtotal(orderItem.getSubtotal());
        response.setSpecialInstructions(orderItem.getSpecialInstructions());

        // Historical menu item information (snapshot)
        response.setMenuItemName(orderItem.getMenuItemName());
        response.setMenuItemDescription(orderItem.getMenuItemDescription());
        response.setCategoryName(orderItem.getCategoryName());

        // Current menu item information
        response.setMenuItem(mapMenuItemInfo(orderItem.getMenuItem()));

        // Price comparison information
        BigDecimal currentPrice = orderItem.getCurrentMenuItemPrice();
        response.setCurrentPrice(currentPrice);
        response.setPriceChanged(orderItem.hasPriceChanged());
        response.setPriceDifference(orderItem.getPriceDifference());

        return response;
    }

    /**
     * Maps MenuItem entity to MenuItemInfo DTO.
     * 
     * @param menuItem the menu item entity
     * @return menu item info DTO with current details
     */
    private OrderResponse.MenuItemInfo mapMenuItemInfo(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }

        return new OrderResponse.MenuItemInfo(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getAvailable(),
                menuItem.getCategory() != null ? menuItem.getCategory().getName() : null);
    }

    // ================================================================
    // DTO to Entity Mapping
    // ================================================================

    /**
     * Creates Order entity from CreateOrderRequest DTO.
     * 
     * Note: This method creates the basic order structure.
     * Order items and relationships are set by the service layer.
     * 
     * @param request         the create order request
     * @param customer        the customer entity
     * @param deliveryAddress the delivery address entity (optional)
     * @return new order entity
     */
    public Order toOrderEntity(CreateOrderRequest request, Customer customer, Address deliveryAddress) {
        if (request == null || customer == null) {
            return null;
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryAddress(deliveryAddress);
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setEstimatedPrepTime(request.getEstimatedPrepTime());

        // Order items will be added by the service layer
        // Status, totals, and timestamps are set by business logic

        return order;
    }

    /**
     * Creates OrderItem entity from OrderItemRequest DTO.
     * 
     * @param request  the order item request
     * @param order    the parent order
     * @param menuItem the menu item entity
     * @return new order item entity
     */
    public OrderItem toOrderItemEntity(CreateOrderRequest.OrderItemRequest request,
            Order order, MenuItem menuItem) {
        if (request == null || order == null || menuItem == null) {
            return null;
        }

        return new OrderItem(order, menuItem, request.getQuantity(), request.getSpecialInstructions());
    }

    // ================================================================
    // Utility Methods
    // ================================================================

    /**
     * Calculates total quantity across all order items.
     * 
     * @param orderItems the order items
     * @return total quantity
     */
    private int calculateTotalQuantity(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return 0;
        }

        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Determines if order can be cancelled based on status.
     * 
     * @param status the order status
     * @return true if order can be cancelled
     */
    private boolean canBeCancelled(OrderStatus status) {
        if (status == null) {
            return false;
        }
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /**
     * Determines if order can be modified based on status.
     * 
     * @param status the order status
     * @return true if order can be modified
     */
    private boolean canBeModified(OrderStatus status) {
        if (status == null) {
            return false;
        }
        return status == OrderStatus.PENDING;
    }

    /**
     * Gets valid next statuses for the current status.
     * 
     * @param status the current status
     * @return list of valid next statuses
     */
    private List<OrderStatus> getValidNextStatuses(OrderStatus status) {
        if (status == null) {
            return Arrays.asList(OrderStatus.PENDING);
        }
        return Arrays.asList(status.getValidNextStatuses());
    }

    /**
     * Maps list of orders to order response DTOs.
     * 
     * Convenience method for mapping collections.
     * 
     * @param orders the order entities
     * @return list of order response DTOs
     */
    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        if (orders == null) {
            return null;
        }

        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a summary order response with minimal details.
     * 
     * Used for order lists where full details are not needed.
     * Improves performance by avoiding deep object graph traversal.
     * 
     * @param order the order entity
     * @return summary order response
     */
    public OrderResponse toOrderSummaryResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();

        // Basic information only
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setDelivery(order.getDeliveryAddress() != null);

        // Minimal customer info
        if (order.getCustomer() != null) {
            Customer customer = order.getCustomer();
            response.setCustomer(new OrderResponse.CustomerInfo(
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    null, // Don't include email in summary
                    null // Don't include phone in summary
            ));
        }

        // Item count without loading full items
        response.setTotalItems(order.getOrderItems() != null ? order.getOrderItems().size() : 0);

        return response;
    }
}
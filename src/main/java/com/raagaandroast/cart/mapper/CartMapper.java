package com.raagaandroast.cart.mapper;

import org.springframework.stereotype.Component;

import com.raagaandroast.cart.dto.CartItemResponse;
import com.raagaandroast.cart.dto.CartResponse;
import com.raagaandroast.cart.entity.Cart;
import com.raagaandroast.cart.entity.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Cart entities and DTOs.
 * 
 * This mapper demonstrates clean separation between entity and API layers:
 * - Manual mapping for full control over transformations
 * - Business logic integration (price change detection, availability checks)
 * - Performance considerations (avoiding N+1 queries)
 * - Security considerations (not exposing sensitive data)
 * 
 * Design Decisions:
 * - Manual mapping instead of MapStruct for educational purposes
 * - Business logic integration for enhanced responses
 * - Null safety throughout
 * - Performance-conscious implementations
 * 
 * Interview Points:
 * - Why manual mapping vs MapStruct? Full control and educational value
 * - Why business logic in mapper? Enhanced API responses
 * - Why null checks? Defensive programming and robustness
 * - Why separate methods? Single Responsibility Principle
 * 
 * @author RaagaAndRoast Development Team
 */
@Component
public class CartMapper {

    /**
     * Converts Cart entity to CartResponse DTO.
     * 
     * This method creates a comprehensive cart response with:
     * - All cart items with full details
     * - Calculated totals and counts
     * - Business logic indicators (price changes, availability)
     * - Audit information
     * 
     * @param cart the cart entity
     * @return cart response DTO
     */
    public CartResponse toCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        // Convert cart items
        List<CartItemResponse> itemResponses = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                        .map(this::toCartItemResponse)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        // Calculate business indicators
        boolean hasPriceChanges = itemResponses.stream()
                .anyMatch(item -> Boolean.TRUE.equals(item.getPriceChanged()));

        boolean hasUnavailableItems = itemResponses.stream()
                .anyMatch(item -> Boolean.FALSE.equals(item.getMenuItemAvailable()));

        // Generate warnings
        List<String> warnings = new ArrayList<>();
        if (hasPriceChanges) {
            warnings.add("Some items have price changes since they were added to cart");
        }
        if (hasUnavailableItems) {
            warnings.add("Some items are no longer available");
        }

        return CartResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null)
                .customerName(cart.getCustomer() != null ? cart.getCustomer().getFullName() : null)
                .items(itemResponses)
                .totalAmount(cart.getTotalAmount() != null ? cart.getTotalAmount() : BigDecimal.ZERO)
                .totalItemCount(cart.getTotalItemCount())
                .uniqueItemCount(cart.getUniqueItemCount())
                .isEmpty(cart.isEmpty())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .version(cart.getVersion())
                .hasPriceChanges(hasPriceChanges)
                .hasUnavailableItems(hasUnavailableItems)
                .warnings(warnings.isEmpty() ? null : warnings)
                .build();
    }

    /**
     * Converts CartItem entity to CartItemResponse DTO.
     * 
     * This method creates a comprehensive cart item response with:
     * - Menu item details for display
     * - Price comparison (current vs when added)
     * - Availability status
     * - Calculated fields
     * 
     * @param cartItem the cart item entity
     * @return cart item response DTO
     */
    public CartItemResponse toCartItemResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        // Extract menu item details
        String menuItemName = null;
        String menuItemDescription = null;
        BigDecimal currentMenuItemPrice = null;
        Boolean menuItemAvailable = null;
        String categoryName = null;

        if (cartItem.getMenuItem() != null) {
            menuItemName = cartItem.getMenuItem().getName();
            menuItemDescription = cartItem.getMenuItem().getDescription();
            currentMenuItemPrice = cartItem.getMenuItem().getPrice();
            menuItemAvailable = cartItem.getMenuItem().getAvailable();

            if (cartItem.getMenuItem().getCategory() != null) {
                categoryName = cartItem.getMenuItem().getCategory().getName();
            }
        }

        // Determine if price has changed
        Boolean priceChanged = false;
        if (cartItem.getUnitPrice() != null && currentMenuItemPrice != null) {
            priceChanged = cartItem.getUnitPrice().compareTo(currentMenuItemPrice) != 0;
        }

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .menuItemId(cartItem.getMenuItem() != null ? cartItem.getMenuItem().getId() : null)
                .menuItemName(menuItemName)
                .menuItemDescription(menuItemDescription)
                .currentMenuItemPrice(currentMenuItemPrice)
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .subtotal(cartItem.getSubtotal())
                .notes(null) // Notes field would be added to CartItem entity if needed
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .version(cartItem.getVersion())
                .priceChanged(priceChanged)
                .menuItemAvailable(menuItemAvailable)
                .categoryName(categoryName)
                .build();
    }

    /**
     * Converts a list of Cart entities to CartResponse DTOs.
     * 
     * @param carts list of cart entities
     * @return list of cart response DTOs
     */
    public List<CartResponse> toCartResponseList(List<Cart> carts) {
        if (carts == null) {
            return new ArrayList<>();
        }

        return carts.stream()
                .map(this::toCartResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of CartItem entities to CartItemResponse DTOs.
     * 
     * @param cartItems list of cart item entities
     * @return list of cart item response DTOs
     */
    public List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems) {
        if (cartItems == null) {
            return new ArrayList<>();
        }

        return cartItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a simple cart response for cases where full details aren't needed.
     * 
     * This is useful for operations that return basic cart information
     * without loading all cart items.
     * 
     * @param cart the cart entity
     * @return simplified cart response DTO
     */
    public CartResponse toSimpleCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        return CartResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null)
                .customerName(cart.getCustomer() != null ? cart.getCustomer().getFullName() : null)
                .items(new ArrayList<>()) // Empty list for simple response
                .totalAmount(cart.getTotalAmount() != null ? cart.getTotalAmount() : BigDecimal.ZERO)
                .totalItemCount(cart.getTotalItemCount())
                .uniqueItemCount(cart.getUniqueItemCount())
                .isEmpty(cart.isEmpty())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .version(cart.getVersion())
                .hasPriceChanges(false) // Not calculated for simple response
                .hasUnavailableItems(false) // Not calculated for simple response
                .warnings(null)
                .build();
    }

    /**
     * Creates a cart summary response with minimal information.
     * 
     * Useful for displaying cart status in headers or navigation.
     * 
     * @param cart the cart entity
     * @return cart summary response
     */
    public CartResponse toCartSummary(Cart cart) {
        if (cart == null) {
            return CartResponse.builder()
                    .isEmpty(true)
                    .totalAmount(BigDecimal.ZERO)
                    .totalItemCount(0)
                    .uniqueItemCount(0)
                    .build();
        }

        return CartResponse.builder()
                .id(cart.getId())
                .totalAmount(cart.getTotalAmount() != null ? cart.getTotalAmount() : BigDecimal.ZERO)
                .totalItemCount(cart.getTotalItemCount())
                .uniqueItemCount(cart.getUniqueItemCount())
                .isEmpty(cart.isEmpty())
                .build();
    }
}
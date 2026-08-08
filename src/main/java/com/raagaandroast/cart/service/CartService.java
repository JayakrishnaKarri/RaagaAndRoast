package com.raagaandroast.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raagaandroast.cart.dto.AddCartItemRequest;
import com.raagaandroast.cart.dto.CartResponse;
import com.raagaandroast.cart.dto.UpdateCartItemRequest;
import com.raagaandroast.cart.entity.Cart;
import com.raagaandroast.cart.entity.CartItem;
import com.raagaandroast.cart.mapper.CartMapper;
import com.raagaandroast.cart.repository.CartRepository;
import com.raagaandroast.common.exception.EmptyCartException;
import com.raagaandroast.common.exception.InvalidQuantityException;
import com.raagaandroast.common.exception.MenuItemUnavailableException;
import com.raagaandroast.common.exception.ResourceNotFoundException;
import com.raagaandroast.customer.entity.Customer;
import com.raagaandroast.customer.repository.CustomerRepository;
import com.raagaandroast.menu.entity.MenuItem;
import com.raagaandroast.menu.repository.MenuItemRepository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service class for cart operations.
 * 
 * This service demonstrates advanced Spring patterns:
 * - @Transactional boundaries for data consistency
 * - Complex business logic with validation
 * - Performance optimization with JOIN FETCH
 * - Proper exception handling
 * - BigDecimal arithmetic for monetary calculations
 * - Optimistic locking handling
 * 
 * Design Decisions:
 * - @Transactional at method level for precise control
 * - Business validation before persistence
 * - Price snapshot capture for cart items
 * - Comprehensive error handling
 * - Performance-conscious repository usage
 * 
 * Interview Points:
 * - Why @Transactional? Data consistency for cart operations
 * - Why capture price snapshot? Menu prices can change
 * - Why validate availability? Business rule enforcement
 * - Why optimistic locking? Concurrent cart access
 * - Why BigDecimal? Precise monetary calculations
 * 
 * @author RaagaAndRoast Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartService {

        private final CartRepository cartRepository;
        private final CustomerRepository customerRepository;
        private final MenuItemRepository menuItemRepository;
        private final CartMapper cartMapper;

        // ================================================================
        // Cart Retrieval Operations
        // ================================================================

        /**
         * Gets the cart for a customer.
         * 
         * If no cart exists, creates a new one.
         * Uses JOIN FETCH to load cart items efficiently.
         * 
         * @param customerId the customer ID
         * @return cart response DTO
         */
        public CartResponse getCartByCustomerId(UUID customerId) {
                log.debug("Getting cart for customer: {}", customerId);

                // Use optimized query to load customer with user relationship
                Customer customer = customerRepository.findByIdWithAllRelationships(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Customer not found with id: " + customerId));

                Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                                .orElseGet(() -> createNewCartForCustomer(customer));

                return cartMapper.toCartResponse(cart);
        }

        /**
         * Gets cart summary (without items) for performance.
         * 
         * Useful for displaying cart status in navigation.
         * 
         * @param customerId the customer ID
         * @return cart summary response
         */
        public CartResponse getCartSummary(UUID customerId) {
                log.debug("Getting cart summary for customer: {}", customerId);

                Cart cart = cartRepository.findByCustomerId(customerId).orElse(null);
                return cartMapper.toCartSummary(cart);
        }

        // ================================================================
        // Cart Item Operations
        // ================================================================

        /**
         * Adds an item to the customer's cart.
         * 
         * Business Logic:
         * - Validates menu item exists and is available
         * - Captures current price as snapshot
         * - If item already exists, increases quantity
         * - If new item, creates new cart item
         * - Recalculates cart total
         * 
         * @param customerId the customer ID
         * @param request    the add cart item request
         * @return updated cart response
         */
        @Transactional
        public CartResponse addItemToCart(UUID customerId, AddCartItemRequest request) {
                log.debug("Adding item to cart - Customer: {}, MenuItem: {}, Quantity: {}",
                                customerId, request.getMenuItemId(), request.getQuantity());

                // Validate customer exists (use optimized query)
                Customer customer = customerRepository.findByIdWithAllRelationships(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Customer not found with id: " + customerId));

                // Validate menu item exists and is available (use optimized query with
                // category)
                MenuItem menuItem = menuItemRepository.findByIdWithCategory(request.getMenuItemId())
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Menu item not found with id: "
                                                                + request.getMenuItemId()));

                if (!menuItem.getAvailable()) {
                        throw new MenuItemUnavailableException(menuItem.getName(), "add to cart");
                }

                // Get or create cart
                Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                                .orElseGet(() -> createNewCartForCustomer(customer));

                // Check if item already exists in cart
                CartItem existingCartItem = cart.findCartItemByMenuItemId(request.getMenuItemId());

                if (existingCartItem != null) {
                        // Update existing item quantity
                        existingCartItem.increaseQuantity(request.getQuantity());
                        log.debug("Updated existing cart item quantity to: {}", existingCartItem.getQuantity());
                } else {
                        // Create new cart item with current price snapshot
                        CartItem newCartItem = new CartItem(cart, menuItem, request.getQuantity(), menuItem.getPrice());
                        cart.addCartItem(newCartItem);
                        log.debug("Added new cart item: {}", newCartItem);
                }

                // Save cart (cascade will save cart items)
                Cart savedCart = cartRepository.save(cart);

                log.info("Successfully added item to cart - Customer: {}, MenuItem: {}, New Total: {}",
                                customerId, request.getMenuItemId(), savedCart.getTotalAmount());

                return cartMapper.toCartResponse(savedCart);
        }

        /**
         * Updates the quantity of a cart item.
         * 
         * @param customerId the customer ID
         * @param cartItemId the cart item ID
         * @param request    the update request
         * @return updated cart response
         */
        @Transactional
        public CartResponse updateCartItemQuantity(UUID customerId, UUID cartItemId, UpdateCartItemRequest request) {
                log.debug("Updating cart item quantity - Customer: {}, CartItem: {}, New Quantity: {}",
                                customerId, cartItemId, request.getQuantity());

                // Get cart with items
                Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart not found for customer: " + customerId));

                // Find the cart item
                CartItem cartItem = cart.getCartItems().stream()
                                .filter(item -> item.getId().equals(cartItemId))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart item not found with id: " + cartItemId));

                // Update quantity
                cartItem.updateQuantity(request.getQuantity());

                // Save cart
                Cart savedCart = cartRepository.save(cart);

                log.info("Successfully updated cart item quantity - Customer: {}, CartItem: {}, New Total: {}",
                                customerId, cartItemId, savedCart.getTotalAmount());

                return cartMapper.toCartResponse(savedCart);
        }

        /**
         * Removes an item from the cart.
         * 
         * @param customerId the customer ID
         * @param cartItemId the cart item ID
         * @return updated cart response
         */
        @Transactional
        public CartResponse removeItemFromCart(UUID customerId, UUID cartItemId) {
                log.debug("Removing item from cart - Customer: {}, CartItem: {}", customerId, cartItemId);

                // Get cart with items
                Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart not found for customer: " + customerId));

                // Find and remove the cart item
                CartItem cartItemToRemove = cart.getCartItems().stream()
                                .filter(item -> item.getId().equals(cartItemId))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart item not found with id: " + cartItemId));

                cart.removeCartItem(cartItemToRemove);

                // Save cart (orphanRemoval will delete the cart item)
                Cart savedCart = cartRepository.save(cart);

                log.info("Successfully removed item from cart - Customer: {}, CartItem: {}, New Total: {}",
                                customerId, cartItemId, savedCart.getTotalAmount());

                return cartMapper.toCartResponse(savedCart);
        }

        /**
         * Clears all items from the cart.
         * 
         * @param customerId the customer ID
         * @return updated cart response
         */
        @Transactional
        public CartResponse clearCart(UUID customerId) {
                log.debug("Clearing cart for customer: {}", customerId);

                // Get cart
                Cart cart = cartRepository.findByCustomerId(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart not found for customer: " + customerId));

                // Clear all items
                cart.clearItems();

                // Save cart
                Cart savedCart = cartRepository.save(cart);

                log.info("Successfully cleared cart for customer: {}", customerId);

                return cartMapper.toCartResponse(savedCart);
        }

        // ================================================================
        // Cart Validation Operations
        // ================================================================

        /**
         * Validates cart for checkout.
         * 
         * Checks:
         * - Cart is not empty
         * - All items are still available
         * - All quantities are valid
         * - Prices haven't changed significantly (optional warning)
         * 
         * @param customerId the customer ID
         * @return validation result with cart details
         */
        public CartResponse validateCartForCheckout(UUID customerId) {
                log.debug("Validating cart for checkout - Customer: {}", customerId);

                Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart not found for customer: " + customerId));

                if (cart.isEmpty()) {
                        throw new EmptyCartException("Cannot checkout with empty cart");
                }

                // Validate each cart item
                for (CartItem cartItem : cart.getCartItems()) {
                        MenuItem menuItem = cartItem.getMenuItem();

                        if (!menuItem.getAvailable()) {
                                throw new MenuItemUnavailableException(menuItem.getName(), "checkout");
                        }

                        if (cartItem.getQuantity() <= 0) {
                                throw new InvalidQuantityException(menuItem.getName(), cartItem.getQuantity());
                        }
                }

                log.info("Cart validation successful for customer: {}", customerId);
                return cartMapper.toCartResponse(cart);
        }

        /**
         * Recalculates cart totals.
         * 
         * Useful for maintenance or after price updates.
         * 
         * @param customerId the customer ID
         * @return updated cart response
         */
        @Transactional
        public CartResponse recalculateCartTotals(UUID customerId) {
                log.debug("Recalculating cart totals for customer: {}", customerId);

                Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cart not found for customer: " + customerId));

                // Recalculate all cart item subtotals
                cart.getCartItems().forEach(CartItem::calculateSubtotal);

                // Recalculate cart total
                cart.updateTotal();

                // Save cart
                Cart savedCart = cartRepository.save(cart);

                log.info("Successfully recalculated cart totals - Customer: {}, New Total: {}",
                                customerId, savedCart.getTotalAmount());

                return cartMapper.toCartResponse(savedCart);
        }

        // ================================================================
        // Helper Methods
        // ================================================================

        /**
         * Creates a new cart for a customer.
         * 
         * @param customer the customer
         * @return new cart
         */
        @Transactional
        protected Cart createNewCartForCustomer(Customer customer) {
                log.debug("Creating new cart for customer: {}", customer.getId());

                Cart cart = new Cart();
                cart.setCustomer(customer);
                cart.setTotalAmount(BigDecimal.ZERO);

                // Update customer relationship
                customer.setCart(cart);

                Cart savedCart = cartRepository.save(cart);
                log.info("Created new cart for customer: {}", customer.getId());

                return savedCart;
        }

        /**
         * Checks if customer owns the cart.
         * 
         * @param customerId the customer ID
         * @param cartId     the cart ID
         * @return true if customer owns the cart
         */
        public boolean isCartOwnedByCustomer(UUID customerId, UUID cartId) {
                // Use optimized query to load cart with customer relationship
                return cartRepository.findByIdWithItems(cartId)
                                .map(cart -> cart.getCustomer() != null
                                                && cart.getCustomer().getId().equals(customerId))
                                .orElse(false);
        }

        /**
         * Gets cart item count for customer.
         * 
         * @param customerId the customer ID
         * @return total item count
         */
        public int getCartItemCount(UUID customerId) {
                return cartRepository.findByCustomerId(customerId)
                                .map(Cart::getTotalItemCount)
                                .orElse(0);
        }

        /**
         * Gets cart total for customer.
         * 
         * @param customerId the customer ID
         * @return cart total amount
         */
        public BigDecimal getCartTotal(UUID customerId) {
                return cartRepository.findByCustomerId(customerId)
                                .map(cart -> cart.getTotalAmount() != null ? cart.getTotalAmount() : BigDecimal.ZERO)
                                .orElse(BigDecimal.ZERO);
        }
}
package com.raagaandroast.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.raagaandroast.cart.dto.AddCartItemRequest;
import com.raagaandroast.cart.dto.CartResponse;
import com.raagaandroast.cart.dto.UpdateCartItemRequest;
import com.raagaandroast.cart.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

/**
 * REST controller for cart operations.
 * 
 * This controller demonstrates advanced Spring MVC patterns:
 * - RESTful API design with proper HTTP methods and status codes
 * - Method-level security with @PreAuthorize
 * - Resource ownership authorization
 * - Comprehensive validation
 * - Proper error handling
 * - Clean separation of concerns
 * 
 * Design Decisions:
 * - Customer-centric URLs (/api/customers/{customerId}/cart)
 * - Proper HTTP methods (GET, POST, PATCH, DELETE)
 * - Resource ownership validation
 * - Comprehensive authorization
 * - Detailed API documentation
 * 
 * Interview Points:
 * - Why customer-centric URLs? Clear resource ownership
 * - Why @PreAuthorize? Method-level security control
 * - Why PATCH for updates? Partial resource modification
 * - Why separate endpoints? Single Responsibility Principle
 * - Why validation? Input sanitization and business rules
 * 
 * Security Model:
 * - Customers can only access their own cart
 * - Staff can view any cart for customer service
 * - Managers can perform cart operations for customers
 * - Admins have full access
 * 
 * @author RaagaAndRoast Development Team
 */
@RestController
@RequestMapping("/api/customers/{customerId}/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping Cart", description = "Shopping cart management operations")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

        private final CartService cartService;

        // ================================================================
        // Cart Retrieval Operations
        // ================================================================

        /**
         * Gets the customer's cart with all items.
         * 
         * Security: Customer can access own cart, staff+ can access any cart
         * 
         * @param customerId     the customer ID
         * @param authentication the current authentication
         * @return cart response with all items
         */
        @GetMapping
        @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
        @Operation(summary = "Get customer cart", description = "Retrieves the customer's cart with all items and calculated totals. Customers can access their own cart, staff can access any cart.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Access denied - not cart owner or staff"),
                        @ApiResponse(responseCode = "404", description = "Customer or cart not found")
        })
        public ResponseEntity<CartResponse> getCart(
                        @Parameter(description = "Customer ID") @PathVariable UUID customerId,
                        Authentication authentication) {

                log.debug("Getting cart for customer: {} by user: {}", customerId, authentication.getName());

                CartResponse cart = cartService.getCartByCustomerId(customerId);

                log.info("Successfully retrieved cart for customer: {} - Items: {}, Total: {}",
                                customerId, cart.getUniqueItemCount(), cart.getTotalAmount());

                return ResponseEntity.ok(cart);
        }

        /**
         * Gets the customer's cart summary (without items).
         * 
         * Useful for navigation/header display where full cart details aren't needed.
         * 
         * @param customerId     the customer ID
         * @param authentication the current authentication
         * @return cart summary response
         */
        @GetMapping("/summary")
        @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
        @Operation(summary = "Get cart summary", description = "Retrieves cart summary without detailed items. Useful for navigation/header display.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cart summary retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Access denied - not cart owner or staff"),
                        @ApiResponse(responseCode = "404", description = "Customer not found")
        })
        public ResponseEntity<CartResponse> getCartSummary(
                        @Parameter(description = "Customer ID") @PathVariable UUID customerId,
                        Authentication authentication) {

                log.debug("Getting cart summary for customer: {} by user: {}", customerId, authentication.getName());

                CartResponse cartSummary = cartService.getCartSummary(customerId);

                return ResponseEntity.ok(cartSummary);
        }

        // ================================================================
        // Cart Item Management Operations
        // ================================================================

        /**
         * Adds an item to the customer's cart.
         * 
         * Business Logic:
         * - Validates menu item exists and is available
         * - Captures current price as snapshot
         * - If item exists, increases quantity
         * - If new item, creates new cart item
         * - Recalculates cart total
         * 
         * Security: Customer can modify own cart, staff+ can modify any cart
         * 
         * @param customerId     the customer ID
         * @param request        the add cart item request
         * @param authentication the current authentication
         * @return updated cart response
         */
        @PostMapping("/items")
        @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
        @Operation(summary = "Add item to cart", description = "Adds a menu item to the customer's cart with specified quantity. Creates cart if it doesn't exist. If item already exists, increases quantity.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or menu item not available"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Access denied - not cart owner or staff"),
                        @ApiResponse(responseCode = "404", description = "Customer or menu item not found")
        })
        public ResponseEntity<CartResponse> addItemToCart(
                        @Parameter(description = "Customer ID") @PathVariable UUID customerId,
                        @Valid @RequestBody AddCartItemRequest request,
                        Authentication authentication) {

                log.debug("Adding item to cart - Customer: {}, MenuItem: {}, Quantity: {} by user: {}",
                                customerId, request.getMenuItemId(), request.getQuantity(), authentication.getName());

                CartResponse updatedCart = cartService.addItemToCart(customerId, request);

                log.info("Successfully added item to cart - Customer: {}, MenuItem: {}, New Total: {}",
                                customerId, request.getMenuItemId(), updatedCart.getTotalAmount());

                return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
        }

        /**
         * Updates the quantity of a cart item.
         * 
         * Uses PATCH method for partial resource update.
         * 
         * @param customerId     the customer ID
         * @param cartItemId     the cart item ID
         * @param request        the update request
         * @param authentication the current authentication
         * @return updated cart response
         */
        @PatchMapping("/items/{cartItemId}")
        @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
        @Operation(summary = "Update cart item quantity", description = "Updates the quantity of a specific cart item. Uses PATCH method for partial resource update.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cart item quantity updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid quantity or request data"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Access denied - not cart owner or staff"),
                        @ApiResponse(responseCode = "404", description = "Customer, cart, or cart item not found")
        })
        public ResponseEntity<CartResponse> updateCartItemQuantity(
                        @Parameter(description = "Customer ID") @PathVariable UUID customerId,
                        @Parameter(description = "Cart item ID") @PathVariable UUID cartItemId,
                        @Valid @RequestBody UpdateCartItemRequest request,
                        Authentication authentication) {

                log.debug("Updating cart item quantity - Customer: {}, CartItem: {}, New Quantity: {} by user: {}",
                                customerId, cartItemId, request.getQuantity(), authentication.getName());

                CartResponse updatedCart = cartService.updateCartItemQuantity(customerId, cartItemId, request);

                log.info("Successfully updated cart item quantity - Customer: {}, CartItem: {}, New Total: {}",
                                customerId, cartItemId, updatedCart.getTotalAmount());

                return ResponseEntity.ok(updatedCart);
        }

        /**
         * Removes an item from the cart.
         * 
         * @param customerId     the customer ID
         * @param cartItemId     the cart item ID
         * @param authentication the current authentication
         * @return updated cart response
         */
        @DeleteMapping("/items/{cartItemId}")
        @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
        @Operation(summary = "Remove item from cart", description = "Removes a specific item from the customer's cart completely.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Item removed from cart successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Access denied - not cart owner or staff"),
                        @ApiResponse(responseCode = "404", description = "Customer, cart, or cart item not found")
        })
        public ResponseEntity<CartResponse> removeItemFromCart(
                        @Parameter(description = "Customer ID") @PathVariable UUID customerId,
                        @Parameter(description = "Cart item ID") @PathVariable UUID cartItemId,
                        Authentication authentication) {

                log.debug("Removing item from cart - Customer: {}, CartItem: {} by user: {}",
                                customerId, cartItemId, authentication.getName());

                CartResponse updatedCart = cartService.removeItemFromCart(customerId, cartItemId);

                log.info("Successfully removed item from cart - Customer: {}, CartItem: {}, New Total: {}",
                                customerId, cartItemId, updatedCart.getTotalAmount());

                return ResponseEntity.ok(updatedCart);
        }

        /**
         * Clears all items from the cart.
         * 
         * @param customerId     the customer ID
         * @param authentication the current authentication
         * @return updated cart response
         */
        @DeleteMapping("/items")
        @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
        @Operation(summary = "Clear cart", description = "Removes all items from the customer's cart, leaving an empty cart.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Access denied - not cart owner or staff"),
                        @ApiResponse(responseCode = "404", description = "Customer or cart not found")
        })
        public ResponseEntity<CartResponse> clearCart(
                        @Parameter(description = "Customer ID") @PathVariable UUID customerId,
                        Authentication authentication) {

                log.debug("Clearing cart for customer: {} by user: {}", customerId, authentication.getName());

                CartResponse clearedCart = cartService.clearCart(customerId);

                log.info("Successfully cleared cart for customer: {}", customerId);

                return ResponseEntity.ok(clearedCart);
        }

        // ================================================================
        // Cart Validation and Utility Operations
        // ================================================================

        /**
         * Validates cart for checkout.
         * 
         * Checks:
         * - Cart is not empty
         * - All items are still available
         * - All quantities are valid
         * - Returns warnings for price changes
         * 
         * @param customerId     the customer ID
         * @param authentication the current authentication
         * @return validation result with cart details
         */
        @GetMapping("/validate")
        @PreAuthorize("@resourceOwnership.isCustomerOwnerOrStaff(authentication, #customerId)")
        @Operation(summary = "Validate cart for checkout", description = "Validates cart for checkout by checking if cart is not empty, all items are available, quantities are valid, and returns warnings for price changes.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cart validation completed - check warnings in response"),
                        @ApiResponse(responseCode = "400", description = "Cart validation failed - cart empty or invalid items"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Access denied - not cart owner or staff"),
                        @ApiResponse(responseCode = "404", description = "Customer or cart not found")
        })
        public ResponseEntity<CartResponse> validateCartForCheckout(
                        @Parameter(description = "Customer ID") @PathVariable UUID customerId,
                        Authentication authentication) {

                log.debug("Validating cart for checkout - Customer: {} by user: {}", customerId,
                                authentication.getName());

                CartResponse validatedCart = cartService.validateCartForCheckout(customerId);

                log.info("Cart validation completed for customer: {} - Valid: {}",
                                customerId,
                                validatedCart.getWarnings() == null || validatedCart.getWarnings().isEmpty());

                return ResponseEntity.ok(validatedCart);
        }

        /**
         * Recalculates cart totals.
         * 
         * Useful for maintenance or after price updates.
         * Restricted to staff+ for data integrity.
         * 
         * @param customerId     the customer ID
         * @param authentication the current authentication
         * @return updated cart response
         */
        @PostMapping("/recalculate")
        @PreAuthorize("@resourceOwnership.isStaffOrHigher(authentication)")
        @Operation(summary = "Recalculate cart totals", description = "Recalculates cart totals. Useful for maintenance or after price updates. Restricted to staff+ for data integrity.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cart totals recalculated successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Access denied - staff role or higher required"),
                        @ApiResponse(responseCode = "404", description = "Customer or cart not found")
        })
        public ResponseEntity<CartResponse> recalculateCartTotals(
                        @Parameter(description = "Customer ID") @PathVariable UUID customerId,
                        Authentication authentication) {

                log.debug("Recalculating cart totals for customer: {} by user: {}", customerId,
                                authentication.getName());

                CartResponse recalculatedCart = cartService.recalculateCartTotals(customerId);

                log.info("Successfully recalculated cart totals for customer: {} - New Total: {}",
                                customerId, recalculatedCart.getTotalAmount());

                return ResponseEntity.ok(recalculatedCart);
        }

        // ================================================================
        // Note: Convenience cart endpoints moved to CurrentUserCartController
        // ================================================================

        /*
         * The convenience endpoints for current user cart access have been moved
         * to CurrentUserCartController to avoid path conflicts and follow the
         * Single Responsibility Principle.
         *
         * Available at:
         * - GET /api/cart
         * - POST /api/cart/items
         * - PATCH /api/cart/items/{cartItemId}
         * - DELETE /api/cart/items/{cartItemId}
         * - DELETE /api/cart/items
         * - GET /api/cart/validate
         * - GET /api/cart/summary
         */
}
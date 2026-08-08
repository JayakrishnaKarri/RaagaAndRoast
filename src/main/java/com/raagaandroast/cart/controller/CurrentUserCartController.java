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
import com.raagaandroast.customer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

/**
 * REST controller for current user's cart operations.
 * 
 * This controller provides convenience endpoints for the authenticated customer
 * to access their own cart without needing to specify customer ID in the path.
 * 
 * Design Decisions:
 * - Separate controller to avoid path conflicts
 * - Automatic customer ID resolution from authentication
 * - Simplified URLs for frontend convenience
 * - Customer-only access (no staff override)
 * 
 * Interview Points:
 * - Why separate controller? Avoids path conflicts and follows SRP
 * - Why convenience endpoints? Better UX for authenticated customers
 * - Why customer-only? These are self-service endpoints
 * - How is customer ID resolved? From Spring Security Authentication
 * 
 * @author RaagaAndRoast Development Team
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Current User Cart", description = "Convenience cart operations for authenticated customer")
@SecurityRequirement(name = "bearerAuth")
public class CurrentUserCartController {

    private final CartService cartService;
    private final CustomerService customerService;

    /**
     * Gets the current user's cart.
     * 
     * Convenience endpoint that doesn't require customer ID in path.
     * Automatically uses the authenticated customer's cart.
     * 
     * @param authentication the current authentication
     * @return cart response
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current user's cart", description = "Retrieves the authenticated customer's cart with all items and calculated totals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer or cart not found")
    })
    public ResponseEntity<CartResponse> getCurrentUserCart(Authentication authentication) {

        log.debug("Getting current user's cart for user: {}", authentication.getName());

        // Get customer ID from authentication
        UUID customerId = customerService.getCustomerIdByUsername(authentication.getName());

        CartResponse cart = cartService.getCartByCustomerId(customerId);

        log.info("Successfully retrieved current user's cart - Items: {}, Total: {}",
                cart.getUniqueItemCount(), cart.getTotalAmount());

        return ResponseEntity.ok(cart);
    }

    /**
     * Gets the current user's cart summary.
     * 
     * @param authentication the current authentication
     * @return cart summary response
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current user's cart summary", description = "Retrieves cart summary without detailed items for the authenticated customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CartResponse> getCurrentUserCartSummary(Authentication authentication) {

        log.debug("Getting current user's cart summary for user: {}", authentication.getName());

        UUID customerId = customerService.getCustomerIdByUsername(authentication.getName());
        CartResponse cartSummary = cartService.getCartSummary(customerId);

        return ResponseEntity.ok(cartSummary);
    }

    /**
     * Adds item to current user's cart.
     * 
     * Convenience endpoint for the authenticated customer.
     * 
     * @param request        the add cart item request
     * @param authentication the current authentication
     * @return updated cart response
     */
    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Add item to current user's cart", description = "Adds a menu item to the authenticated customer's cart with specified quantity.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or menu item not available"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer or menu item not found")
    })
    public ResponseEntity<CartResponse> addItemToCurrentUserCart(
            @Valid @RequestBody AddCartItemRequest request,
            Authentication authentication) {

        log.debug("Adding item to current user's cart - MenuItem: {}, Quantity: {} by user: {}",
                request.getMenuItemId(), request.getQuantity(), authentication.getName());

        // Get customer ID from authentication
        UUID customerId = customerService.getCustomerIdByUsername(authentication.getName());

        CartResponse updatedCart = cartService.addItemToCart(customerId, request);

        log.info("Successfully added item to current user's cart - MenuItem: {}, New Total: {}",
                request.getMenuItemId(), updatedCart.getTotalAmount());

        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    /**
     * Updates the quantity of a cart item for current user.
     * 
     * @param cartItemId     the cart item ID
     * @param request        the update request
     * @param authentication the current authentication
     * @return updated cart response
     */
    @PatchMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update current user's cart item quantity", description = "Updates the quantity of a specific cart item for the authenticated customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart item quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer, cart, or cart item not found")
    })
    public ResponseEntity<CartResponse> updateCurrentUserCartItemQuantity(
            @Parameter(description = "Cart item ID") @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {

        log.debug("Updating current user's cart item quantity - CartItem: {}, New Quantity: {} by user: {}",
                cartItemId, request.getQuantity(), authentication.getName());

        UUID customerId = customerService.getCustomerIdByUsername(authentication.getName());
        CartResponse updatedCart = cartService.updateCartItemQuantity(customerId, cartItemId, request);

        log.info("Successfully updated current user's cart item quantity - CartItem: {}, New Total: {}",
                cartItemId, updatedCart.getTotalAmount());

        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Removes an item from current user's cart.
     * 
     * @param cartItemId     the cart item ID
     * @param authentication the current authentication
     * @return updated cart response
     */
    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Remove item from current user's cart", description = "Removes a specific item from the authenticated customer's cart completely.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed from cart successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer, cart, or cart item not found")
    })
    public ResponseEntity<CartResponse> removeItemFromCurrentUserCart(
            @Parameter(description = "Cart item ID") @PathVariable UUID cartItemId,
            Authentication authentication) {

        log.debug("Removing item from current user's cart - CartItem: {} by user: {}",
                cartItemId, authentication.getName());

        UUID customerId = customerService.getCustomerIdByUsername(authentication.getName());
        CartResponse updatedCart = cartService.removeItemFromCart(customerId, cartItemId);

        log.info("Successfully removed item from current user's cart - CartItem: {}, New Total: {}",
                cartItemId, updatedCart.getTotalAmount());

        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Clears all items from current user's cart.
     * 
     * @param authentication the current authentication
     * @return updated cart response
     */
    @DeleteMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Clear current user's cart", description = "Removes all items from the authenticated customer's cart, leaving an empty cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer or cart not found")
    })
    public ResponseEntity<CartResponse> clearCurrentUserCart(Authentication authentication) {

        log.debug("Clearing current user's cart for user: {}", authentication.getName());

        UUID customerId = customerService.getCustomerIdByUsername(authentication.getName());
        CartResponse clearedCart = cartService.clearCart(customerId);

        log.info("Successfully cleared current user's cart for user: {}", authentication.getName());

        return ResponseEntity.ok(clearedCart);
    }

    /**
     * Validates current user's cart for checkout.
     * 
     * @param authentication the current authentication
     * @return validation result with cart details
     */
    @GetMapping("/validate")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Validate current user's cart for checkout", description = "Validates the authenticated customer's cart for checkout.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart validation completed - check warnings in response"),
            @ApiResponse(responseCode = "400", description = "Cart validation failed - cart empty or invalid items"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied - customer role required"),
            @ApiResponse(responseCode = "404", description = "Customer or cart not found")
    })
    public ResponseEntity<CartResponse> validateCurrentUserCartForCheckout(Authentication authentication) {

        log.debug("Validating current user's cart for checkout - User: {}", authentication.getName());

        UUID customerId = customerService.getCustomerIdByUsername(authentication.getName());
        CartResponse validatedCart = cartService.validateCartForCheckout(customerId);

        log.info("Cart validation completed for current user: {} - Valid: {}",
                authentication.getName(),
                validatedCart.getWarnings() == null || validatedCart.getWarnings().isEmpty());

        return ResponseEntity.ok(validatedCart);
    }
}
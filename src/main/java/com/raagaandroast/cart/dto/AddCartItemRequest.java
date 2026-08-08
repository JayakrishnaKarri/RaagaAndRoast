package com.raagaandroast.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for adding an item to the cart.
 * 
 * This DTO demonstrates proper validation and encapsulation:
 * - Input validation with Jakarta Bean Validation
 * - Immutable design with builder pattern
 * - Clear separation from entity layer
 * - Business rule validation
 * 
 * Design Decisions:
 * - Only essential fields for adding items
 * - Validation annotations for business rules
 * - UUID for menu item reference
 * - Positive quantity validation
 * 
 * Interview Points:
 * - Why separate DTO? API contract independence from entities
 * - Why validation here? Input validation at API boundary
 * - Why not expose price? Server determines price for security
 * - Why builder pattern? Immutable objects and flexible construction
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemRequest {

    /**
     * ID of the menu item to add to cart.
     * Must be a valid menu item that exists and is available.
     */
    @NotNull(message = "Menu item ID is required")
    private UUID menuItemId;

    /**
     * Quantity of the menu item to add.
     * Must be positive (greater than 0).
     */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    /**
     * Optional special instructions or notes for this item.
     * Could be used for customizations, allergies, etc.
     */
    private String notes;
}
package com.raagaandroast.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating cart item quantity.
 * 
 * This DTO is used when a customer wants to change the quantity
 * of an existing item in their cart.
 * 
 * Design Decisions:
 * - Simple DTO with only necessary fields
 * - Validation for business rules
 * - Immutable design
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {

    /**
     * New quantity for the cart item.
     * Must be positive (greater than 0).
     */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    /**
     * Optional updated notes for this item.
     */
    private String notes;
}
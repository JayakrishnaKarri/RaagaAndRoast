package com.raagaandroast.order.dto;

import com.raagaandroast.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating order status.
 * 
 * This DTO demonstrates:
 * - Status transition validation at API level
 * - Optional fields for status-specific information
 * - Business rule enforcement through validation
 * - Audit trail support with reason tracking
 * 
 * Design Decisions:
 * - Separate DTO for status updates (single responsibility)
 * - Validation for required fields based on status
 * - Optional reason field for audit trail
 * - Optional prep time for kitchen operations
 * 
 * Interview Points:
 * - Why separate DTO? Status updates have different validation rules
 * - Why validation here? Ensure valid status transitions at API boundary
 * - Why optional fields? Different statuses require different information
 * - Why reason field? Audit trail and business intelligence
 * 
 * Business Rules:
 * - Status transitions must be valid per business workflow
 * - Cancellation requires reason
 * - Preparation completion can include actual prep time
 * - Status updates are audited for compliance
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    /**
     * New status for the order.
     * 
     * Must be a valid status transition from current status.
     * Validation occurs at service layer.
     */
    @NotNull(message = "Status is required")
    private OrderStatus status;

    /**
     * Reason for status change (required for cancellation).
     * 
     * Provides audit trail and business intelligence.
     */
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    /**
     * Actual preparation time in minutes (for completion).
     * 
     * Used when marking order as ready or completed.
     * Helps track kitchen performance.
     */
    @Positive(message = "Preparation time must be positive")
    private Integer actualPrepTime;

    /**
     * Additional notes for the status change.
     * 
     * Optional field for additional context.
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    // ================================================================
    // Validation Helper Methods
    // ================================================================

    /**
     * Checks if this is a cancellation request.
     * 
     * @return true if status is CANCELLED
     */
    public boolean isCancellation() {
        return OrderStatus.CANCELLED.equals(status);
    }

    /**
     * Checks if this is a completion request.
     * 
     * @return true if status is COMPLETED or READY
     */
    public boolean isCompletion() {
        return OrderStatus.COMPLETED.equals(status) || OrderStatus.READY.equals(status);
    }

    /**
     * Checks if reason is required for this status.
     * 
     * @return true if reason should be provided
     */
    public boolean isReasonRequired() {
        return isCancellation();
    }

    /**
     * Checks if prep time can be provided for this status.
     * 
     * @return true if prep time is relevant
     */
    public boolean canHavePrepTime() {
        return OrderStatus.READY.equals(status) || OrderStatus.COMPLETED.equals(status);
    }

    /**
     * Validates the request based on status-specific rules.
     * 
     * @return validation error message or null if valid
     */
    public String validateRequest() {
        if (isCancellation() && (reason == null || reason.trim().isEmpty())) {
            return "Cancellation reason is required";
        }

        if (actualPrepTime != null && !canHavePrepTime()) {
            return "Preparation time not applicable for this status";
        }

        return null; // Valid
    }
}
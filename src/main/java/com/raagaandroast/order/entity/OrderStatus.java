package com.raagaandroast.order.entity;

/**
 * Enumeration representing the various states of an order in the system.
 * 
 * This enum demonstrates proper order workflow management:
 * - Clear state definitions for order lifecycle
 * - Logical progression from creation to completion
 * - Support for cancellation at appropriate stages
 * - Business rule enforcement through code
 * 
 * Design Decisions:
 * - Simple enum without complex state machine (KISS principle)
 * - Clear naming for business understanding
 * - Support for common café order workflows
 * - Extensible for future status additions
 * 
 * Interview Points:
 * - Why enum vs String? Type safety, compile-time validation
 * - Why these specific statuses? Common café order workflow
 * - How to handle status transitions? Business logic in service layer
 * - What about invalid transitions? Validation in service methods
 * 
 * Order Workflow:
 * PENDING → CONFIRMED → PREPARING → READY → COMPLETED
 * ↓ ↓ ↓
 * CANCELLED CANCELLED CANCELLED
 * 
 * Business Rules:
 * - Orders start as PENDING
 * - PENDING orders can be CONFIRMED or CANCELLED
 * - CONFIRMED orders can be moved to PREPARING or CANCELLED
 * - PREPARING orders can be moved to READY or CANCELLED
 * - READY orders can be COMPLETED
 * - COMPLETED and CANCELLED are terminal states
 * 
 * @author RaagaAndRoast Development Team
 */
public enum OrderStatus {

    /**
     * Order has been created but not yet confirmed.
     * 
     * This is the initial state when an order is first placed.
     * Payment may be pending, inventory may need verification.
     * 
     * Valid transitions: CONFIRMED, CANCELLED
     */
    PENDING("Pending", "Order has been placed and is awaiting confirmation"),

    /**
     * Order has been confirmed and accepted for preparation.
     * 
     * Payment has been processed, inventory is reserved,
     * and the order is ready to be prepared.
     * 
     * Valid transitions: PREPARING, CANCELLED
     */
    CONFIRMED("Confirmed", "Order has been confirmed and accepted"),

    /**
     * Order is currently being prepared.
     * 
     * Kitchen staff are actively working on the order.
     * Items are being cooked, assembled, or packaged.
     * 
     * Valid transitions: READY, CANCELLED
     */
    PREPARING("Preparing", "Order is being prepared"),

    /**
     * Order is ready for pickup or delivery.
     * 
     * All items have been prepared and the order is
     * waiting for customer pickup or delivery dispatch.
     * 
     * Valid transitions: COMPLETED
     */
    READY("Ready", "Order is ready for pickup or delivery"),

    /**
     * Order has been completed successfully.
     * 
     * Customer has received their order. This is a terminal state.
     * No further transitions are allowed.
     * 
     * Valid transitions: None (terminal state)
     */
    COMPLETED("Completed", "Order has been completed and delivered"),

    /**
     * Order has been cancelled.
     * 
     * Order was cancelled either by customer request,
     * system issues, or business reasons. This is a terminal state.
     * 
     * Valid transitions: None (terminal state)
     */
    CANCELLED("Cancelled", "Order has been cancelled");

    private final String displayName;
    private final String description;

    /**
     * Constructor for OrderStatus enum.
     * 
     * @param displayName user-friendly name for display
     * @param description detailed description of the status
     */
    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the user-friendly display name.
     * 
     * @return display name for UI presentation
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the detailed description of this status.
     * 
     * @return description explaining the status
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this status can transition to the target status.
     * 
     * This method encodes the business rules for valid status transitions.
     * 
     * @param targetStatus the status to transition to
     * @return true if transition is valid
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        return switch (this) {
            case PENDING -> targetStatus == CONFIRMED || targetStatus == CANCELLED;
            case CONFIRMED -> targetStatus == PREPARING || targetStatus == CANCELLED;
            case PREPARING -> targetStatus == READY || targetStatus == CANCELLED;
            case READY -> targetStatus == COMPLETED;
            case COMPLETED, CANCELLED -> false; // Terminal states
        };
    }

    /**
     * Gets all valid next statuses from the current status.
     * 
     * @return array of valid next statuses
     */
    public OrderStatus[] getValidNextStatuses() {
        return switch (this) {
            case PENDING -> new OrderStatus[] { CONFIRMED, CANCELLED };
            case CONFIRMED -> new OrderStatus[] { PREPARING, CANCELLED };
            case PREPARING -> new OrderStatus[] { READY, CANCELLED };
            case READY -> new OrderStatus[] { COMPLETED };
            case COMPLETED, CANCELLED -> new OrderStatus[] {}; // No valid transitions
        };
    }

    /**
     * Checks if this is a terminal status (no further transitions allowed).
     * 
     * @return true if this is a terminal status
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * Checks if this status allows cancellation.
     * 
     * @return true if order can be cancelled from this status
     */
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED || this == PREPARING;
    }

    /**
     * Checks if this status indicates the order is active (not terminal).
     * 
     * @return true if order is still active
     */
    public boolean isActive() {
        return !isTerminal();
    }

    /**
     * Gets the next logical status in the normal workflow.
     * 
     * This represents the "happy path" progression.
     * 
     * @return next status in normal workflow, or null if terminal
     */
    public OrderStatus getNextNormalStatus() {
        return switch (this) {
            case PENDING -> CONFIRMED;
            case CONFIRMED -> PREPARING;
            case PREPARING -> READY;
            case READY -> COMPLETED;
            case COMPLETED, CANCELLED -> null; // Terminal states
        };
    }

    /**
     * Gets the CSS class name for UI styling.
     * 
     * @return CSS class name for status styling
     */
    public String getCssClass() {
        return switch (this) {
            case PENDING -> "status-pending";
            case CONFIRMED -> "status-confirmed";
            case PREPARING -> "status-preparing";
            case READY -> "status-ready";
            case COMPLETED -> "status-completed";
            case CANCELLED -> "status-cancelled";
        };
    }

    /**
     * Gets the color code for status visualization.
     * 
     * @return hex color code for status
     */
    public String getColorCode() {
        return switch (this) {
            case PENDING -> "#FFA500"; // Orange
            case CONFIRMED -> "#007BFF"; // Blue
            case PREPARING -> "#FFC107"; // Yellow
            case READY -> "#28A745"; // Green
            case COMPLETED -> "#6C757D"; // Gray
            case CANCELLED -> "#DC3545"; // Red
        };
    }
}
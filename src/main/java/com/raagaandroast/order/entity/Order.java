package com.raagaandroast.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.raagaandroast.customer.entity.Customer;
import com.raagaandroast.customer.entity.Address;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.time.Duration;

/**
 * Order entity representing customer orders in the system.
 * 
 * This entity demonstrates advanced JPA patterns and business logic:
 * - Complex entity relationships with proper cascade strategies
 * - Order status workflow management
 * - BigDecimal for precise monetary calculations
 * - Optimistic locking for concurrent order updates
 * - JPA auditing for order lifecycle tracking
 * - Business methods for order management
 * - Price snapshot preservation from cart
 * 
 * Design Decisions:
 * - UUID primary key for security and distributed systems
 * - @ManyToOne with Customer (many orders per customer)
 * - @OneToMany with OrderItem using proper cascade
 * - @ManyToOne with Address for delivery information
 * - OrderStatus enum for workflow management
 * - BigDecimal with precision 10, scale 2 for monetary values
 * - Optimistic locking for concurrent access
 * - Comprehensive auditing for business requirements
 * 
 * Interview Points:
 * - Why separate Order from Cart? Different lifecycle and business rules
 * - Why capture delivery address? Orders need immutable delivery info
 * - Why OrderStatus enum? Type safety and workflow validation
 * - Why optimistic locking? Multiple staff may update order status
 * - Why price snapshots? Historical accuracy for accounting
 * - Why audit fields? Business compliance and customer service
 * 
 * Business Rules:
 * - Orders start in PENDING status
 * - Total amount calculated from order items
 * - Delivery address captured at order time
 * - Status transitions follow business workflow
 * - Order items preserve price at time of ordering
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at"),
        @Index(name = "idx_orders_updated_at", columnList = "updated_at"),
        @Index(name = "idx_orders_customer_status", columnList = "customer_id, status"),
        @Index(name = "idx_orders_customer_created", columnList = "customer_id, created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * @ManyToOne relationship with Customer.
     * 
     *            Many orders belong to one customer.
     *            FetchType.LAZY for performance - customer loaded only when
     *            accessed.
     * 
     *            nullable = false ensures every order belongs to a customer.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * @ManyToOne relationship with Address for delivery.
     * 
     *            Captures the delivery address at time of ordering.
     *            This is a snapshot - even if customer's address changes,
     *            the order retains the original delivery address.
     * 
     *            FetchType.EAGER because delivery address is frequently needed
     *            when displaying order details.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_address_id")
    private Address deliveryAddress;

    /**
     * @OneToMany relationship with OrderItem.
     * 
     *            CascadeType.ALL: OrderItem lifecycle tied to Order
     *            orphanRemoval = true: Remove order items when removed from order
     *            FetchType.LAZY: Order items loaded only when accessed
     * 
     *            Use JOIN FETCH in repository when order items are needed.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Current status of the order.
     * 
     * Uses OrderStatus enum for type safety and workflow validation.
     * 
     * @Enumerated(EnumType.STRING) stores enum name in database
     *                              for readability and database independence.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Total amount of the order.
     * 
     * BigDecimal with precision 10, scale 2 for monetary values.
     * Calculated from order items but persisted for performance
     * and historical accuracy.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Subtotal before taxes and fees.
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Tax amount for the order.
     */
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /**
     * Delivery fee if applicable.
     */
    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    /**
     * Special instructions or notes for the order.
     */
    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions;

    /**
     * Estimated preparation time in minutes.
     */
    @Column(name = "estimated_prep_time")
    private Integer estimatedPrepTime;

    /**
     * Actual preparation time in minutes (filled when order is ready).
     */
    @Column(name = "actual_prep_time")
    private Integer actualPrepTime;

    /**
     * When the order was confirmed.
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * When preparation started.
     */
    @Column(name = "preparation_started_at")
    private LocalDateTime preparationStartedAt;

    /**
     * When the order was ready.
     */
    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    /**
     * When the order was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * When the order was cancelled (if applicable).
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Reason for cancellation (if applicable).
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Optimistic locking version field.
     * 
     * Prevents lost updates when multiple staff members
     * update the same order simultaneously.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Audit fields for tracking order lifecycle.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ================================================================
    // Business Methods
    // ================================================================

    /**
     * Calculates the subtotal from all order items.
     * 
     * @return subtotal amount
     */
    public BigDecimal calculateSubtotal() {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total amount including tax and delivery fee.
     * 
     * @return total amount
     */
    public BigDecimal calculateTotal() {
        BigDecimal calculatedSubtotal = calculateSubtotal();
        return calculatedSubtotal
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .add(deliveryFee != null ? deliveryFee : BigDecimal.ZERO);
    }

    /**
     * Updates all calculated amounts.
     */
    public void updateAmounts() {
        this.subtotal = calculateSubtotal();
        this.totalAmount = calculateTotal();
    }

    /**
     * Adds an order item to this order.
     * 
     * @param orderItem the order item to add
     */
    public void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("OrderItem cannot be null");
        }

        orderItems.add(orderItem);
        orderItem.setOrder(this);
        updateAmounts();
    }

    /**
     * Removes an order item from this order.
     * 
     * @param orderItem the order item to remove
     */
    public void removeOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            return;
        }

        orderItems.remove(orderItem);
        orderItem.setOrder(null);
        updateAmounts();
    }

    /**
     * Gets the total number of items in the order.
     * 
     * @return total item count
     */
    public int getTotalItemCount() {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0;
        }

        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Gets the number of unique items in the order.
     * 
     * @return unique item count
     */
    public int getUniqueItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    /**
     * Checks if the order is empty.
     * 
     * @return true if order has no items
     */
    public boolean isEmpty() {
        return orderItems == null || orderItems.isEmpty();
    }

    // ================================================================
    // Status Management Methods
    // ================================================================

    /**
     * Attempts to transition the order to a new status.
     * 
     * @param newStatus the target status
     * @throws IllegalStateException if transition is not valid
     */
    public void transitionTo(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", status, newStatus));
        }

        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        updateStatusTimestamps(oldStatus, newStatus);
    }

    /**
     * Updates timestamp fields based on status transition.
     * 
     * @param oldStatus the previous status
     * @param newStatus the new status
     */

    private void updateStatusTimestamps(OrderStatus oldStatus, OrderStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {

            case PENDING -> {
                // Initial order state.
                // No timestamp update required.
            }

            case CONFIRMED -> {
                this.confirmedAt = now;

                if (estimatedPrepTime != null) {
                    // Estimated ready time can be calculated here
                    // if required by the business logic.
                }
            }

            case PREPARING -> {
                this.preparationStartedAt = now;
            }

            case READY -> {
                this.readyAt = now;

                if (preparationStartedAt != null) {
                    this.actualPrepTime = (int) Duration
                            .between(preparationStartedAt, now)
                            .toMinutes();
                }
            }

            case COMPLETED -> {
                this.completedAt = now;
            }

            case CANCELLED -> {
                this.cancelledAt = now;
            }
        }
    }

    /**
     * Confirms the order (transitions from PENDING to CONFIRMED).
     */
    public void confirm() {
        transitionTo(OrderStatus.CONFIRMED);
    }

    /**
     * Starts preparation (transitions to PREPARING).
     */
    public void startPreparation() {
        transitionTo(OrderStatus.PREPARING);
    }

    /**
     * Marks order as ready (transitions to READY).
     */
    public void markReady() {
        transitionTo(OrderStatus.READY);
    }

    /**
     * Completes the order (transitions to COMPLETED).
     */
    public void complete() {
        transitionTo(OrderStatus.COMPLETED);
    }

    /**
     * Cancels the order with a reason.
     * 
     * @param reason the cancellation reason
     */
    public void cancel(String reason) {
        this.cancellationReason = reason;
        transitionTo(OrderStatus.CANCELLED);
    }

    /**
     * Checks if the order can be cancelled.
     * 
     * @return true if order can be cancelled
     */
    public boolean isCancellable() {
        return status.isCancellable();
    }

    /**
     * Checks if the order is in a terminal state.
     * 
     * @return true if order is completed or cancelled
     */
    public boolean isTerminal() {
        return status.isTerminal();
    }

    /**
     * Checks if the order is active (not terminal).
     * 
     * @return true if order is still active
     */
    public boolean isActive() {
        return status.isActive();
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    /**
     * Gets the customer's full name for display.
     * 
     * @return customer full name or "Unknown" if customer is null
     */
    public String getCustomerName() {
        return customer != null ? customer.getFullName() : "Unknown";
    }

    /**
     * Gets the delivery address as a formatted string.
     * 
     * @return formatted delivery address or "Pickup" if no address
     */
    public String getDeliveryAddressString() {
        return deliveryAddress != null ? deliveryAddress.getFullAddress() : "Pickup";
    }

    /**
     * Calculates the elapsed time since order creation.
     * 
     * @return elapsed time in minutes
     */
    public long getElapsedTimeMinutes() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }

    // ================================================================
    // JPA Lifecycle Callbacks
    // ================================================================

    /**
     * JPA callback method called before persisting.
     */
    @PrePersist
    protected void onCreate() {
        updateAmounts();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }

    /**
     * JPA callback method called before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updateAmounts();
    }

    // ================================================================
    // toString, equals, hashCode
    // ================================================================

    @Override
    public String toString() {
        return String.format(
                "Order{id=%s, status=%s, totalAmount=%s, itemCount=%d, customer=%s, createdAt=%s}",
                id, status, totalAmount, getUniqueItemCount(), getCustomerName(), createdAt);
    }

    /**
     * Equals based on ID for entity identity.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Order order = (Order) obj;
        return id != null && id.equals(order.id);
    }

    /**
     * HashCode based on ID for entity identity.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
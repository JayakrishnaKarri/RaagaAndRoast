package com.raagaandroast.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.raagaandroast.menu.entity.MenuItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OrderItem entity representing individual items within an order.
 * 
 * This entity demonstrates critical business patterns:
 * - Price snapshot preservation for historical accuracy
 * - @ManyToOne relationships with Order and MenuItem
 * - BigDecimal for precise monetary calculations
 * - Immutable order item data for audit compliance
 * - JPA auditing for order item lifecycle tracking
 * 
 * Design Decisions:
 * - UUID primary key for consistency and security
 * - @ManyToOne with Order (many items per order)
 * - @ManyToOne with MenuItem (reference to menu item)
 * - Price snapshot captured at order time (critical for accounting)
 * - Quantity as int (sufficient for order quantities)
 * - Subtotal calculated and persisted for performance
 * - No cascade on relationships (Order manages lifecycle)
 * 
 * Interview Points:
 * - Why capture unitPrice? Menu prices change, orders need historical accuracy
 * - Why @ManyToOne for both? Multiple order items can reference same menu item
 * - Why no cascade? OrderItem doesn't own Order or MenuItem lifecycle
 * - Why immutable after creation? Order integrity and audit compliance
 * - Why BigDecimal? Precision for monetary calculations and accounting
 * 
 * Business Rules:
 * - Order items are immutable after order confirmation
 * - Unit price captured from menu item at order time
 * - Subtotal = quantity * unitPrice
 * - Order items cannot exist without Order and MenuItem
 * - Menu item details captured for historical reference
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_item_order_id", columnList = "order_id"),
        @Index(name = "idx_order_item_menu_item_id", columnList = "menu_item_id"),
        @Index(name = "idx_order_item_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * @ManyToOne relationship with Order.
     * 
     *            Many OrderItems belong to one Order.
     *            FetchType.LAZY for performance - order loaded only when accessed.
     * 
     *            nullable = false ensures OrderItem always belongs to an Order.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * @ManyToOne relationship with MenuItem.
     * 
     *            Many OrderItems can reference the same MenuItem.
     *            FetchType.LAZY for performance - menu item loaded only when
     *            accessed.
     * 
     *            nullable = false ensures OrderItem always references a MenuItem.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    /**
     * Quantity of this menu item in the order.
     * 
     * Must be positive. Immutable after order confirmation.
     */
    @Column(name = "quantity", nullable = false)
    private int quantity;

    /**
     * Unit price of the menu item at the time the order was placed.
     * 
     * This is a CRITICAL price snapshot - even if MenuItem price changes,
     * the order preserves the price at time of ordering for:
     * - Historical accuracy
     * - Accounting compliance
     * - Customer billing integrity
     * - Financial reporting
     * 
     * BigDecimal with precision 10, scale 2 for monetary values.
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Subtotal for this order item (quantity * unitPrice).
     * 
     * Calculated and persisted for:
     * - Query performance
     * - Audit trail
     * - Financial reporting
     * - Historical accuracy
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Menu item name at time of ordering (snapshot for historical reference).
     * 
     * Captured in case menu item is renamed or deleted later.
     */
    @Column(name = "menu_item_name", nullable = false, length = 100)
    private String menuItemName;

    /**
     * Menu item description at time of ordering (snapshot).
     */
    @Column(name = "menu_item_description", length = 500)
    private String menuItemDescription;

    /**
     * Category name at time of ordering (snapshot).
     */
    @Column(name = "category_name", length = 50)
    private String categoryName;

    /**
     * Special instructions or customizations for this item.
     */
    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    /**
     * Audit fields for tracking order item creation and modification.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Constructor for creating OrderItem from MenuItem and quantity.
     * 
     * Captures price and details snapshot from MenuItem.
     * 
     * @param order    the order this item belongs to
     * @param menuItem the menu item being ordered
     * @param quantity the quantity ordered
     */
    public OrderItem(Order order, MenuItem menuItem, int quantity) {
        this.order = order;
        this.menuItem = menuItem;
        this.quantity = quantity;

        // Capture price snapshot
        this.unitPrice = menuItem.getPrice();

        // Capture menu item details snapshot
        this.menuItemName = menuItem.getName();
        this.menuItemDescription = menuItem.getDescription();
        this.categoryName = menuItem.getCategory() != null ? menuItem.getCategory().getName() : null;

        // Calculate subtotal
        calculateSubtotal();
    }

    /**
     * Constructor with special instructions.
     * 
     * @param order               the order this item belongs to
     * @param menuItem            the menu item being ordered
     * @param quantity            the quantity ordered
     * @param specialInstructions special instructions for this item
     */
    public OrderItem(Order order, MenuItem menuItem, int quantity, String specialInstructions) {
        this(order, menuItem, quantity);
        this.specialInstructions = specialInstructions;
    }

    // ================================================================
    // Business Methods
    // ================================================================

    /**
     * Calculates the subtotal for this order item.
     * 
     * Uses BigDecimal arithmetic for precise monetary calculations.
     * subtotal = quantity * unitPrice
     * 
     * @return the calculated subtotal
     */
    public BigDecimal calculateSubtotal() {
        if (unitPrice == null || quantity <= 0) {
            this.subtotal = BigDecimal.ZERO;
        } else {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return this.subtotal;
    }

    /**
     * Updates the quantity (only allowed before order confirmation).
     * 
     * @param newQuantity the new quantity (must be positive)
     * @throws IllegalArgumentException if quantity is not positive
     * @throws IllegalStateException    if order is already confirmed
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Check if order allows modification
        if (order != null && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify order item after order confirmation");
        }

        this.quantity = newQuantity;
        calculateSubtotal();
    }

    /**
     * Checks if this order item is for the specified menu item.
     * 
     * @param menuItemId the menu item ID to check
     * @return true if this order item is for the specified menu item
     */
    public boolean isForMenuItem(UUID menuItemId) {
        return menuItem != null && menuItemId != null &&
                menuItemId.equals(menuItem.getId());
    }

    /**
     * Gets the current menu item price (may differ from unit price).
     * 
     * @return current menu item price or null if menu item is null
     */
    public BigDecimal getCurrentMenuItemPrice() {
        return menuItem != null ? menuItem.getPrice() : null;
    }

    /**
     * Checks if the menu item price has changed since ordering.
     * 
     * @return true if current price differs from unit price
     */
    public boolean hasPriceChanged() {
        BigDecimal currentPrice = getCurrentMenuItemPrice();
        return currentPrice != null && unitPrice != null &&
                currentPrice.compareTo(unitPrice) != 0;
    }

    /**
     * Gets the price difference (current - unit price).
     * 
     * @return price difference or zero if no change
     */
    public BigDecimal getPriceDifference() {
        BigDecimal currentPrice = getCurrentMenuItemPrice();
        if (currentPrice != null && unitPrice != null) {
            return currentPrice.subtract(unitPrice);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Checks if the menu item is still available.
     * 
     * @return true if menu item is still available
     */
    public boolean isMenuItemAvailable() {
        return menuItem != null && menuItem.getAvailable();
    }

    /**
     * Gets the menu item ID for reference.
     * 
     * @return menu item ID or null if menu item is null
     */
    public UUID getMenuItemId() {
        return menuItem != null ? menuItem.getId() : null;
    }

    // ================================================================
    // JPA Lifecycle Callbacks
    // ================================================================

    /**
     * JPA callback method called before persisting.
     * Ensures subtotal is calculated before saving.
     */
    @PrePersist
    protected void onCreate() {
        calculateSubtotal();
    }

    /**
     * JPA callback method called before updating.
     * Ensures subtotal is calculated before saving.
     */
    @PreUpdate
    protected void onUpdate() {
        calculateSubtotal();
    }

    // ================================================================
    // toString, equals, hashCode
    // ================================================================

    @Override
    public String toString() {
        return String.format(
                "OrderItem{id=%s, quantity=%d, unitPrice=%s, subtotal=%s, menuItem=%s}",
                id, quantity, unitPrice, subtotal, menuItemName);
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

        OrderItem orderItem = (OrderItem) obj;
        return id != null && id.equals(orderItem.id);
    }

    /**
     * HashCode based on ID for entity identity.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
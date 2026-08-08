package com.raagaandroast.cart.entity;

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
 * CartItem entity representing an item in a customer's shopping cart.
 * 
 * This entity demonstrates several advanced JPA patterns:
 * - @ManyToOne relationships with Cart and MenuItem
 * - BigDecimal for monetary calculations (subtotal)
 * - Optimistic locking with @Version
 * - JPA auditing with @CreatedDate and @LastModifiedDate
 * - Business methods for subtotal calculation
 * - Proper validation constraints
 * 
 * Design Decisions:
 * - UUID primary key for security and distributed systems
 * - @ManyToOne with Cart (many cart items belong to one cart)
 * - @ManyToOne with MenuItem (many cart items can reference same menu item)
 * - quantity as int (sufficient for cart quantities)
 * - unitPrice captured at time of adding to cart (price snapshot)
 * - subtotal calculated as quantity * unitPrice
 * - No cascade on relationships (Cart and MenuItem manage their own lifecycle)
 * 
 * Interview Points:
 * - Why capture unitPrice? Menu item prices can change, but cart should
 * preserve price at time of adding
 * - Why @ManyToOne for both Cart and MenuItem? Multiple cart items can exist
 * for same menu item in different carts
 * - Why no cascade? CartItem doesn't own Cart or MenuItem lifecycle
 * - Why BigDecimal for unitPrice and subtotal? Precision for monetary
 * calculations
 * - Why @Version? Optimistic locking for concurrent cart updates
 * 
 * Business Rules:
 * - quantity must be positive
 * - unitPrice must be positive
 * - subtotal = quantity * unitPrice
 * - CartItem cannot exist without Cart and MenuItem
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart_id", columnList = "cart_id"),
        @Index(name = "idx_cart_item_menu_item_id", columnList = "menu_item_id"),
        @Index(name = "idx_cart_item_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * @ManyToOne relationship with Cart.
     * 
     *            Many CartItems belong to one Cart.
     *            FetchType.LAZY for performance - cart loaded only when accessed.
     * 
     * @JoinColumn specifies the foreign key column name.
     *             nullable = false ensures CartItem always belongs to a Cart.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /**
     * @ManyToOne relationship with MenuItem.
     * 
     *            Many CartItems can reference the same MenuItem.
     *            FetchType.LAZY for performance - menu item loaded only when
     *            accessed.
     * 
     *            nullable = false ensures CartItem always references a MenuItem.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    /**
     * Quantity of this menu item in the cart.
     * 
     * Must be positive (validated at service layer).
     * int is sufficient for cart quantities (no need for Long).
     */
    @Column(name = "quantity", nullable = false)
    private int quantity;

    /**
     * Unit price of the menu item at the time it was added to cart.
     * 
     * This is a price snapshot - even if MenuItem price changes,
     * cart preserves the price at time of adding.
     * 
     * BigDecimal with precision 10, scale 2 for monetary values.
     * Must be positive (validated at service layer).
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Subtotal for this cart item (quantity * unitPrice).
     * 
     * This could be calculated dynamically, but persisting it:
     * - Improves query performance
     * - Provides audit trail
     * - Simplifies reporting
     * 
     * Updated automatically when quantity or unitPrice changes.
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Optimistic locking version field.
     * 
     * Prevents lost updates when multiple operations modify the same cart item.
     * JPA automatically increments this on each update.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Audit fields for tracking cart item creation and modification.
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
     * Constructor for creating a new CartItem.
     * 
     * @param cart      the cart this item belongs to
     * @param menuItem  the menu item being added
     * @param quantity  the quantity of the menu item
     * @param unitPrice the unit price at time of adding
     */
    public CartItem(Cart cart, MenuItem menuItem, int quantity, BigDecimal unitPrice) {
        this.cart = cart;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    // ================================================================
    // Business Methods
    // ================================================================

    /**
     * Calculates the subtotal for this cart item.
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
     * Updates the quantity and recalculates subtotal.
     * 
     * @param newQuantity the new quantity (must be positive)
     * @throws IllegalArgumentException if quantity is not positive
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        calculateSubtotal();
    }

    /**
     * Updates the unit price and recalculates subtotal.
     * 
     * This might be used if menu item price changes and we want to update cart.
     * However, typically we preserve the original price.
     * 
     * @param newUnitPrice the new unit price (must be positive)
     * @throws IllegalArgumentException if unit price is not positive
     */
    public void updateUnitPrice(BigDecimal newUnitPrice) {
        if (newUnitPrice == null || newUnitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.unitPrice = newUnitPrice;
        calculateSubtotal();
    }

    /**
     * Increases the quantity by the specified amount.
     * 
     * @param additionalQuantity the amount to add (must be positive)
     * @throws IllegalArgumentException if additional quantity is not positive
     */
    public void increaseQuantity(int additionalQuantity) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }
        this.quantity += additionalQuantity;
        calculateSubtotal();
    }

    /**
     * Decreases the quantity by the specified amount.
     * 
     * @param decreaseAmount the amount to subtract (must be positive)
     * @throws IllegalArgumentException if decrease amount is not positive or would
     *                                  result in non-positive quantity
     */
    public void decreaseQuantity(int decreaseAmount) {
        if (decreaseAmount <= 0) {
            throw new IllegalArgumentException("Decrease amount must be positive");
        }
        if (this.quantity - decreaseAmount <= 0) {
            throw new IllegalArgumentException("Cannot decrease quantity below 1");
        }
        this.quantity -= decreaseAmount;
        calculateSubtotal();
    }

    /**
     * Checks if this cart item is for the specified menu item.
     * 
     * @param menuItemId the menu item ID to check
     * @return true if this cart item is for the specified menu item
     */
    public boolean isForMenuItem(UUID menuItemId) {
        return menuItem != null && menuItemId != null &&
                menuItemId.equals(menuItem.getId());
    }

    /**
     * Gets the menu item name for display purposes.
     * 
     * @return the menu item name or "Unknown" if menu item is null
     */
    public String getMenuItemName() {
        return menuItem != null ? menuItem.getName() : "Unknown";
    }

    /**
     * Gets the menu item ID for reference purposes.
     * 
     * @return the menu item ID or null if menu item is null
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
                "CartItem{id=%s, quantity=%d, unitPrice=%s, subtotal=%s, menuItem=%s, version=%d}",
                id, quantity, unitPrice, subtotal, getMenuItemName(), version);
    }

    /**
     * Equals based on ID for entity identity.
     * 
     * Important: Only use ID for equals/hashCode in JPA entities
     * to avoid issues with proxy objects and lazy loading.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        CartItem cartItem = (CartItem) obj;
        return id != null && id.equals(cartItem.id);
    }

    /**
     * HashCode based on ID for entity identity.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
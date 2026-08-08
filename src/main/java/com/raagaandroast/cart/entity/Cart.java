package com.raagaandroast.cart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.raagaandroast.customer.entity.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cart entity representing a customer's shopping cart.
 * 
 * This entity demonstrates several advanced JPA patterns:
 * - @OneToOne bidirectional relationship with Customer
 * - @OneToMany relationship with CartItem using proper cascade
 * - BigDecimal for monetary calculations (never double for money!)
 * - Optimistic locking with @Version
 * - JPA auditing with @CreatedDate and @LastModifiedDate
 * - Business methods for cart operations
 * 
 * Design Decisions:
 * - UUID primary key for security and distributed systems
 * - Bidirectional relationship with Customer for easy navigation
 * - Cascade PERSIST and MERGE for CartItems (not REMOVE to prevent accidental
 * deletion)
 * - orphanRemoval = true for CartItems (when removed from cart, delete from DB)
 * - BigDecimal with precision 10, scale 2 for monetary values
 * - Business methods encapsulate cart logic (calculateTotal, addItem, etc.)
 * 
 * Interview Points:
 * - Why @OneToOne with Customer? Each customer has exactly one active cart
 * - Why BigDecimal instead of double? Precision for monetary calculations
 * - Why orphanRemoval = true? CartItems have no meaning without a Cart
 * - Why CascadeType.PERSIST and MERGE? We want to save CartItems with Cart
 * - Why not CascadeType.REMOVE? We don't want to delete Customer when Cart is
 * deleted
 * - Why @Version? Optimistic locking for concurrent cart updates
 * 
 * Performance Considerations:
 * - LAZY loading for cartItems (fetched only when needed)
 * - Use JOIN FETCH in repository when loading cart with items
 * - Index on customer_id for fast cart lookup
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_customer_id", columnList = "customer_id"),
        @Index(name = "idx_cart_created_at", columnList = "created_at"),
        @Index(name = "idx_cart_updated_at", columnList = "updated_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Bidirectional @OneToOne relationship with Customer.
     * Each customer has exactly one active cart.
     * 
     * mappedBy = "cart" indicates that Customer entity owns the relationship
     * (Customer has the foreign key).
     * 
     * FetchType.LAZY for performance - customer loaded only when accessed.
     */
    @OneToOne(mappedBy = "cart", fetch = FetchType.LAZY)
    private Customer customer;

    /**
     * @OneToMany relationship with CartItem.
     * 
     *            CascadeType.PERSIST: When cart is saved, save new cart items
     *            CascadeType.MERGE: When cart is updated, update cart items
     *            CascadeType.REFRESH: When cart is refreshed, refresh cart items
     * 
     *            orphanRemoval = true: When CartItem is removed from this list,
     *            delete it from database (CartItems have no meaning without Cart)
     * 
     *            FetchType.LAZY: CartItems loaded only when accessed
     *            (use JOIN FETCH in repository when needed)
     */
    @OneToMany(mappedBy = "cart", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REFRESH }, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    /**
     * Total amount of the cart.
     * 
     * BigDecimal with precision 10, scale 2 for monetary values.
     * This is calculated dynamically but can be persisted for performance.
     * 
     * In production, consider:
     * - Calculating on-the-fly for accuracy
     * - Caching for performance
     * - Hybrid approach: calculate and cache, invalidate on changes
     */
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Optimistic locking version field.
     * 
     * Prevents lost updates when multiple users modify the same cart.
     * JPA automatically increments this on each update.
     * 
     * Interview Question: What happens if two users modify the same cart?
     * Answer: OptimisticLockException is thrown, application should handle
     * gracefully
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Audit fields for tracking cart creation and modification.
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
     * Calculates the total amount of all items in the cart.
     * 
     * Uses BigDecimal arithmetic for precise monetary calculations.
     * This method demonstrates proper handling of monetary values.
     * 
     * @return the total amount as BigDecimal
     */
    public BigDecimal calculateTotal() {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Updates the total amount by calculating from cart items.
     * This method should be called after any cart modifications.
     */
    public void updateTotal() {
        this.totalAmount = calculateTotal();
    }

    /**
     * Adds a CartItem to this cart.
     * 
     * This method maintains the bidirectional relationship
     * and updates the total amount.
     * 
     * @param cartItem the cart item to add
     */
    public void addCartItem(CartItem cartItem) {
        if (cartItem == null) {
            throw new IllegalArgumentException("CartItem cannot be null");
        }

        cartItems.add(cartItem);
        cartItem.setCart(this);
        updateTotal();
    }

    /**
     * Removes a CartItem from this cart.
     * 
     * This method maintains the bidirectional relationship
     * and updates the total amount.
     * 
     * @param cartItem the cart item to remove
     */
    public void removeCartItem(CartItem cartItem) {
        if (cartItem == null) {
            return;
        }

        cartItems.remove(cartItem);
        cartItem.setCart(null);
        updateTotal();
    }

    /**
     * Clears all items from the cart.
     * 
     * Due to orphanRemoval = true, this will delete all CartItems from database.
     */
    public void clearItems() {
        cartItems.clear();
        updateTotal();
    }

    /**
     * Gets the number of items in the cart.
     * 
     * @return total quantity of all items
     */
    public int getTotalItemCount() {
        if (cartItems == null || cartItems.isEmpty()) {
            return 0;
        }

        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Gets the number of unique items in the cart.
     * 
     * @return number of different menu items
     */
    public int getUniqueItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    /**
     * Checks if the cart is empty.
     * 
     * @return true if cart has no items
     */
    public boolean isEmpty() {
        return cartItems == null || cartItems.isEmpty();
    }

    /**
     * Finds a cart item by menu item ID.
     * 
     * @param menuItemId the menu item ID to search for
     * @return the cart item if found, null otherwise
     */
    public CartItem findCartItemByMenuItemId(UUID menuItemId) {
        if (cartItems == null || menuItemId == null) {
            return null;
        }

        return cartItems.stream()
                .filter(item -> item.getMenuItem() != null &&
                        menuItemId.equals(item.getMenuItem().getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if the cart contains a specific menu item.
     * 
     * @param menuItemId the menu item ID to check
     * @return true if cart contains the menu item
     */
    public boolean containsMenuItem(UUID menuItemId) {
        return findCartItemByMenuItemId(menuItemId) != null;
    }

    // ================================================================
    // Helper Methods for JPA
    // ================================================================

    /**
     * JPA callback method called before persisting.
     * Ensures total is calculated before saving.
     */
    @PrePersist
    protected void onCreate() {
        updateTotal();
    }

    /**
     * JPA callback method called before updating.
     * Ensures total is calculated before saving.
     */
    @PreUpdate
    protected void onUpdate() {
        updateTotal();
    }

    // ================================================================
    // toString, equals, hashCode
    // ================================================================

    @Override
    public String toString() {
        return String.format(
                "Cart{id=%s, totalAmount=%s, itemCount=%d, version=%d, createdAt=%s}",
                id, totalAmount, getUniqueItemCount(), version, createdAt);
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

        Cart cart = (Cart) obj;
        return id != null && id.equals(cart.id);
    }

    /**
     * HashCode based on ID for entity identity.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
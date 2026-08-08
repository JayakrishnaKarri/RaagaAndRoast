package com.raagaandroast.customer.entity;

import com.raagaandroast.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Customer entity representing café customers.
 * 
 * This entity bridges the authentication system (User) with the business
 * domain.
 * It contains customer-specific information needed for ordering, delivery,
 * and customer relationship management.
 * 
 * Design Decisions:
 * - One-to-One relationship with User for clean separation of concerns
 * - UUID primary key for consistency and security
 * - JPA Auditing for tracking customer lifecycle
 * - Optimistic locking for concurrent access control
 * - One-to-Many relationship with Address for multiple delivery locations
 * 
 * Relationship Design:
 * - User (1) ↔ (1) Customer: Authentication vs Business separation
 * - Customer (1) → (N) Address: Multiple delivery locations
 * - Customer (1) → (1) Cart: Shopping cart (to be implemented)
 * - Customer (1) → (N) Order: Order history (to be implemented)
 * 
 * Interview Points:
 * - Why separate Customer from User? Single Responsibility Principle
 * - Why One-to-One vs embedding? Different lifecycle, different concerns
 * - Why UUID? Security, distributed systems, no sequence conflicts
 * - Why optimistic locking? Better performance than pessimistic locks
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_user_id", columnList = "user_id"),
        @Index(name = "idx_customers_phone", columnList = "phone_number"),
        @Index(name = "idx_customers_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    /**
     * Primary key using UUID for consistency with other entities.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * One-to-One relationship with User entity.
     * 
     * Design Notes:
     * - LAZY loading: User loaded only when needed
     * - CascadeType.PERSIST: New users can be created with customers
     * - No CascadeType.REMOVE: Users exist independently
     * - fetch = LAZY: Explicit for clarity and performance
     * - optional = false: Every customer must have a user
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Customer's first name.
     * Used for personalization and delivery information.
     */
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * Customer's last name.
     * Used for personalization and delivery information.
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Customer's phone number.
     * Used for delivery coordination and order updates.
     * 
     * Pattern validates:
     * - Optional country code (+1, +91, etc.)
     * - 10-15 digits
     * - Optional spaces, hyphens, parentheses
     */
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4,6}$", message = "Phone number must be valid (e.g., +1-555-123-4567 or 555-123-4567)")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Customer's date of birth.
     * Used for birthday promotions and age verification if needed.
     */
    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    /**
     * Customer preferences as JSON or simple text.
     * Could include dietary restrictions, favorite items, etc.
     */
    @Size(max = 1000, message = "Preferences must not exceed 1000 characters")
    @Column(name = "preferences", length = 1000)
    private String preferences;

    /**
     * Whether the customer wants to receive marketing communications.
     */
    @Builder.Default
    @Column(name = "marketing_consent", nullable = false)
    private Boolean marketingConsent = false;

    /**
     * One-to-Many relationship with Address entity.
     * 
     * Design Notes:
     * - mappedBy: Address entity owns the relationship
     * - LAZY loading: Addresses loaded only when needed
     * - CascadeType.ALL: Address lifecycle tied to customer
     * - orphanRemoval: Remove addresses when removed from customer
     * - fetch = LAZY: Performance optimization
     */
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    /**
     * One-to-One relationship with Cart entity.
     *
     * Design Notes:
     * - Each customer has exactly one active shopping cart
     * - CascadeType.ALL: Cart lifecycle tied to customer
     * - orphanRemoval = true: Remove cart when customer is deleted
     * - fetch = LAZY: Cart loaded only when needed
     * - Customer owns the relationship (has the foreign key)
     *
     * Interview Points:
     * - Why @OneToOne? Each customer has exactly one active cart
     * - Why CascadeType.ALL? Cart has no meaning without customer
     * - Why orphanRemoval? Cart should be deleted with customer
     * - Why LAZY? Cart might not always be needed when loading customer
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private com.raagaandroast.cart.entity.Cart cart;

    /**
     * Audit field: When the customer record was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Audit field: When the customer record was last modified.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Version field for optimistic locking.
     * Prevents concurrent modification conflicts.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // ================================================================
    // Business Methods
    // ================================================================

    /**
     * Adds an address to the customer's address list.
     * Maintains bidirectional relationship consistency.
     * 
     * @param address the address to add
     */
    public void addAddress(Address address) {
        if (address != null) {
            addresses.add(address);
            address.setCustomer(this);
        }
    }

    /**
     * Removes an address from the customer's address list.
     * Maintains bidirectional relationship consistency.
     * 
     * @param address the address to remove
     */
    public void removeAddress(Address address) {
        if (address != null) {
            addresses.remove(address);
            address.setCustomer(null);
        }
    }

    /**
     * Gets the customer's full name.
     * Convenience method for display purposes.
     * 
     * @return the full name (first + last)
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Gets the primary address (first address in the list).
     * Convenience method for default delivery address.
     * 
     * @return the primary address or null if no addresses
     */
    public Address getPrimaryAddress() {
        return addresses.isEmpty() ? null : addresses.get(0);
    }

    /**
     * Gets addresses by type.
     * Useful for finding specific address types (HOME, WORK, etc.).
     * 
     * @param type the address type to filter by
     * @return list of addresses of the specified type
     */
    public List<Address> getAddressesByType(AddressType type) {
        return addresses.stream()
                .filter(address -> address.getType() == type)
                .toList();
    }

    /**
     * Checks if the customer has any addresses.
     * 
     * @return true if customer has at least one address
     */
    public boolean hasAddresses() {
        return !addresses.isEmpty();
    }

    /**
     * Gets the customer's username from the associated user.
     * Convenience method for logging and identification.
     * 
     * @return the username or null if user is not loaded
     */
    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }

    /**
     * Gets the customer's email from the associated user.
     * Convenience method for communication.
     *
     * @return the email or null if user is not loaded
     */
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    /**
     * Gets or creates the customer's cart.
     * If no cart exists, creates a new one.
     *
     * @return the customer's cart
     */
    public com.raagaandroast.cart.entity.Cart getOrCreateCart() {
        if (cart == null) {
            cart = new com.raagaandroast.cart.entity.Cart();
            cart.setCustomer(this);
        }
        return cart;
    }

    /**
     * Checks if the customer has a cart.
     *
     * @return true if customer has a cart
     */
    public boolean hasCart() {
        return cart != null;
    }

    /**
     * Clears the customer's cart by setting it to null.
     * Due to orphanRemoval = true, this will delete the cart from database.
     */
    public void clearCart() {
        if (cart != null) {
            cart.setCustomer(null);
            cart = null;
        }
    }

    // ================================================================
    // Object Methods
    // ================================================================

    /**
     * Custom equals based on business key (user relationship).
     * Two customers are equal if they have the same associated user.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Customer customer))
            return false;
        return user != null && user.equals(customer.user);
    }

    /**
     * Custom hashCode based on business key (user relationship).
     */
    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }

    /**
     * Custom toString that provides useful information without circular references.
     */
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", addressCount=" + (addresses != null ? addresses.size() : 0) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
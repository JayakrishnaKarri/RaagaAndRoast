package com.raagaandroast.customer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.util.UUID;

/**
 * Address entity representing customer delivery addresses.
 * 
 * This entity stores customer address information for delivery purposes.
 * It supports multiple address types (HOME, WORK, OTHER) and maintains
 * a many-to-one relationship with Customer.
 * 
 * Design Decisions:
 * - UUID primary key for consistency and security
 * - Many-to-One relationship with Customer (customers can have multiple
 * addresses)
 * - AddressType enum for type safety and validation
 * - JPA Auditing for tracking address lifecycle
 * - Optimistic locking for concurrent access control
 * - Comprehensive validation for address fields
 * 
 * Relationship Design:
 * - Address (N) → (1) Customer: Multiple addresses per customer
 * - Address uses AddressType enum for categorization
 * 
 * Interview Points:
 * - Why separate Address entity? Normalization, multiple addresses per customer
 * - Why Many-to-One vs embedded? Flexibility, separate lifecycle
 * - Why AddressType enum? Type safety, validation, business rules
 * - Why optimistic locking? Performance, concurrent access control
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_addresses_customer_id", columnList = "customer_id"),
        @Index(name = "idx_addresses_type", columnList = "address_type"),
        @Index(name = "idx_addresses_city", columnList = "city"),
        @Index(name = "idx_addresses_postal_code", columnList = "postal_code")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    /**
     * Primary key using UUID for consistency with other entities.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Many-to-One relationship with Customer entity.
     * 
     * Design Notes:
     * - LAZY loading: Customer loaded only when needed
     * - No cascade: Address lifecycle independent of customer deletion
     * - fetch = LAZY: Performance optimization
     * - optional = false: Every address must belong to a customer
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Type of address (HOME, WORK, OTHER).
     * Uses enum for type safety and validation.
     */
    @NotNull(message = "Address type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType type;

    /**
     * Street address line 1.
     * Primary address information (street number, street name).
     */
    @NotBlank(message = "Street address is required")
    @Size(min = 5, max = 100, message = "Street address must be between 5 and 100 characters")
    @Column(name = "street_address", nullable = false, length = 100)
    private String streetAddress;

    /**
     * Street address line 2.
     * Optional additional address information (apartment, suite, etc.).
     */
    @Size(max = 100, message = "Address line 2 must not exceed 100 characters")
    @Column(name = "address_line_2", length = 100)
    private String addressLine2;

    /**
     * City name.
     * Required for delivery routing and validation.
     */
    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    @Column(name = "city", nullable = false, length = 50)
    private String city;

    /**
     * State or province.
     * Required for delivery routing and tax calculation.
     */
    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    @Column(name = "state", nullable = false, length = 50)
    private String state;

    /**
     * Postal code or ZIP code.
     * Required for delivery routing and validation.
     */
    @NotBlank(message = "Postal code is required")
    @Size(min = 3, max = 20, message = "Postal code must be between 3 and 20 characters")
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    /**
     * Country name or code.
     * Defaults to appropriate country for the business.
     */
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
    @Builder.Default
    @Column(name = "country", nullable = false, length = 50)
    private String country = "India";

    /**
     * Optional delivery instructions.
     * Special instructions for delivery personnel.
     */
    @Size(max = 500, message = "Delivery instructions must not exceed 500 characters")
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;

    /**
     * Whether this is the default address for the customer.
     * Only one address per customer should be default.
     */
    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * Whether this address is currently active.
     * Allows soft deletion of addresses.
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Audit field: When the address was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Audit field: When the address was last modified.
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
     * Gets the full address as a formatted string.
     * Useful for display purposes and delivery labels.
     * 
     * @return formatted address string
     */
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();

        address.append(streetAddress);

        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            address.append(", ").append(addressLine2);
        }

        address.append(", ").append(city);
        address.append(", ").append(state);
        address.append(" ").append(postalCode);

        if (country != null && !country.trim().isEmpty()) {
            address.append(", ").append(country);
        }

        return address.toString();
    }

    /**
     * Gets a short address format for display in lists.
     * Shows only essential information.
     * 
     * @return short address string
     */
    public String getShortAddress() {
        return streetAddress + ", " + city + ", " + state + " " + postalCode;
    }

    /**
     * Checks if this address has delivery instructions.
     * 
     * @return true if delivery instructions are present
     */
    public boolean hasDeliveryInstructions() {
        return deliveryInstructions != null && !deliveryInstructions.trim().isEmpty();
    }

    /**
     * Marks this address as the default address.
     * Note: Business logic should ensure only one default per customer.
     */
    public void markAsDefault() {
        this.isDefault = true;
    }

    /**
     * Removes the default flag from this address.
     */
    public void removeDefaultFlag() {
        this.isDefault = false;
    }

    /**
     * Activates this address.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates this address (soft delete).
     */
    public void deactivate() {
        this.isActive = false;
        this.isDefault = false; // Default address cannot be inactive
    }

    /**
     * Gets the customer ID for this address.
     * Convenience method for queries and logging.
     * 
     * @return customer ID or null if customer is not loaded
     */
    public UUID getCustomerId() {
        return customer != null ? customer.getId() : null;
    }

    /**
     * Checks if this address is suitable for delivery.
     * An address is suitable if it's active and has all required fields.
     * 
     * @return true if suitable for delivery
     */
    public boolean isSuitableForDelivery() {
        return isActive &&
                streetAddress != null && !streetAddress.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                state != null && !state.trim().isEmpty() &&
                postalCode != null && !postalCode.trim().isEmpty();
    }

    // ================================================================
    // Object Methods
    // ================================================================

    /**
     * Custom equals based on business key (customer + address details).
     * Two addresses are equal if they belong to the same customer and have the same
     * address details.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Address address))
            return false;

        return customer != null && customer.equals(address.customer) &&
                streetAddress != null && streetAddress.equals(address.streetAddress) &&
                city != null && city.equals(address.city) &&
                state != null && state.equals(address.state) &&
                postalCode != null && postalCode.equals(address.postalCode);
    }

    /**
     * Custom hashCode based on business key.
     */
    @Override
    public int hashCode() {
        int result = customer != null ? customer.hashCode() : 0;
        result = 31 * result + (streetAddress != null ? streetAddress.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        return result;
    }

    /**
     * Custom toString that provides useful information without circular references.
     */
    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", type=" + type +
                ", streetAddress='" + streetAddress + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", isDefault=" + isDefault +
                ", isActive=" + isActive +
                ", customerId=" + getCustomerId() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
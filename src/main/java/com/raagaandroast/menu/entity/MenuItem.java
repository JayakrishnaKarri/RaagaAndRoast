package com.raagaandroast.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MenuItem entity representing individual menu items in the café system.
 * 
 * This entity demonstrates advanced JPA patterns including:
 * - BigDecimal for monetary values (never use double for money!)
 * - UUID primary keys for distributed systems
 * - JPA auditing for tracking creation and modification times
 * - Optimistic locking with @Version for concurrent access control
 * - Many-to-One relationship with Category entity
 * - Comprehensive business methods for menu item management
 * 
 * Design Decisions:
 * - BigDecimal for price to avoid floating-point precision issues
 * - UUID for primary key to support distributed architecture
 * - Auditing enabled for compliance and debugging
 * - Optimistic locking to handle concurrent modifications
 * - Business methods encapsulated in entity for domain-driven design
 * 
 * Performance Considerations:
 * - LAZY loading for category relationship when not needed
 * - Indexed fields for common query patterns (name, category, available)
 * - Version field for optimistic locking performance
 * 
 * Interview Points:
 * - Why BigDecimal over double? Precision for monetary calculations
 * - Why UUID over Long? Distributed systems, no central ID generation
 * - Why optimistic locking? Better performance than pessimistic for read-heavy
 * workloads
 * - Why LAZY loading? Performance optimization, load data only when needed
 * - How to handle money in Java? BigDecimal with proper scale and rounding
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "menu_items", indexes = {
        @Index(name = "idx_menu_item_name", columnList = "name"),
        @Index(name = "idx_menu_item_category", columnList = "category_id"),
        @Index(name = "idx_menu_item_available", columnList = "available"),
        @Index(name = "idx_menu_item_price", columnList = "price"),
        @Index(name = "idx_menu_item_category_available", columnList = "category_id, available")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Price stored as BigDecimal for precise monetary calculations.
     * Scale of 2 for cents precision, precision of 10 for large values.
     * Never use double or float for monetary values!
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "preparation_time_minutes")
    private Integer preparationTimeMinutes;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "vegetarian", nullable = false)
    private Boolean vegetarian = false;

    @Column(name = "vegan", nullable = false)
    private Boolean vegan = false;

    @Column(name = "gluten_free", nullable = false)
    private Boolean glutenFree = false;

    @Column(name = "spicy", nullable = false)
    private Boolean spicy = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // ================================================================
    // Relationships
    // ================================================================

    /**
     * Many-to-One relationship with Category.
     * LAZY loading by default for performance.
     * JoinColumn specifies the foreign key column.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Constructor for creating a new menu item.
     * Automatically generates UUID and sets default values.
     * 
     * @param name        the item name
     * @param description the item description
     * @param price       the item price
     * @param category    the category
     */
    public MenuItem(String name, String description, BigDecimal price, Category category) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.available = true;
        this.vegetarian = false;
        this.vegan = false;
        this.glutenFree = false;
        this.spicy = false;
    }

    /**
     * Constructor with dietary information.
     * 
     * @param name        the item name
     * @param description the item description
     * @param price       the item price
     * @param category    the category
     * @param vegetarian  vegetarian flag
     * @param vegan       vegan flag
     * @param glutenFree  gluten-free flag
     * @param spicy       spicy flag
     */
    public MenuItem(String name, String description, BigDecimal price, Category category,
            Boolean vegetarian, Boolean vegan, Boolean glutenFree, Boolean spicy) {
        this(name, description, price, category);
        this.vegetarian = vegetarian != null ? vegetarian : false;
        this.vegan = vegan != null ? vegan : false;
        this.glutenFree = glutenFree != null ? glutenFree : false;
        this.spicy = spicy != null ? spicy : false;
    }

    // ================================================================
    // Business Methods
    // ================================================================

    /**
     * Makes the menu item available.
     * Business method to ensure proper state management.
     */
    public void makeAvailable() {
        this.available = true;
    }

    /**
     * Makes the menu item unavailable.
     * Business method to ensure proper state management.
     */
    public void makeUnavailable() {
        this.available = false;
    }

    /**
     * Checks if the menu item is available.
     * 
     * @return true if the item is available
     */
    public boolean isAvailable() {
        return available != null && available;
    }

    /**
     * Updates the price with validation.
     * Business method to ensure price constraints.
     * 
     * @param newPrice the new price
     * @throws IllegalArgumentException if price is invalid
     */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (newPrice.scale() > 2) {
            throw new IllegalArgumentException("Price cannot have more than 2 decimal places");
        }
        this.price = newPrice;
    }

    /**
     * Updates the menu item information.
     * Business method for controlled updates.
     * 
     * @param name        the new name
     * @param description the new description
     * @param price       the new price
     */
    public void updateInfo(String name, String description, BigDecimal price) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim().isEmpty() ? null : description.trim();
        }
        if (price != null) {
            updatePrice(price);
        }
    }

    /**
     * Updates dietary information.
     * 
     * @param vegetarian vegetarian flag
     * @param vegan      vegan flag
     * @param glutenFree gluten-free flag
     * @param spicy      spicy flag
     */
    public void updateDietaryInfo(Boolean vegetarian, Boolean vegan, Boolean glutenFree, Boolean spicy) {
        this.vegetarian = vegetarian != null ? vegetarian : false;
        this.vegan = vegan != null ? vegan : false;
        this.glutenFree = glutenFree != null ? glutenFree : false;
        this.spicy = spicy != null ? spicy : false;
    }

    /**
     * Gets the formatted price as a string.
     * 
     * @return formatted price (e.g., "12.99")
     */
    public String getFormattedPrice() {
        return price != null ? price.toString() : "0.00";
    }

    /**
     * Gets the price in cents as a long.
     * Useful for calculations and storage in some systems.
     * 
     * @return price in cents
     */
    public long getPriceInCents() {
        return price != null ? price.multiply(new BigDecimal("100")).longValue() : 0L;
    }

    /**
     * Checks if the item has dietary restrictions.
     * 
     * @return true if the item has any dietary flags set
     */
    public boolean hasDietaryInfo() {
        return (vegetarian != null && vegetarian) ||
                (vegan != null && vegan) ||
                (glutenFree != null && glutenFree) ||
                (spicy != null && spicy);
    }

    /**
     * Gets a display-friendly name for the menu item.
     * 
     * @return formatted menu item name
     */
    public String getDisplayName() {
        return name != null ? name : "Unnamed Item";
    }

    /**
     * Checks if the item has an image.
     * 
     * @return true if the item has an image URL
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    /**
     * Checks if the item has preparation time information.
     * 
     * @return true if preparation time is set
     */
    public boolean hasPreparationTime() {
        return preparationTimeMinutes != null && preparationTimeMinutes > 0;
    }

    /**
     * Checks if the item has calorie information.
     * 
     * @return true if calories are set
     */
    public boolean hasCalorieInfo() {
        return calories != null && calories > 0;
    }

    /**
     * Validates if the menu item is properly configured.
     * 
     * @return true if the item is valid for ordering
     */
    public boolean isValidForOrdering() {
        return name != null && !name.trim().isEmpty() &&
                price != null && price.compareTo(BigDecimal.ZERO) > 0 &&
                category != null && category.isActive() &&
                isAvailable();
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MenuItem))
            return false;
        MenuItem menuItem = (MenuItem) o;
        return id != null && id.equals(menuItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("MenuItem{id=%s, name='%s', price=%s, available=%s}",
                id, name, price, available);
    }

    // ================================================================
    // JPA Lifecycle Methods
    // ================================================================

    /**
     * Pre-persist callback to ensure UUID is set and validate data.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (available == null) {
            available = true;
        }
        if (vegetarian == null) {
            vegetarian = false;
        }
        if (vegan == null) {
            vegan = false;
        }
        if (glutenFree == null) {
            glutenFree = false;
        }
        if (spicy == null) {
            spicy = false;
        }
        validatePrice();
    }

    /**
     * Pre-update callback for validation.
     */
    @PreUpdate
    protected void onUpdate() {
        if (name != null) {
            name = name.trim();
        }
        if (description != null && description.trim().isEmpty()) {
            description = null;
        }
        if (imageUrl != null && imageUrl.trim().isEmpty()) {
            imageUrl = null;
        }
        validatePrice();
    }

    /**
     * Validates the price field.
     * 
     * @throws IllegalStateException if price is invalid
     */
    private void validatePrice() {
        if (price == null) {
            throw new IllegalStateException("Price cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Price must be greater than zero");
        }
        if (price.scale() > 2) {
            throw new IllegalStateException("Price cannot have more than 2 decimal places");
        }
    }
}
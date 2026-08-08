package com.raagaandroast.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
 * Category entity representing menu categories in the café system.
 * 
 * This entity demonstrates advanced JPA patterns including:
 * - UUID primary keys for distributed systems
 * - JPA auditing for tracking creation and modification times
 * - Optimistic locking with @Version for concurrent access control
 * - One-to-Many relationship with MenuItem entities
 * - Soft delete pattern with active flag
 * - Business methods for category management
 * 
 * Design Decisions:
 * - UUID for primary key to support distributed architecture
 * - Auditing enabled for compliance and debugging
 * - Optimistic locking to handle concurrent modifications
 * - Soft delete to preserve referential integrity
 * - Business methods encapsulated in entity for domain-driven design
 * 
 * Performance Considerations:
 * - LAZY loading for menuItems collection to prevent N+1 problems
 * - Indexed fields for common query patterns
 * - Version field for optimistic locking performance
 * 
 * Interview Points:
 * - Why UUID over Long? Distributed systems, no central ID generation
 * - Why optimistic locking? Better performance than pessimistic for read-heavy
 * workloads
 * - Why soft delete? Referential integrity, audit trail, business requirements
 * - Why LAZY loading? Performance optimization, load data only when needed
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_name", columnList = "name"),
        @Index(name = "idx_category_active", columnList = "active"),
        @Index(name = "idx_category_display_order", columnList = "display_order")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

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
     * One-to-Many relationship with MenuItem.
     * LAZY loading to prevent N+1 problems.
     * CascadeType.PERSIST and MERGE for category operations.
     * orphanRemoval = false to prevent accidental deletion of menu items.
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<MenuItem> menuItems = new ArrayList<>();

    // ================================================================
    // Constructors
    // ================================================================

    /**
     * Constructor for creating a new category.
     * Automatically generates UUID and sets default values.
     * 
     * @param name         the category name
     * @param description  the category description
     * @param displayOrder the display order
     */
    public Category(String name, String description, Integer displayOrder) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    /**
     * Constructor for creating a category with image.
     * 
     * @param name         the category name
     * @param description  the category description
     * @param displayOrder the display order
     * @param imageUrl     the image URL
     */
    public Category(String name, String description, Integer displayOrder, String imageUrl) {
        this(name, description, displayOrder);
        this.imageUrl = imageUrl;
    }

    // ================================================================
    // Business Methods
    // ================================================================

    /**
     * Activates the category.
     * Business method to ensure proper state management.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivates the category.
     * Business method to ensure proper state management.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Checks if the category is active.
     * 
     * @return true if the category is active
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Checks if the category has menu items.
     * 
     * @return true if the category has menu items
     */
    public boolean hasMenuItems() {
        return menuItems != null && !menuItems.isEmpty();
    }

    /**
     * Gets the count of active menu items in this category.
     * 
     * @return count of active menu items
     */
    public long getActiveMenuItemCount() {
        if (menuItems == null) {
            return 0;
        }
        return menuItems.stream()
                .filter(MenuItem::isAvailable)
                .count();
    }

    /**
     * Adds a menu item to this category.
     * Maintains bidirectional relationship consistency.
     * 
     * @param menuItem the menu item to add
     */
    public void addMenuItem(MenuItem menuItem) {
        if (menuItems == null) {
            menuItems = new ArrayList<>();
        }
        menuItems.add(menuItem);
        menuItem.setCategory(this);
    }

    /**
     * Removes a menu item from this category.
     * Maintains bidirectional relationship consistency.
     * 
     * @param menuItem the menu item to remove
     */
    public void removeMenuItem(MenuItem menuItem) {
        if (menuItems != null) {
            menuItems.remove(menuItem);
            menuItem.setCategory(null);
        }
    }

    /**
     * Updates the category information.
     * Business method for controlled updates.
     * 
     * @param name         the new name
     * @param description  the new description
     * @param displayOrder the new display order
     * @param imageUrl     the new image URL
     */
    public void updateInfo(String name, String description, Integer displayOrder, String imageUrl) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim().isEmpty() ? null : description.trim();
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl.trim().isEmpty() ? null : imageUrl.trim();
        }
    }

    /**
     * Validates if the category can be deleted.
     * Business rule: Cannot delete category with active menu items.
     * 
     * @return true if the category can be deleted
     */
    public boolean canBeDeleted() {
        return getActiveMenuItemCount() == 0;
    }

    /**
     * Gets a display-friendly name for the category.
     * 
     * @return formatted category name
     */
    public String getDisplayName() {
        return name != null ? name : "Unnamed Category";
    }

    /**
     * Checks if the category has an image.
     * 
     * @return true if the category has an image URL
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    // ================================================================
    // Object Methods
    // ================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Category))
            return false;
        Category category = (Category) o;
        return id != null && id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Category{id=%s, name='%s', active=%s, displayOrder=%d}",
                id, name, active, displayOrder);
    }

    // ================================================================
    // JPA Lifecycle Methods
    // ================================================================

    /**
     * Pre-persist callback to ensure UUID is set.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (active == null) {
            active = true;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
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
    }
}
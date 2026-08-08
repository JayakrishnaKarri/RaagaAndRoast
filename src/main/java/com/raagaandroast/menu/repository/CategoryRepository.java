package com.raagaandroast.menu.repository;

import com.raagaandroast.menu.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Category entity operations.
 * 
 * This repository provides data access methods for category management,
 * including custom queries for business operations and performance
 * optimization.
 * 
 * Design Decisions:
 * - Extends JpaRepository for standard CRUD operations
 * - Custom queries using @Query for complex business logic
 * - Method naming follows Spring Data JPA conventions
 * - Pagination support for list operations
 * - Performance-optimized queries for common access patterns
 * 
 * Performance Considerations:
 * - Queries use indexed fields for optimal performance
 * - Pagination prevents memory issues with large datasets
 * - Ordering by display_order for consistent UI presentation
 * 
 * Interview Points:
 * - Why custom queries? Business logic that can't be expressed with method
 * names
 * - Why pagination? Performance and user experience
 * - Why ordering by display_order? Consistent UI presentation
 * - How to optimize queries? Use indexed fields, avoid N+1 problems
 * 
 * @author RaagaAndRoast Development Team
 */
public interface CategoryRepository extends JpaRepository<Category, UUID> {

        // ================================================================
        // Basic Finder Methods
        // ================================================================

        /**
         * Finds a category by name (case-insensitive).
         * 
         * @param name the category name
         * @return Optional containing the category if found
         */
        @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
        Optional<Category> findByNameIgnoreCase(@Param("name") String name);

        /**
         * Finds all active categories ordered by display order.
         * 
         * @return List of active categories in display order
         */
        @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder ASC, c.name ASC")
        List<Category> findAllActiveOrderByDisplayOrder();

        /**
         * Finds all categories ordered by display order with pagination.
         * 
         * @param pageable pagination information
         * @return Page of categories in display order
         */
        @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC, c.name ASC")
        Page<Category> findAllOrderByDisplayOrder(Pageable pageable);

        /**
         * Finds active categories with pagination.
         * 
         * @param pageable pagination information
         * @return Page of active categories
         */
        @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder ASC, c.name ASC")
        Page<Category> findActiveCategories(Pageable pageable);

        // ================================================================
        // Search and Filter Methods
        // ================================================================

        /**
         * Searches categories by name (partial matching, case-insensitive).
         * 
         * @param name     the name to search for
         * @param pageable pagination information
         * @return Page of categories matching the search criteria
         */
        @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
                        "ORDER BY c.displayOrder ASC, c.name ASC")
        Page<Category> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

        /**
         * Searches categories by name or description (case-insensitive).
         * 
         * @param searchTerm the term to search for
         * @param pageable   pagination information
         * @return Page of categories matching the search criteria
         */
        @Query("SELECT c FROM Category c WHERE " +
                        "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "ORDER BY c.displayOrder ASC, c.name ASC")
        Page<Category> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Finds categories by active status.
         * 
         * @param active   the active status
         * @param pageable pagination information
         * @return Page of categories with the specified active status
         */
        Page<Category> findByActiveOrderByDisplayOrderAscNameAsc(Boolean active, Pageable pageable);

        // ================================================================
        // Business Logic Queries
        // ================================================================

        /**
         * Finds categories with menu items.
         * Uses JOIN to filter categories that have associated menu items.
         * 
         * @param pageable pagination information
         * @return Page of categories that have menu items
         */
        @Query("SELECT DISTINCT c FROM Category c JOIN c.menuItems m WHERE m.available = true " +
                        "ORDER BY c.displayOrder ASC, c.name ASC")
        Page<Category> findCategoriesWithAvailableMenuItems(Pageable pageable);

        /**
         * Finds categories without any menu items.
         * Useful for cleanup and category management.
         * 
         * @param pageable pagination information
         * @return Page of categories without menu items
         */
        @Query("SELECT c FROM Category c WHERE c.menuItems IS EMPTY " +
                        "ORDER BY c.displayOrder ASC, c.name ASC")
        Page<Category> findCategoriesWithoutMenuItems(Pageable pageable);

        /**
         * Finds the next available display order.
         * Useful for adding new categories at the end.
         * 
         * @return the next available display order
         */
        @Query("SELECT COALESCE(MAX(c.displayOrder), 0) + 1 FROM Category c")
        Integer findNextDisplayOrder();

        /**
         * Finds categories with display order greater than the specified value.
         * Used for reordering operations.
         * 
         * @param displayOrder the display order threshold
         * @return List of categories with higher display order
         */
        @Query("SELECT c FROM Category c WHERE c.displayOrder > :displayOrder ORDER BY c.displayOrder ASC")
        List<Category> findByDisplayOrderGreaterThan(@Param("displayOrder") Integer displayOrder);

        // ================================================================
        // Statistical and Reporting Queries
        // ================================================================

        /**
         * Counts active categories.
         * 
         * @return count of active categories
         */
        long countByActiveTrue();

        /**
         * Counts categories with available menu items.
         * 
         * @return count of categories with available menu items
         */
        @Query("SELECT COUNT(DISTINCT c) FROM Category c JOIN c.menuItems m WHERE c.active = true AND m.available = true")
        long countCategoriesWithAvailableMenuItems();

        /**
         * Gets category statistics including menu item counts.
         * Returns category ID, name, and count of available menu items.
         * 
         * @return List of category statistics
         */
        @Query("SELECT c.id, c.name, COUNT(m) FROM Category c " +
                        "LEFT JOIN c.menuItems m ON m.available = true " +
                        "WHERE c.active = true " +
                        "GROUP BY c.id, c.name " +
                        "ORDER BY c.displayOrder ASC")
        List<Object[]> getCategoryStatistics();

        // ================================================================
        // Validation and Business Logic Queries
        // ================================================================

        /**
         * Checks if a category name exists (case-insensitive).
         * 
         * @param name the category name to check
         * @return true if a category with the name exists
         */
        @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
        boolean existsByNameIgnoreCase(@Param("name") String name);

        /**
         * Checks if a category name exists excluding a specific category ID.
         * Useful for update operations to avoid self-conflict.
         * 
         * @param name the category name to check
         * @param id   the category ID to exclude
         * @return true if another category with the name exists
         */
        @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id != :id")
        boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") UUID id);

        /**
         * Checks if a display order is already used.
         * 
         * @param displayOrder the display order to check
         * @return true if the display order is already used
         */
        boolean existsByDisplayOrder(Integer displayOrder);

        /**
         * Checks if a display order is used by another category.
         * 
         * @param displayOrder the display order to check
         * @param id           the category ID to exclude
         * @return true if another category uses the display order
         */
        boolean existsByDisplayOrderAndIdNot(Integer displayOrder, UUID id);

        // ================================================================
        // Performance-Optimized Queries
        // ================================================================

        /**
         * Finds categories with their menu item counts in a single query.
         * Optimized for dashboard and summary views.
         * 
         * @param pageable pagination information
         * @return Page of categories with menu item counts
         */
        @Query("SELECT c, COUNT(m) as menuItemCount FROM Category c " +
                        "LEFT JOIN c.menuItems m ON m.available = true " +
                        "WHERE c.active = true " +
                        "GROUP BY c " +
                        "ORDER BY c.displayOrder ASC")
        Page<Object[]> findActiveCategoriesWithMenuItemCounts(Pageable pageable);

        /**
         * Finds the most popular categories based on menu item count.
         * 
         * @param pageable pagination information
         * @return Page of categories ordered by menu item count
         */
        @Query("SELECT c FROM Category c " +
                        "LEFT JOIN c.menuItems m ON m.available = true " +
                        "WHERE c.active = true " +
                        "GROUP BY c " +
                        "ORDER BY COUNT(m) DESC, c.name ASC")
        Page<Category> findMostPopularCategories(Pageable pageable);

        // ================================================================
        // Bulk Operations Support
        // ================================================================

        /**
         * Finds all categories for bulk operations.
         * Returns minimal data for performance.
         * 
         * @return List of category IDs and names
         */
        @Query("SELECT c.id, c.name FROM Category c ORDER BY c.displayOrder ASC")
        List<Object[]> findAllCategoryIdsAndNames();

        /**
         * Finds categories that can be safely deleted.
         * Categories without menu items or with only inactive menu items.
         * 
         * @return List of deletable categories
         */
        @Query("SELECT c FROM Category c WHERE c.id NOT IN " +
                        "(SELECT DISTINCT m.category.id FROM MenuItem m WHERE m.available = true)")
        List<Category> findDeletableCategories();
}
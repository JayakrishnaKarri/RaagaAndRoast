package com.raagaandroast.menu.repository;

import com.raagaandroast.menu.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for MenuItem entity operations.
 * 
 * This repository provides data access methods for menu item management,
 * including advanced features like JPA Specifications for dynamic filtering,
 * full-text search, and complex business queries.
 * 
 * Design Decisions:
 * - Extends JpaRepository for standard CRUD operations
 * - Extends JpaSpecificationExecutor for dynamic filtering
 * - Custom queries using @Query for complex business logic
 * - Full-text search capabilities for menu item discovery
 * - Performance-optimized queries with proper indexing
 * 
 * Advanced Features:
 * - JPA Specifications for dynamic filtering (price range, dietary preferences)
 * - Full-text search on names and descriptions
 * - Complex aggregation queries for analytics
 * - Performance-optimized queries with JOIN FETCH
 * 
 * Interview Points:
 * - Why JpaSpecificationExecutor? Dynamic filtering without creating dozens of
 * methods
 * - Why full-text search? Better user experience for menu discovery
 * - How to handle BigDecimal in queries? Proper precision and comparison
 * - Why complex aggregation? Business analytics and reporting needs
 * 
 * @author RaagaAndRoast Development Team
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID>, JpaSpecificationExecutor<MenuItem> {

        // ================================================================
        // Performance-Optimized Basic Finders (N+1 Prevention)
        // ================================================================

        /**
         * Finds a menu item by ID with category loaded in single query.
         * Prevents N+1 problem when accessing category information.
         *
         * @param id the menu item ID
         * @return Optional containing the menu item with category if found
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category WHERE m.id = :id")
        Optional<MenuItem> findByIdWithCategory(@Param("id") UUID id);

        /**
         * Finds multiple menu items by IDs with categories loaded.
         * Optimized for bulk operations requiring category information.
         *
         * @param ids the menu item IDs
         * @return List of menu items with categories
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category WHERE m.id IN :ids")
        List<MenuItem> findByIdsWithCategory(@Param("ids") List<UUID> ids);

        // ================================================================
        // Basic Finder Methods
        // ================================================================

        /**
         * Finds a menu item by name within a category (case-insensitive).
         * 
         * @param name       the menu item name
         * @param categoryId the category ID
         * @return Optional containing the menu item if found
         */
        @Query("SELECT m FROM MenuItem m WHERE LOWER(m.name) = LOWER(:name) AND m.category.id = :categoryId")
        Optional<MenuItem> findByNameAndCategoryId(@Param("name") String name, @Param("categoryId") UUID categoryId);

        /**
         * Finds all available menu items ordered by category and display order.
         * 
         * @return List of available menu items
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        List<MenuItem> findAllAvailableOrderedByCategory();

        /**
         * Finds available menu items with pagination.
         * 
         * @param pageable pagination information
         * @return Page of available menu items
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findAvailableMenuItems(Pageable pageable);

        // ================================================================
        // Category-based Queries
        // ================================================================

        /**
         * Finds menu items by category ID.
         * 
         * @param categoryId the category ID
         * @param pageable   pagination information
         * @return Page of menu items in the category
         */
        @Query("SELECT m FROM MenuItem m WHERE m.category.id = :categoryId " +
                        "ORDER BY m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

        /**
         * Finds available menu items by category ID.
         * 
         * @param categoryId the category ID
         * @param pageable   pagination information
         * @return Page of available menu items in the category
         */
        @Query("SELECT m FROM MenuItem m WHERE m.category.id = :categoryId AND m.available = true " +
                        "ORDER BY m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findAvailableByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

        /**
         * Finds menu items by category name (case-insensitive).
         * 
         * @param categoryName the category name
         * @param pageable     pagination information
         * @return Page of menu items in the category
         */
        @Query("SELECT m FROM MenuItem m JOIN m.category c " +
                        "WHERE LOWER(c.name) = LOWER(:categoryName) AND m.available = true AND c.active = true " +
                        "ORDER BY m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findByCategoryNameIgnoreCase(@Param("categoryName") String categoryName, Pageable pageable);

        // ================================================================
        // Search and Filter Methods
        // ================================================================

        /**
         * Advanced search on menu item names and descriptions.
         * Uses LIKE-based search for compatibility across databases.
         *
         * @param searchTerm the search term
         * @param pageable   pagination information
         * @return Page of menu items matching the search
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "AND m.available = true AND c.active = true " +
                        "ORDER BY " +
                        "CASE WHEN LOWER(m.name) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 1 " +
                        "     WHEN LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) THEN 2 " +
                        "     ELSE 3 END, m.name ASC")
        Page<MenuItem> advancedSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Searches menu items by name (partial matching, case-insensitive).
         * 
         * @param name     the name to search for
         * @param pageable pagination information
         * @return Page of menu items matching the search criteria
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
                        "AND m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

        /**
         * Searches menu items by name or description (case-insensitive).
         * 
         * @param searchTerm the term to search for
         * @param pageable   pagination information
         * @return Page of menu items matching the search criteria
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "AND m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

        // ================================================================
        // Price-based Queries
        // ================================================================

        /**
         * Finds menu items within a price range.
         * 
         * @param minPrice the minimum price (inclusive)
         * @param maxPrice the maximum price (inclusive)
         * @param pageable pagination information
         * @return Page of menu items within the price range
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.price BETWEEN :minPrice AND :maxPrice " +
                        "AND m.available = true AND c.active = true " +
                        "ORDER BY m.price ASC, m.name ASC")
        Page<MenuItem> findByPriceBetween(@Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);

        /**
         * Finds menu items below a maximum price.
         * 
         * @param maxPrice the maximum price
         * @param pageable pagination information
         * @return Page of menu items below the price
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.price <= :maxPrice AND m.available = true AND c.active = true " +
                        "ORDER BY m.price ASC, m.name ASC")
        Page<MenuItem> findByPriceLessThanEqual(@Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

        /**
         * Finds the most expensive menu items.
         * 
         * @param pageable pagination information
         * @return Page of menu items ordered by price descending
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.available = true AND c.active = true " +
                        "ORDER BY m.price DESC, m.name ASC")
        Page<MenuItem> findMostExpensive(Pageable pageable);

        /**
         * Finds the least expensive menu items.
         * 
         * @param pageable pagination information
         * @return Page of menu items ordered by price ascending
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.available = true AND c.active = true " +
                        "ORDER BY m.price ASC, m.name ASC")
        Page<MenuItem> findLeastExpensive(Pageable pageable);

        // ================================================================
        // Dietary Preference Queries
        // ================================================================

        /**
         * Finds vegetarian menu items.
         * 
         * @param pageable pagination information
         * @return Page of vegetarian menu items
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.vegetarian = true AND m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findVegetarianItems(Pageable pageable);

        /**
         * Finds vegan menu items.
         * 
         * @param pageable pagination information
         * @return Page of vegan menu items
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.vegan = true AND m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findVeganItems(Pageable pageable);

        /**
         * Finds gluten-free menu items.
         * 
         * @param pageable pagination information
         * @return Page of gluten-free menu items
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.glutenFree = true AND m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findGlutenFreeItems(Pageable pageable);

        /**
         * Finds spicy menu items.
         * 
         * @param pageable pagination information
         * @return Page of spicy menu items
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.spicy = true AND m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findSpicyItems(Pageable pageable);

        /**
         * Finds menu items by multiple dietary preferences.
         * 
         * @param vegetarian vegetarian flag
         * @param vegan      vegan flag
         * @param glutenFree gluten-free flag
         * @param spicy      spicy flag
         * @param pageable   pagination information
         * @return Page of menu items matching dietary preferences
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE (:vegetarian IS NULL OR m.vegetarian = :vegetarian) " +
                        "AND (:vegan IS NULL OR m.vegan = :vegan) " +
                        "AND (:glutenFree IS NULL OR m.glutenFree = :glutenFree) " +
                        "AND (:spicy IS NULL OR m.spicy = :spicy) " +
                        "AND m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findByDietaryPreferences(@Param("vegetarian") Boolean vegetarian,
                        @Param("vegan") Boolean vegan,
                        @Param("glutenFree") Boolean glutenFree,
                        @Param("spicy") Boolean spicy,
                        Pageable pageable);

        // ================================================================
        // Statistical and Reporting Queries
        // ================================================================

        /**
         * Counts available menu items.
         * 
         * @return count of available menu items
         */
        long countByAvailableTrue();

        /**
         * Counts menu items by category.
         * 
         * @param categoryId the category ID
         * @return count of menu items in the category
         */
        long countByCategoryId(UUID categoryId);

        /**
         * Counts available menu items by category.
         * 
         * @param categoryId the category ID
         * @return count of available menu items in the category
         */
        long countByCategoryIdAndAvailableTrue(UUID categoryId);

        /**
         * Gets price statistics for available menu items.
         * Returns min, max, and average prices.
         * 
         * @return Object array with [minPrice, maxPrice, avgPrice]
         */
        @Query("SELECT MIN(m.price), MAX(m.price), AVG(m.price) FROM MenuItem m " +
                        "WHERE m.available = true")
        Object[] getPriceStatistics();

        /**
         * Gets menu item statistics by category.
         * Returns category info with item counts and price ranges.
         * 
         * @return List of category statistics
         */
        @Query("SELECT c.id, c.name, COUNT(m), MIN(m.price), MAX(m.price), AVG(m.price) " +
                        "FROM MenuItem m JOIN m.category c " +
                        "WHERE m.available = true AND c.active = true " +
                        "GROUP BY c.id, c.name " +
                        "ORDER BY c.displayOrder ASC")
        List<Object[]> getMenuItemStatisticsByCategory();

        /**
         * Gets dietary preference statistics.
         * Returns counts for each dietary preference.
         * 
         * @return Object array with [vegetarianCount, veganCount, glutenFreeCount,
         *         spicyCount]
         */
        @Query("SELECT " +
                        "SUM(CASE WHEN m.vegetarian = true THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN m.vegan = true THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN m.glutenFree = true THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN m.spicy = true THEN 1 ELSE 0 END) " +
                        "FROM MenuItem m WHERE m.available = true")
        Object[] getDietaryPreferenceStatistics();

        // ================================================================
        // Validation and Business Logic Queries
        // ================================================================

        /**
         * Checks if a menu item name exists within a category (case-insensitive).
         * 
         * @param name       the menu item name
         * @param categoryId the category ID
         * @return true if a menu item with the name exists in the category
         */
        @Query("SELECT COUNT(m) > 0 FROM MenuItem m " +
                        "WHERE LOWER(m.name) = LOWER(:name) AND m.category.id = :categoryId")
        boolean existsByNameAndCategoryId(@Param("name") String name, @Param("categoryId") UUID categoryId);

        /**
         * Checks if a menu item name exists within a category excluding a specific
         * item.
         * 
         * @param name       the menu item name
         * @param categoryId the category ID
         * @param id         the menu item ID to exclude
         * @return true if another menu item with the name exists in the category
         */
        @Query("SELECT COUNT(m) > 0 FROM MenuItem m " +
                        "WHERE LOWER(m.name) = LOWER(:name) AND m.category.id = :categoryId AND m.id != :id")
        boolean existsByNameAndCategoryIdAndIdNot(@Param("name") String name,
                        @Param("categoryId") UUID categoryId,
                        @Param("id") UUID id);

        // ================================================================
        // Performance-Optimized Queries
        // ================================================================

        /**
         * Finds menu items with category information in a single query.
         * Optimized for menu display with category context.
         * 
         * @param pageable pagination information
         * @return Page of menu items with category information
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.available = true AND c.active = true " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findAvailableMenuItemsWithCategory(Pageable pageable);

        /**
         * Finds featured menu items (items with images and complete information).
         * 
         * @param pageable pagination information
         * @return Page of featured menu items
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.available = true AND c.active = true " +
                        "AND m.imageUrl IS NOT NULL " +
                        "AND m.description IS NOT NULL " +
                        "ORDER BY c.displayOrder ASC, m.displayOrder ASC, m.name ASC")
        Page<MenuItem> findFeaturedMenuItems(Pageable pageable);

        /**
         * Finds quick preparation items (items with preparation time <= specified
         * minutes).
         * 
         * @param maxPreparationTime maximum preparation time in minutes
         * @param pageable           pagination information
         * @return Page of quick preparation menu items
         */
        @Query("SELECT m FROM MenuItem m JOIN FETCH m.category c " +
                        "WHERE m.available = true AND c.active = true " +
                        "AND m.preparationTimeMinutes IS NOT NULL " +
                        "AND m.preparationTimeMinutes <= :maxPreparationTime " +
                        "ORDER BY m.preparationTimeMinutes ASC, m.name ASC")
        Page<MenuItem> findQuickPreparationItems(@Param("maxPreparationTime") Integer maxPreparationTime,
                        Pageable pageable);

        // ================================================================
        // Bulk Operations Support
        // ================================================================

        /**
         * Finds all menu item IDs for bulk operations.
         * 
         * @return List of menu item IDs
         */
        @Query("SELECT m.id FROM MenuItem m")
        List<UUID> findAllMenuItemIds();

        /**
         * Finds menu items that can be safely deleted.
         * Items that are not available and not referenced in orders.
         * 
         * @return List of deletable menu items
         */
        @Query("SELECT m FROM MenuItem m WHERE m.available = false")
        List<MenuItem> findDeletableMenuItems();
}
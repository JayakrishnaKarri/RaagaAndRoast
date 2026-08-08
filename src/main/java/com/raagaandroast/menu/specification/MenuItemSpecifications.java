package com.raagaandroast.menu.specification;

import com.raagaandroast.menu.entity.Category;
import com.raagaandroast.menu.entity.MenuItem;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications for dynamic filtering of MenuItem entities.
 * 
 * This class demonstrates advanced JPA Criteria API usage for building
 * dynamic queries without creating dozens of repository methods for
 * every possible combination of filters.
 * 
 * Design Decisions:
 * - Static factory methods for creating specifications
 * - Composable specifications using and(), or(), not()
 * - Type-safe query building with Criteria API
 * - Performance-optimized with proper JOIN strategies
 * - Null-safe parameter handling
 * 
 * Advanced Features:
 * - Dynamic filtering based on multiple criteria
 * - Price range filtering with BigDecimal precision
 * - Dietary preference combinations
 * - Category-based filtering with JOIN
 * - Text search with case-insensitive matching
 * 
 * Interview Points:
 * - Why Specifications over custom repository methods? Composability and
 * flexibility
 * - How does Criteria API work? Type-safe query building at runtime
 * - Why static factory methods? Clean API and method chaining
 * - How to handle performance? Proper JOINs and indexed fields
 * - When to use Specifications vs @Query? Complex dynamic filtering vs fixed
 * queries
 * 
 * Usage Examples:
 * ```java
 * // Simple filter
 * Specification<MenuItem> spec = MenuItemSpecifications.isAvailable();
 * 
 * // Complex filter combination
 * Specification<MenuItem> complexSpec = MenuItemSpecifications.isAvailable()
 * .and(MenuItemSpecifications.inCategory(categoryId))
 * .and(MenuItemSpecifications.priceBetween(minPrice, maxPrice))
 * .and(MenuItemSpecifications.isVegetarian());
 * 
 * // Use with repository
 * Page<MenuItem> results = menuItemRepository.findAll(complexSpec, pageable);
 * ```
 * 
 * @author RaagaAndRoast Development Team
 */
public class MenuItemSpecifications {

    // ================================================================
    // Basic Availability and Status Specifications
    // ================================================================

    /**
     * Specification for available menu items.
     * 
     * @return Specification that filters for available items
     */
    public static Specification<MenuItem> isAvailable() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("available"));
    }

    /**
     * Specification for unavailable menu items.
     * 
     * @return Specification that filters for unavailable items
     */
    public static Specification<MenuItem> isUnavailable() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("available"));
    }

    /**
     * Specification for menu items in active categories.
     * 
     * @return Specification that filters for items in active categories
     */
    public static Specification<MenuItem> inActiveCategory() {
        return (root, query, criteriaBuilder) -> {
            Join<MenuItem, Category> categoryJoin = root.join("category", JoinType.INNER);
            return criteriaBuilder.isTrue(categoryJoin.get("active"));
        };
    }

    /**
     * Specification for available menu items in active categories.
     * This is the most common filter combination.
     * 
     * @return Specification for available items in active categories
     */
    public static Specification<MenuItem> isAvailableInActiveCategory() {
        return isAvailable().and(inActiveCategory());
    }

    // ================================================================
    // Category-based Specifications
    // ================================================================

    /**
     * Specification for menu items in a specific category.
     * 
     * @param categoryId the category ID
     * @return Specification that filters by category
     */
    public static Specification<MenuItem> inCategory(UUID categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction(); // Always true
            }
            Join<MenuItem, Category> categoryJoin = root.join("category", JoinType.INNER);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    /**
     * Specification for menu items in categories with specific names.
     * 
     * @param categoryNames list of category names (case-insensitive)
     * @return Specification that filters by category names
     */
    public static Specification<MenuItem> inCategoriesNamed(List<String> categoryNames) {
        return (root, query, criteriaBuilder) -> {
            if (categoryNames == null || categoryNames.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            Join<MenuItem, Category> categoryJoin = root.join("category", JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();

            for (String categoryName : categoryNames) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(categoryJoin.get("name")),
                        categoryName.toLowerCase()));
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    // ================================================================
    // Price-based Specifications
    // ================================================================

    /**
     * Specification for menu items within a price range.
     * 
     * @param minPrice minimum price (inclusive), null for no minimum
     * @param maxPrice maximum price (inclusive), null for no maximum
     * @return Specification that filters by price range
     */
    public static Specification<MenuItem> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicates.isEmpty() ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification for menu items below a maximum price.
     * 
     * @param maxPrice maximum price (inclusive)
     * @return Specification that filters by maximum price
     */
    public static Specification<MenuItem> priceBelow(BigDecimal maxPrice) {
        return priceBetween(null, maxPrice);
    }

    /**
     * Specification for menu items above a minimum price.
     * 
     * @param minPrice minimum price (inclusive)
     * @return Specification that filters by minimum price
     */
    public static Specification<MenuItem> priceAbove(BigDecimal minPrice) {
        return priceBetween(minPrice, null);
    }

    // ================================================================
    // Dietary Preference Specifications
    // ================================================================

    /**
     * Specification for vegetarian menu items.
     * 
     * @return Specification that filters for vegetarian items
     */
    public static Specification<MenuItem> isVegetarian() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("vegetarian"));
    }

    /**
     * Specification for vegan menu items.
     * 
     * @return Specification that filters for vegan items
     */
    public static Specification<MenuItem> isVegan() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("vegan"));
    }

    /**
     * Specification for gluten-free menu items.
     * 
     * @return Specification that filters for gluten-free items
     */
    public static Specification<MenuItem> isGlutenFree() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("glutenFree"));
    }

    /**
     * Specification for spicy menu items.
     * 
     * @return Specification that filters for spicy items
     */
    public static Specification<MenuItem> isSpicy() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("spicy"));
    }

    /**
     * Specification for non-spicy menu items.
     * 
     * @return Specification that filters for non-spicy items
     */
    public static Specification<MenuItem> isNotSpicy() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("spicy"));
    }

    /**
     * Specification for menu items matching dietary preferences.
     * Only applies filters for non-null boolean values.
     * 
     * @param vegetarian vegetarian preference (null to ignore)
     * @param vegan      vegan preference (null to ignore)
     * @param glutenFree gluten-free preference (null to ignore)
     * @param spicy      spicy preference (null to ignore)
     * @return Specification that filters by dietary preferences
     */
    public static Specification<MenuItem> matchesDietaryPreferences(Boolean vegetarian,
            Boolean vegan,
            Boolean glutenFree,
            Boolean spicy) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (vegetarian != null) {
                predicates.add(criteriaBuilder.equal(root.get("vegetarian"), vegetarian));
            }

            if (vegan != null) {
                predicates.add(criteriaBuilder.equal(root.get("vegan"), vegan));
            }

            if (glutenFree != null) {
                predicates.add(criteriaBuilder.equal(root.get("glutenFree"), glutenFree));
            }

            if (spicy != null) {
                predicates.add(criteriaBuilder.equal(root.get("spicy"), spicy));
            }

            return predicates.isEmpty() ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ================================================================
    // Text Search Specifications
    // ================================================================

    /**
     * Specification for menu items with names containing the search term.
     * Case-insensitive search.
     * 
     * @param searchTerm the search term
     * @return Specification that filters by name
     */
    public static Specification<MenuItem> nameContains(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    pattern);
        };
    }

    /**
     * Specification for menu items with descriptions containing the search term.
     * Case-insensitive search.
     * 
     * @param searchTerm the search term
     * @return Specification that filters by description
     */
    public static Specification<MenuItem> descriptionContains(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    pattern);
        };
    }

    /**
     * Specification for menu items with names or descriptions containing the search
     * term.
     * Case-insensitive search across both fields.
     * 
     * @param searchTerm the search term
     * @return Specification that filters by name or description
     */
    public static Specification<MenuItem> nameOrDescriptionContains(String searchTerm) {
        return nameContains(searchTerm).or(descriptionContains(searchTerm));
    }

    // ================================================================
    // Preparation Time and Calorie Specifications
    // ================================================================

    /**
     * Specification for menu items with preparation time within a range.
     * 
     * @param maxMinutes maximum preparation time in minutes
     * @return Specification that filters by preparation time
     */
    public static Specification<MenuItem> preparationTimeBelow(Integer maxMinutes) {
        return (root, query, criteriaBuilder) -> {
            if (maxMinutes == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("preparationTimeMinutes")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("preparationTimeMinutes"), maxMinutes));
        };
    }

    /**
     * Specification for menu items with calorie count within a range.
     * 
     * @param maxCalories maximum calorie count
     * @return Specification that filters by calorie count
     */
    public static Specification<MenuItem> caloriesBelow(Integer maxCalories) {
        return (root, query, criteriaBuilder) -> {
            if (maxCalories == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("calories")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("calories"), maxCalories));
        };
    }

    /**
     * Specification for menu items with calorie information available.
     * 
     * @return Specification that filters for items with calorie data
     */
    public static Specification<MenuItem> hasCalorieInfo() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get("calories"));
    }

    // ================================================================
    // Complex Composite Specifications
    // ================================================================

    /**
     * Specification for healthy menu items.
     * Defines healthy as: vegetarian OR vegan, AND calories < 500 (if available).
     * 
     * @return Specification for healthy menu items
     */
    public static Specification<MenuItem> isHealthy() {
        return (root, query, criteriaBuilder) -> {
            // Vegetarian or vegan
            Predicate dietaryPredicate = criteriaBuilder.or(
                    criteriaBuilder.isTrue(root.get("vegetarian")),
                    criteriaBuilder.isTrue(root.get("vegan")));

            // Low calorie (if calorie info is available)
            Predicate caloriePredicate = criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("calories")),
                    criteriaBuilder.lessThan(root.get("calories"), 500));

            return criteriaBuilder.and(dietaryPredicate, caloriePredicate);
        };
    }

    /**
     * Specification for quick and easy menu items.
     * Defines quick as: preparation time <= 5 minutes AND available.
     * 
     * @return Specification for quick menu items
     */
    public static Specification<MenuItem> isQuickAndEasy() {
        return isAvailable()
                .and(preparationTimeBelow(5));
    }

    /**
     * Specification for premium menu items.
     * Defines premium as: price > average price AND has image AND has description.
     * Note: This is a simplified version; in practice, you'd calculate the average
     * price dynamically.
     * 
     * @param averagePrice the average price threshold
     * @return Specification for premium menu items
     */
    public static Specification<MenuItem> isPremium(BigDecimal averagePrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Above average price
            if (averagePrice != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("price"), averagePrice));
            }

            // Has image
            predicates.add(criteriaBuilder.isNotNull(root.get("imageUrl")));

            // Has description
            predicates.add(criteriaBuilder.isNotNull(root.get("description")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ================================================================
    // Utility Methods for Complex Filtering
    // ================================================================

    /**
     * Creates a specification that matches any of the provided specifications.
     * Useful for OR operations across multiple criteria.
     * 
     * @param specifications the specifications to combine with OR
     * @return Combined specification using OR logic
     */
    @SafeVarargs
    public static Specification<MenuItem> anyOf(Specification<MenuItem>... specifications) {
        return (root, query, criteriaBuilder) -> {
            if (specifications == null || specifications.length == 0) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            for (Specification<MenuItem> spec : specifications) {
                if (spec != null) {
                    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }

            return predicates.isEmpty() ? criteriaBuilder.conjunction()
                    : criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Creates a specification that matches all of the provided specifications.
     * Useful for AND operations across multiple criteria.
     * 
     * @param specifications the specifications to combine with AND
     * @return Combined specification using AND logic
     */
    @SafeVarargs
    public static Specification<MenuItem> allOf(Specification<MenuItem>... specifications) {
        return (root, query, criteriaBuilder) -> {
            if (specifications == null || specifications.length == 0) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            for (Specification<MenuItem> spec : specifications) {
                if (spec != null) {
                    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }

            return predicates.isEmpty() ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
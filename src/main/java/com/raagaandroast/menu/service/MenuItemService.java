package com.raagaandroast.menu.service;

import com.raagaandroast.common.exception.*;
import com.raagaandroast.menu.dto.MenuItemRequest;
import com.raagaandroast.menu.dto.MenuItemResponse;
import com.raagaandroast.menu.entity.Category;
import com.raagaandroast.menu.entity.MenuItem;
import com.raagaandroast.menu.mapper.MenuItemMapper;
import com.raagaandroast.menu.repository.CategoryRepository;
import com.raagaandroast.menu.repository.MenuItemRepository;
import com.raagaandroast.menu.specification.MenuItemSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service layer for MenuItem management.
 * 
 * This service demonstrates:
 * - Advanced JPA Specifications for dynamic filtering
 * - BigDecimal handling for monetary values
 * - Complex business logic with dietary preferences
 * - Performance optimization with JOIN FETCH
 * - Proper transaction boundaries
 * - Comprehensive validation and error handling
 * - Integration with CategoryService for relationship management
 * 
 * Key Learning Points:
 * - Use Specifications for complex, composable queries
 * - Handle BigDecimal precision carefully for money
 * - Apply business rules consistently across operations
 * - Use @Transactional appropriately for data consistency
 * - Validate entity relationships before persistence
 * - Log important business events for monitoring
 * 
 * @author RaagaAndRoast Development Team
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemMapper menuItemMapper;

    /**
     * Creates a new menu item.
     * 
     * @param request the menu item creation request
     * @return the created menu item response
     * @throws ResourceNotFoundException if category not found
     * @throws IllegalArgumentException  if request is invalid
     */
    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        log.info("Creating new menu item with name: {} in category: {}",
                request.name(), request.categoryId());

        // Validate request
        if (!menuItemMapper.isValidForCreation(request)) {
            throw MenuItemRequestValidationException.invalidCreationRequest();
        }

        // Validate category exists and is active (no optimization needed for single
        // lookup)
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with ID: " + request.categoryId()));

        if (!category.isActive()) {
            throw MenuItemRequestValidationException.inactiveCategoryAssignment();
        }

        // Create and save entity
        MenuItem menuItem = menuItemMapper.toEntityWithCategory(request, category);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        log.info("Successfully created menu item with ID: {} and name: {}",
                savedMenuItem.getId(), savedMenuItem.getName());

        return menuItemMapper.toResponse(savedMenuItem);
    }

    /**
     * Retrieves a menu item by ID.
     * 
     * @param id the menu item ID
     * @return the menu item response
     * @throws ResourceNotFoundException if menu item not found
     */
    public MenuItemResponse getMenuItemById(UUID id) {
        log.debug("Retrieving menu item with ID: {}", id);

        // Use optimized query to load menu item with category
        MenuItem menuItem = menuItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + id));

        return menuItemMapper.toResponse(menuItem);
    }

    /**
     * Retrieves all menu items with pagination.
     * 
     * @param pageable pagination information
     * @return page of menu item responses
     */
    public Page<MenuItemResponse> getAllMenuItems(Pageable pageable) {
        log.debug("Retrieving all menu items with pagination: {}", pageable);

        Page<MenuItem> menuItems = menuItemRepository.findAvailableMenuItemsWithCategory(pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Retrieves all available menu items with pagination.
     * 
     * @param pageable pagination information
     * @return page of available menu item responses
     */
    public Page<MenuItemResponse> getAvailableMenuItems(Pageable pageable) {
        log.debug("Retrieving available menu items with pagination: {}", pageable);

        Specification<MenuItem> spec = MenuItemSpecifications.isAvailable();
        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Retrieves menu items by category with pagination.
     * 
     * @param categoryId the category ID
     * @param pageable   pagination information
     * @return page of menu item responses
     * @throws ResourceNotFoundException if category not found
     */
    public Page<MenuItemResponse> getMenuItemsByCategory(UUID categoryId, Pageable pageable) {
        log.debug("Retrieving menu items for category: {} with pagination: {}", categoryId, pageable);

        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
        }

        Specification<MenuItem> spec = MenuItemSpecifications.inCategory(categoryId);
        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Searches menu items with advanced filtering.
     * 
     * @param name          optional name filter
     * @param categoryId    optional category filter
     * @param minPrice      optional minimum price filter
     * @param maxPrice      optional maximum price filter
     * @param vegetarian    optional vegetarian filter
     * @param vegan         optional vegan filter
     * @param glutenFree    optional gluten-free filter
     * @param availableOnly whether to include only available items
     * @param pageable      pagination information
     * @return page of filtered menu item responses
     */
    public Page<MenuItemResponse> searchMenuItems(String name, UUID categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Boolean vegetarian, Boolean vegan, Boolean glutenFree,
            Boolean availableOnly, Pageable pageable) {

        log.debug("Searching menu items with filters - name: {}, category: {}, price: {}-{}, " +
                "vegetarian: {}, vegan: {}, glutenFree: {}, availableOnly: {}",
                name, categoryId, minPrice, maxPrice, vegetarian, vegan, glutenFree, availableOnly);

        // Build dynamic specification for sophisticated filtering
        Specification<MenuItem> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        // Add name filter if provided
        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and(MenuItemSpecifications.nameContains(name.trim()));
        }

        // Add category filter if provided
        if (categoryId != null) {
            spec = spec.and(MenuItemSpecifications.inCategory(categoryId));
        }

        // Add price range filters
        if (minPrice != null && maxPrice != null) {
            spec = spec.and(MenuItemSpecifications.priceBetween(minPrice, maxPrice));
        } else if (minPrice != null) {
            spec = spec.and(MenuItemSpecifications.priceAbove(minPrice));
        } else if (maxPrice != null) {
            spec = spec.and(MenuItemSpecifications.priceBelow(maxPrice));
        }

        // Add dietary preference filters
        if (Boolean.TRUE.equals(vegetarian)) {
            spec = spec.and(MenuItemSpecifications.isVegetarian());
        }

        if (Boolean.TRUE.equals(vegan)) {
            spec = spec.and(MenuItemSpecifications.isVegan());
        }

        if (Boolean.TRUE.equals(glutenFree)) {
            spec = spec.and(MenuItemSpecifications.isGlutenFree());
        }

        // Add availability filter if requested
        if (Boolean.TRUE.equals(availableOnly)) {
            spec = spec.and(MenuItemSpecifications.isAvailable());
        }

        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Updates an existing menu item.
     * 
     * @param id      the menu item ID
     * @param request the update request
     * @return the updated menu item response
     * @throws ResourceNotFoundException if menu item or category not found
     */
    @Transactional
    public MenuItemResponse updateMenuItem(UUID id, MenuItemRequest request) {
        log.info("Updating menu item with ID: {}", id);

        // Validate request
        if (!menuItemMapper.isValidForUpdate(request)) {
            throw MenuItemRequestValidationException.invalidUpdateRequest();
        }

        // Find existing menu item with category loaded
        MenuItem existingMenuItem = menuItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + id));

        // Validate category if it's being changed
        if (!request.categoryId().equals(existingMenuItem.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with ID: " + request.categoryId()));

            if (!newCategory.isActive()) {
                throw MenuItemRequestValidationException.moveToInactiveCategory();
            }

            existingMenuItem.setCategory(newCategory);
        }

        // Update entity
        MenuItem updatedMenuItem = menuItemMapper.updateEntity(existingMenuItem, request);
        MenuItem savedMenuItem = menuItemRepository.save(updatedMenuItem);

        log.info("Successfully updated menu item with ID: {}", savedMenuItem.getId());

        return menuItemMapper.toResponse(savedMenuItem);
    }

    /**
     * Makes a menu item available.
     * 
     * @param id the menu item ID
     * @return the updated menu item response
     * @throws ResourceNotFoundException if menu item not found
     */
    @Transactional
    public MenuItemResponse makeAvailable(UUID id) {
        log.info("Making menu item available with ID: {}", id);

        MenuItem menuItem = menuItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + id));

        menuItem.makeAvailable();
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        log.info("Successfully made menu item available with ID: {}", savedMenuItem.getId());

        return menuItemMapper.toResponse(savedMenuItem);
    }

    /**
     * Makes a menu item unavailable.
     * 
     * @param id the menu item ID
     * @return the updated menu item response
     * @throws ResourceNotFoundException if menu item not found
     */
    @Transactional
    public MenuItemResponse makeUnavailable(UUID id) {
        log.info("Making menu item unavailable with ID: {}", id);

        MenuItem menuItem = menuItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + id));

        menuItem.makeUnavailable();
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        log.info("Successfully made menu item unavailable with ID: {}", savedMenuItem.getId());

        return menuItemMapper.toResponse(savedMenuItem);
    }

    /**
     * Updates the price of a menu item.
     * 
     * @param id       the menu item ID
     * @param newPrice the new price
     * @return the updated menu item response
     * @throws ResourceNotFoundException if menu item not found
     * @throws IllegalArgumentException  if price is invalid
     */
    @Transactional
    public MenuItemResponse updatePrice(UUID id, BigDecimal newPrice) {
        log.info("Updating price for menu item with ID: {} to: {}", id, newPrice);

        MenuItem menuItem = menuItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + id));

        menuItem.updatePrice(newPrice);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        log.info("Successfully updated price for menu item with ID: {}", savedMenuItem.getId());

        return menuItemMapper.toResponse(savedMenuItem);
    }

    /**
     * Deletes a menu item.
     * Note: This is a hard delete. Consider implementing soft delete for
     * production.
     * 
     * @param id the menu item ID
     * @throws ResourceNotFoundException if menu item not found
     */
    @Transactional
    public void deleteMenuItem(UUID id) {
        log.info("Deleting menu item with ID: {}", id);

        MenuItem menuItem = menuItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + id));

        // Note: In production, check if item is referenced in orders/carts before
        // deletion
        // For now, we allow deletion for development purposes

        menuItemRepository.delete(menuItem);

        log.info("Successfully deleted menu item with ID: {}", id);
    }

    /**
     * Checks if a menu item exists by ID.
     * 
     * @param id the menu item ID
     * @return true if menu item exists
     */
    public boolean existsById(UUID id) {
        return menuItemRepository.existsById(id);
    }

    /**
     * Gets menu items by dietary preferences.
     * 
     * @param vegetarian include vegetarian items
     * @param vegan      include vegan items
     * @param glutenFree include gluten-free items
     * @param pageable   pagination information
     * @return page of menu items matching dietary preferences
     */
    public Page<MenuItemResponse> getMenuItemsByDietaryPreferences(Boolean vegetarian, Boolean vegan,
            Boolean glutenFree, Pageable pageable) {
        log.debug("Retrieving menu items by dietary preferences - vegetarian: {}, vegan: {}, glutenFree: {}",
                vegetarian, vegan, glutenFree);

        Specification<MenuItem> spec = MenuItemSpecifications.isAvailable();

        if (Boolean.TRUE.equals(vegetarian)) {
            spec = spec.and(MenuItemSpecifications.isVegetarian());
        }

        if (Boolean.TRUE.equals(vegan)) {
            spec = spec.and(MenuItemSpecifications.isVegan());
        }

        if (Boolean.TRUE.equals(glutenFree)) {
            spec = spec.and(MenuItemSpecifications.isGlutenFree());
        }

        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Gets menu items within a price range.
     * 
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     * @param pageable pagination information
     * @return page of menu items within price range
     */
    public Page<MenuItemResponse> getMenuItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {
        log.debug("Retrieving menu items by price range: {} - {}", minPrice, maxPrice);

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw MenuItemRequestValidationException.invalidPriceRange();
        }

        Specification<MenuItem> spec = MenuItemSpecifications.isAvailable()
                .and(MenuItemSpecifications.priceBetween(minPrice, maxPrice));

        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Gets the most popular menu items (placeholder for future analytics).
     * 
     * @param pageable pagination information
     * @return page of popular menu items
     */
    public Page<MenuItemResponse> getPopularMenuItems(Pageable pageable) {
        log.debug("Retrieving popular menu items with pagination: {}", pageable);

        // For now, return available items ordered by name
        // Note: In production, implement popularity logic based on order history and
        // analytics
        Specification<MenuItem> spec = MenuItemSpecifications.isAvailable();
        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Get menu items with comprehensive filtering.
     *
     * @param categoryId    Category filter
     * @param minPrice      Minimum price filter
     * @param maxPrice      Maximum price filter
     * @param vegetarian    Vegetarian filter
     * @param vegan         Vegan filter
     * @param glutenFree    Gluten-free filter
     * @param availableOnly Available only filter
     * @param name          Name search filter
     * @param pageable      Pagination parameters
     * @return Filtered menu items
     */
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsWithFilters(
            UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean vegetarian, Boolean vegan, Boolean glutenFree,
            Boolean availableOnly, String name, Pageable pageable) {

        log.debug("Getting menu items with comprehensive filters");

        // For now, use the existing method with dietary preferences
        Page<MenuItem> menuItems = menuItemRepository.findByDietaryPreferences(
                vegetarian, vegan, glutenFree, availableOnly, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Get menu items by category with availability filter.
     *
     * @param categoryId    Category ID
     * @param availableOnly Show only available items
     * @param pageable      Pagination parameters
     * @return Menu items for category
     */
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsByCategory(UUID categoryId, Boolean availableOnly, Pageable pageable) {
        log.debug("Getting menu items for category: {}, availableOnly: {}", categoryId, availableOnly);

        Page<MenuItem> menuItems = menuItemRepository.findByCategoryId(categoryId, pageable);

        // Filter by availability if needed
        if (Boolean.TRUE.equals(availableOnly)) {
            // Use Specification for availability filtering
            Specification<MenuItem> spec = MenuItemSpecifications.inCategory(categoryId)
                    .and(MenuItemSpecifications.isAvailable());
            menuItems = menuItemRepository.findAll(spec, pageable);
        }

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Toggle menu item availability.
     *
     * @param id Menu item ID
     * @return Updated menu item
     */
    @Transactional
    public MenuItemResponse toggleAvailability(UUID id) {
        log.info("Toggling availability for menu item: {}", id);

        MenuItem menuItem = menuItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        menuItem.setAvailable(!menuItem.isAvailable());
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        log.info("Menu item availability toggled: {} -> available: {}", id, savedMenuItem.isAvailable());
        return menuItemMapper.toResponse(savedMenuItem);
    }

    /**
     * Search menu items by name with availability filter.
     *
     * @param query         Search query
     * @param availableOnly Show only available items
     * @param pageable      Pagination parameters
     * @return Search results
     */
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> searchMenuItemsByName(String query, Boolean availableOnly, Pageable pageable) {
        log.debug("Searching menu items by name: {}, availableOnly: {}", query, availableOnly);

        Specification<MenuItem> spec = MenuItemSpecifications.nameContains(query);

        if (Boolean.TRUE.equals(availableOnly)) {
            spec = spec.and(MenuItemSpecifications.isAvailable());
        }

        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Get menu items by dietary preferences with availability filter.
     *
     * @param vegetarian    Vegetarian filter
     * @param vegan         Vegan filter
     * @param glutenFree    Gluten-free filter
     * @param availableOnly Show only available items
     * @param pageable      Pagination parameters
     * @return Filtered menu items
     */
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsByDietaryPreferences(
            Boolean vegetarian, Boolean vegan, Boolean glutenFree, Boolean availableOnly, Pageable pageable) {

        log.debug(
                "Getting menu items by dietary preferences - vegetarian: {}, vegan: {}, glutenFree: {}, available: {}",
                vegetarian, vegan, glutenFree, availableOnly);

        Page<MenuItem> menuItems = menuItemRepository.findByDietaryPreferences(
                vegetarian, vegan, glutenFree, availableOnly, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }

    /**
     * Get menu items by price range with availability filter.
     *
     * @param minPrice      Minimum price
     * @param maxPrice      Maximum price
     * @param availableOnly Show only available items
     * @param pageable      Pagination parameters
     * @return Price-filtered menu items
     */
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsByPriceRange(
            BigDecimal minPrice, BigDecimal maxPrice, Boolean availableOnly, Pageable pageable) {

        log.debug("Getting menu items by price range: {} - {}, available: {}", minPrice, maxPrice, availableOnly);

        Specification<MenuItem> spec = MenuItemSpecifications.priceBetween(minPrice, maxPrice);

        if (Boolean.TRUE.equals(availableOnly)) {
            spec = spec.and(MenuItemSpecifications.isAvailable());
        }

        Page<MenuItem> menuItems = menuItemRepository.findAll(spec, pageable);

        return menuItems.map(menuItemMapper::toResponse);
    }
}
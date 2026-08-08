package com.raagaandroast.menu.controller;

import com.raagaandroast.menu.dto.MenuItemRequest;
import com.raagaandroast.menu.dto.MenuItemResponse;
import com.raagaandroast.menu.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST Controller for MenuItem management operations.
 * 
 * Provides comprehensive endpoints for menu item CRUD operations with advanced
 * features:
 * - Dynamic filtering by price, dietary preferences, category, availability
 * - Search functionality with pagination and sorting
 * - Proper authorization based on roles and permissions
 * - BigDecimal handling for monetary values
 * - Comprehensive validation and error handling
 * 
 * Authorization Model:
 * - READ operations: Available to all authenticated users
 * - WRITE operations: Restricted to MANAGER and ADMIN roles
 * - Advanced filtering: Demonstrates JPA Specifications usage
 * 
 * @author RaagaAndRoast Development Team
 */
@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Menu Items", description = "Menu item management operations")
@SecurityRequirement(name = "bearerAuth")
public class MenuItemController {

    private final MenuItemService menuItemService;

    /**
     * Create a new menu item.
     * 
     * Only users with MANAGER or ADMIN roles can create menu items.
     * Demonstrates BigDecimal validation and business rule enforcement.
     * 
     * @param request Menu item creation request with comprehensive validation
     * @return Created menu item response with 201 status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or hasAuthority('MENU_WRITE')")
    @Operation(summary = "Create new menu item", description = "Creates a new menu item with BigDecimal pricing and dietary preferences. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Menu item name already exists in category")
    })
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @Valid @RequestBody MenuItemRequest request) {

        log.info("Creating new menu item: {} in category: {}", request.name(), request.categoryId());
        MenuItemResponse response = menuItemService.createMenuItem(request);
        log.info("Menu item created successfully with ID: {}", response.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all menu items with advanced filtering and pagination.
     * 
     * Demonstrates JPA Specifications for dynamic filtering.
     * Available to all authenticated users.
     * 
     * @param categoryId    Optional category filter
     * @param minPrice      Optional minimum price filter (BigDecimal)
     * @param maxPrice      Optional maximum price filter (BigDecimal)
     * @param vegetarian    Optional vegetarian filter
     * @param vegan         Optional vegan filter
     * @param glutenFree    Optional gluten-free filter
     * @param availableOnly Optional availability filter
     * @param name          Optional name search term
     * @param pageable      Pagination and sorting parameters
     * @return Paginated and filtered menu items
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get menu items with filtering", description = "Retrieves menu items with advanced filtering, pagination, and sorting capabilities")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu items retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<MenuItemResponse>> getMenuItems(
            @RequestParam(required = false) @Parameter(description = "Filter by category ID") UUID categoryId,

            @RequestParam(required = false) @Parameter(description = "Minimum price filter", example = "5.99") BigDecimal minPrice,

            @RequestParam(required = false) @Parameter(description = "Maximum price filter", example = "25.99") BigDecimal maxPrice,

            @RequestParam(required = false) @Parameter(description = "Filter vegetarian items") Boolean vegetarian,

            @RequestParam(required = false) @Parameter(description = "Filter vegan items") Boolean vegan,

            @RequestParam(required = false) @Parameter(description = "Filter gluten-free items") Boolean glutenFree,

            @RequestParam(required = false, defaultValue = "false") @Parameter(description = "Show only available items") Boolean availableOnly,

            @RequestParam(required = false) @Parameter(description = "Search by name") String name,

            @PageableDefault(size = 20, sort = "name") @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {

        log.debug(
                "Fetching menu items with filters - category: {}, price: {}-{}, vegetarian: {}, vegan: {}, glutenFree: {}, available: {}, name: {}",
                categoryId, minPrice, maxPrice, vegetarian, vegan, glutenFree, availableOnly, name);

        Page<MenuItemResponse> menuItems = menuItemService.getMenuItemsWithFilters(
                categoryId, minPrice, maxPrice, vegetarian, vegan, glutenFree, availableOnly, name, pageable);

        return ResponseEntity.ok(menuItems);
    }

    /**
     * Get menu items by category.
     * 
     * Optimized endpoint for category-specific menu browsing.
     * Available to all authenticated users.
     * 
     * @param categoryId    Category UUID
     * @param availableOnly Show only available items
     * @param pageable      Pagination parameters
     * @return Paginated menu items for the category
     */
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get menu items by category", description = "Retrieves menu items for a specific category with optional availability filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu items retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Page<MenuItemResponse>> getMenuItemsByCategory(
            @PathVariable @Parameter(description = "Category ID") UUID categoryId,

            @RequestParam(required = false, defaultValue = "false") @Parameter(description = "Show only available items") Boolean availableOnly,

            @PageableDefault(size = 20, sort = "name") @Parameter(description = "Pagination parameters") Pageable pageable) {

        log.debug("Fetching menu items for category: {}, availableOnly: {}", categoryId, availableOnly);
        Page<MenuItemResponse> menuItems = menuItemService.getMenuItemsByCategory(categoryId, availableOnly, pageable);

        return ResponseEntity.ok(menuItems);
    }

    /**
     * Get menu item by ID.
     * 
     * Available to all authenticated users.
     * 
     * @param id Menu item UUID
     * @return Menu item details
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get menu item by ID", description = "Retrieves a specific menu item by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    public ResponseEntity<MenuItemResponse> getMenuItemById(
            @PathVariable @Parameter(description = "Menu item ID") UUID id) {

        log.debug("Fetching menu item with ID: {}", id);
        MenuItemResponse menuItem = menuItemService.getMenuItemById(id);

        return ResponseEntity.ok(menuItem);
    }

    /**
     * Update an existing menu item.
     * 
     * Only users with MANAGER or ADMIN roles can update menu items.
     * Demonstrates optimistic locking and BigDecimal handling.
     * 
     * @param id      Menu item UUID to update
     * @param request Updated menu item data
     * @return Updated menu item response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or hasAuthority('MENU_WRITE')")
    @Operation(summary = "Update menu item", description = "Updates an existing menu item. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Menu item or category not found"),
            @ApiResponse(responseCode = "409", description = "Optimistic locking conflict or name already exists")
    })
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable @Parameter(description = "Menu item ID") UUID id,
            @Valid @RequestBody MenuItemRequest request) {

        log.info("Updating menu item with ID: {}", id);
        MenuItemResponse response = menuItemService.updateMenuItem(id, request);
        log.info("Menu item updated successfully: {}", response.id());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a menu item.
     * 
     * Only users with MANAGER or ADMIN roles can delete menu items.
     * 
     * @param id Menu item UUID to delete
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or hasAuthority('MENU_DELETE')")
    @Operation(summary = "Delete menu item", description = "Deletes a menu item. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Menu item deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Menu item not found"),
            @ApiResponse(responseCode = "409", description = "Menu item is referenced in active orders")
    })
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable @Parameter(description = "Menu item ID") UUID id) {

        log.info("Deleting menu item with ID: {}", id);
        menuItemService.deleteMenuItem(id);
        log.info("Menu item deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle menu item availability.
     * 
     * Allows managers to quickly enable/disable items without full updates.
     * Only users with MANAGER or ADMIN roles can toggle availability.
     * 
     * @param id Menu item UUID
     * @return Updated menu item response
     */
    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or hasAuthority('MENU_WRITE')")
    @Operation(summary = "Toggle menu item availability", description = "Toggles the availability status of a menu item. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item availability toggled successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    public ResponseEntity<MenuItemResponse> toggleMenuItemAvailability(
            @PathVariable @Parameter(description = "Menu item ID") UUID id) {

        log.info("Toggling availability for menu item with ID: {}", id);
        MenuItemResponse response = menuItemService.toggleAvailability(id);
        log.info("Menu item availability toggled successfully: {} -> available: {}",
                response.id(), response.available());

        return ResponseEntity.ok(response);
    }

    /**
     * Search menu items by name.
     * 
     * Provides text-based search functionality with pagination.
     * Available to all authenticated users.
     * 
     * @param query         Search query for menu item name
     * @param availableOnly Show only available items
     * @param pageable      Pagination parameters
     * @return Paginated search results
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search menu items", description = "Searches menu items by name with pagination support")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<MenuItemResponse>> searchMenuItems(
            @RequestParam @Parameter(description = "Search query for menu item name", example = "coffee") String query,

            @RequestParam(required = false, defaultValue = "false") @Parameter(description = "Show only available items") Boolean availableOnly,

            @PageableDefault(size = 20, sort = "name") @Parameter(description = "Pagination parameters") Pageable pageable) {

        log.debug("Searching menu items with query: {}, availableOnly: {}", query, availableOnly);
        Page<MenuItemResponse> menuItems = menuItemService.searchMenuItemsByName(query, availableOnly, pageable);

        return ResponseEntity.ok(menuItems);
    }

    /**
     * Get menu items by dietary preferences.
     * 
     * Specialized endpoint for dietary filtering.
     * Available to all authenticated users.
     * 
     * @param vegetarian    Filter vegetarian items
     * @param vegan         Filter vegan items
     * @param glutenFree    Filter gluten-free items
     * @param availableOnly Show only available items
     * @param pageable      Pagination parameters
     * @return Paginated filtered menu items
     */
    @GetMapping("/dietary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get menu items by dietary preferences", description = "Retrieves menu items filtered by dietary preferences")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu items retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<MenuItemResponse>> getMenuItemsByDietaryPreferences(
            @RequestParam(required = false) @Parameter(description = "Filter vegetarian items") Boolean vegetarian,

            @RequestParam(required = false) @Parameter(description = "Filter vegan items") Boolean vegan,

            @RequestParam(required = false) @Parameter(description = "Filter gluten-free items") Boolean glutenFree,

            @RequestParam(required = false, defaultValue = "false") @Parameter(description = "Show only available items") Boolean availableOnly,

            @PageableDefault(size = 20, sort = "name") @Parameter(description = "Pagination parameters") Pageable pageable) {

        log.debug(
                "Fetching menu items by dietary preferences - vegetarian: {}, vegan: {}, glutenFree: {}, available: {}",
                vegetarian, vegan, glutenFree, availableOnly);

        Page<MenuItemResponse> menuItems = menuItemService.getMenuItemsByDietaryPreferences(
                vegetarian, vegan, glutenFree, availableOnly, pageable);

        return ResponseEntity.ok(menuItems);
    }

    /**
     * Get menu items by price range.
     * 
     * Demonstrates BigDecimal handling for monetary filtering.
     * Available to all authenticated users.
     * 
     * @param minPrice      Minimum price (inclusive)
     * @param maxPrice      Maximum price (inclusive)
     * @param availableOnly Show only available items
     * @param pageable      Pagination parameters
     * @return Paginated price-filtered menu items
     */
    @GetMapping("/price-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get menu items by price range", description = "Retrieves menu items within a specified price range using BigDecimal precision")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu items retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid price range"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<MenuItemResponse>> getMenuItemsByPriceRange(
            @RequestParam @Parameter(description = "Minimum price (inclusive)", example = "5.99") BigDecimal minPrice,

            @RequestParam @Parameter(description = "Maximum price (inclusive)", example = "25.99") BigDecimal maxPrice,

            @RequestParam(required = false, defaultValue = "false") @Parameter(description = "Show only available items") Boolean availableOnly,

            @PageableDefault(size = 20, sort = "price") @Parameter(description = "Pagination parameters") Pageable pageable) {

        log.debug("Fetching menu items by price range: {} - {}, available: {}", minPrice, maxPrice, availableOnly);
        Page<MenuItemResponse> menuItems = menuItemService.getMenuItemsByPriceRange(
                minPrice, maxPrice, availableOnly, pageable);

        return ResponseEntity.ok(menuItems);
    }
}
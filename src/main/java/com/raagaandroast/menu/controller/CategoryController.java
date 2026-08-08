package com.raagaandroast.menu.controller;

import com.raagaandroast.menu.dto.CategoryRequest;
import com.raagaandroast.menu.dto.CategoryResponse;
import com.raagaandroast.menu.service.CategoryService;
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

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Category management operations.
 * 
 * Provides endpoints for CRUD operations on menu categories with proper
 * authorization:
 * - READ operations: Available to all authenticated users
 * - WRITE operations: Restricted to MANAGER and ADMIN roles
 * 
 * Features:
 * - Pagination and sorting support
 * - Comprehensive validation
 * - Proper HTTP status codes
 * - OpenAPI documentation
 * - Security annotations
 * 
 * @author RaagaAndRoast Development Team
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management operations")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Create a new category.
     * 
     * Only users with MANAGER or ADMIN roles can create categories.
     * 
     * @param request Category creation request with validation
     * @return Created category response with 201 status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or hasAuthority('CATEGORY_WRITE')")
    @Operation(summary = "Create new category", description = "Creates a new menu category. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        log.info("Creating new category: {}", request.name());
        CategoryResponse response = categoryService.createCategory(request);
        log.info("Category created successfully with ID: {}", response.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all categories with pagination and sorting.
     * 
     * Available to all authenticated users.
     * 
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of categories
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all categories", description = "Retrieves all categories with pagination and sorting support")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 20, sort = "name") @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {

        log.debug("Fetching categories with pagination: {}", pageable);
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);

        return ResponseEntity.ok(categories);
    }

    /**
     * Get all active categories without pagination.
     * 
     * Useful for dropdown lists and category selection.
     * Available to all authenticated users.
     * 
     * @return List of active categories
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active categories", description = "Retrieves all active categories for selection purposes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active categories retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {

        log.debug("Fetching active categories");
        List<CategoryResponse> categories = categoryService.getActiveCategoriesList();

        return ResponseEntity.ok(categories);
    }

    /**
     * Get category by ID.
     * 
     * Available to all authenticated users.
     * 
     * @param id Category UUID
     * @return Category details
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable @Parameter(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID id) {

        log.debug("Fetching category with ID: {}", id);
        CategoryResponse category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(category);
    }

    /**
     * Update an existing category.
     * 
     * Only users with MANAGER or ADMIN roles can update categories.
     * 
     * @param id      Category UUID to update
     * @param request Updated category data
     * @return Updated category response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or hasAuthority('CATEGORY_WRITE')")
    @Operation(summary = "Update category", description = "Updates an existing category. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable @Parameter(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
            @Valid @RequestBody CategoryRequest request) {

        log.info("Updating category with ID: {}", id);
        CategoryResponse response = categoryService.updateCategory(id, request);
        log.info("Category updated successfully: {}", response.id());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a category.
     * 
     * Only users with MANAGER or ADMIN roles can delete categories.
     * Categories with associated menu items cannot be deleted.
     * 
     * @param id Category UUID to delete
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or hasAuthority('CATEGORY_DELETE')")
    @Operation(summary = "Delete category", description = "Deletes a category. Categories with menu items cannot be deleted. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category has associated menu items")
    })
    public ResponseEntity<Void> deleteCategory(
            @PathVariable @Parameter(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID id) {

        log.info("Deleting category with ID: {}", id);
        categoryService.deleteCategory(id);
        log.info("Category deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle category active status.
     * 
     * Allows managers to activate/deactivate categories without deletion.
     * Only users with MANAGER or ADMIN roles can toggle status.
     * 
     * @param id Category UUID
     * @return Updated category response
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or hasAuthority('CATEGORY_WRITE')")
    @Operation(summary = "Toggle category status", description = "Toggles the active status of a category. Requires MANAGER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category status toggled successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> toggleCategoryStatus(
            @PathVariable @Parameter(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID id) {

        log.info("Toggling status for category with ID: {}", id);
        CategoryResponse response = categoryService.toggleCategoryStatus(id);
        log.info("Category status toggled successfully: {} -> active: {}",
                response.id(), response.active());

        return ResponseEntity.ok(response);
    }

    /**
     * Search categories by name.
     * 
     * Available to all authenticated users.
     * 
     * @param name     Search term for category name
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search categories", description = "Searches categories by name with pagination support")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Page<CategoryResponse>> searchCategories(
            @RequestParam @Parameter(description = "Search term for category name", example = "beverages") String name,
            @PageableDefault(size = 20, sort = "name") @Parameter(description = "Pagination parameters") Pageable pageable) {

        log.debug("Searching categories with name containing: {}", name);
        Page<CategoryResponse> categories = categoryService.searchCategoriesByName(name, pageable);

        return ResponseEntity.ok(categories);
    }
}
package com.raagaandroast.menu.service;

import com.raagaandroast.common.exception.*;
import com.raagaandroast.menu.dto.CategoryRequest;
import com.raagaandroast.menu.dto.CategoryResponse;
import com.raagaandroast.menu.entity.Category;
import com.raagaandroast.menu.mapper.CategoryMapper;
import com.raagaandroast.menu.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for Category management.
 * 
 * This service demonstrates:
 * - Proper transaction boundaries with @Transactional
 * - Business logic encapsulation
 * - Exception handling with custom exceptions
 * - Logging for debugging and monitoring
 * - Input validation and sanitization
 * - Performance considerations with pagination
 * - Clean separation from controller and repository layers
 * 
 * Key Learning Points:
 * - Service layer is where business logic belongs
 * - Use @Transactional for data consistency
 * - Handle exceptions gracefully with meaningful messages
 * - Log important business events for monitoring
 * - Validate inputs even if DTOs have validation
 * - Consider performance implications of operations
 * 
 * @author RaagaAndRoast Development Team
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Creates a new category.
     * 
     * @param request the category creation request
     * @return the created category response
     * @throws DuplicateResourceException if category name already exists
     * @throws IllegalArgumentException   if request is invalid
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category with name: {}", request.name());

        // Validate request
        if (!categoryMapper.isValidForCreation(request)) {
            throw CategoryRequestValidationException.invalidCreationRequest();
        }

        // Check for duplicate name
        String cleanName = request.getCleanName();
        if (categoryRepository.existsByNameIgnoreCase(cleanName)) {
            log.warn("Attempt to create category with duplicate name: {}", cleanName);
            throw new DuplicateResourceException("Category with name '" + cleanName + "' already exists");
        }

        // Create and save entity
        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);

        log.info("Successfully created category with ID: {} and name: {}",
                savedCategory.getId(), savedCategory.getName());

        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Retrieves a category by ID.
     * 
     * @param id the category ID
     * @return the category response
     * @throws ResourceNotFoundException if category not found
     */
    public CategoryResponse getCategoryById(UUID id) {
        log.debug("Retrieving category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        return categoryMapper.toResponse(category);
    }

    /**
     * Retrieves a category by ID with menu item count.
     * 
     * @param id the category ID
     * @return the category response with item count
     * @throws ResourceNotFoundException if category not found
     */
    public CategoryResponse getCategoryByIdWithItemCount(UUID id) {
        log.debug("Retrieving category with ID and item count: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // For now, we'll use a simple count query - this can be optimized later
        Integer itemCount = Math.toIntExact(categoryRepository.countByActiveTrue());

        return categoryMapper.toResponseWithItemCount(category, itemCount);
    }

    /**
     * Retrieves all categories with pagination.
     * 
     * @param pageable pagination information
     * @return page of category responses
     */
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.debug("Retrieving all categories with pagination: {}", pageable);

        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.map(categoryMapper::toResponse);
    }

    /**
     * Retrieves all active categories with pagination.
     * 
     * @param pageable pagination information
     * @return page of active category responses
     */
    public Page<CategoryResponse> getActiveCategories(Pageable pageable) {
        log.debug("Retrieving active categories with pagination: {}", pageable);

        Page<Category> categories = categoryRepository.findActiveCategories(pageable);

        return categories.map(categoryMapper::toResponse);
    }

    /**
     * Retrieves all active categories as a simple list (for dropdowns, etc.).
     * 
     * @return list of active category responses
     */
    public List<CategoryResponse> getActiveCategoriesList() {
        log.debug("Retrieving all active categories as list");

        List<Category> categories = categoryRepository.findAllActiveOrderByDisplayOrder();

        return categoryMapper.toResponseList(categories);
    }

    /**
     * Searches categories by name with pagination.
     * 
     * @param name     the search term
     * @param pageable pagination information
     * @return page of matching category responses
     */
    public Page<CategoryResponse> searchCategoriesByName(String name, Pageable pageable) {
        log.debug("Searching categories by name: {} with pagination: {}", name, pageable);

        if (name == null || name.trim().isEmpty()) {
            return getAllCategories(pageable);
        }

        String searchTerm = name.trim();
        Page<Category> categories = categoryRepository.findByNameContainingIgnoreCase(searchTerm, pageable);

        return categories.map(categoryMapper::toResponse);
    }

    /**
     * Updates an existing category.
     * 
     * @param id      the category ID
     * @param request the update request
     * @return the updated category response
     * @throws ResourceNotFoundException  if category not found
     * @throws DuplicateResourceException if new name conflicts with existing
     *                                    category
     */
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        // Validate request
        if (!categoryMapper.isValidForUpdate(request)) {
            throw CategoryRequestValidationException.invalidUpdateRequest();
        }

        // Find existing category
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check for name conflicts (if name is being changed)
        String newName = request.getCleanName();
        if (newName != null && !newName.equalsIgnoreCase(existingCategory.getName())) {
            if (categoryRepository.existsByNameIgnoreCase(newName)) {
                log.warn("Attempt to update category {} with duplicate name: {}", id, newName);
                throw new DuplicateResourceException("Category with name '" + newName + "' already exists");
            }
        }

        // Update entity
        Category updatedCategory = categoryMapper.updateEntity(existingCategory, request);
        Category savedCategory = categoryRepository.save(updatedCategory);

        log.info("Successfully updated category with ID: {}", savedCategory.getId());

        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Activates a category.
     * 
     * @param id the category ID
     * @return the updated category response
     * @throws ResourceNotFoundException if category not found
     */
    @Transactional
    public CategoryResponse activateCategory(UUID id) {
        log.info("Activating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.activate();
        Category savedCategory = categoryRepository.save(category);

        log.info("Successfully activated category with ID: {}", savedCategory.getId());

        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Deactivates a category.
     * 
     * @param id the category ID
     * @return the updated category response
     * @throws ResourceNotFoundException if category not found
     */
    @Transactional
    public CategoryResponse deactivateCategory(UUID id) {
        log.info("Deactivating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.deactivate();
        Category savedCategory = categoryRepository.save(category);

        log.info("Successfully deactivated category with ID: {}", savedCategory.getId());

        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Deletes a category.
     * Note: This is a hard delete. Consider implementing soft delete for
     * production.
     * 
     * @param id the category ID
     * @throws ResourceNotFoundException if category not found
     * @throws IllegalStateException     if category has associated menu items
     */
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check if category has menu items
        // Note: In production, implement proper menu item count query
        // For development, we allow deletion without checking
        Integer itemCount = 0;
        if (itemCount > 0) {
            log.warn("Attempt to delete category {} with {} menu items", id, itemCount);
            throw new CategoryHasMenuItemsException(id, itemCount);
        }

        categoryRepository.delete(category);

        log.info("Successfully deleted category with ID: {}", id);
    }

    /**
     * Checks if a category exists by ID.
     * 
     * @param id the category ID
     * @return true if category exists
     */
    public boolean existsById(UUID id) {
        return categoryRepository.existsById(id);
    }

    /**
     * Checks if a category name exists (case-insensitive).
     * 
     * @param name the category name
     * @return true if name exists
     */
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return categoryRepository.existsByNameIgnoreCase(name.trim());
    }

    /**
     * Gets the count of menu items for a category.
     * 
     * @param categoryId the category ID
     * @return the count of menu items
     */
    public Integer getMenuItemCount(UUID categoryId) {
        // Note: In production, implement proper menu item count query
        // For development phase, returning 0
        return 0;
    }

    /**
     * Retrieves categories with their menu item counts.
     * Useful for admin dashboards and reporting.
     * 
     * @param pageable pagination information
     * @return page of categories with item counts
     */
    public Page<CategoryResponse> getCategoriesWithItemCounts(Pageable pageable) {
        log.debug("Retrieving categories with item counts, pagination: {}", pageable);

        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.map(category -> {
            // Note: In production, implement proper menu item count query
            // For development phase, returning 0
            Integer itemCount = 0;
            return categoryMapper.toResponseWithItemCount(category, itemCount);
        });
    }

    /**
     * Toggle category active status.
     *
     * @param id Category ID
     * @return Updated category
     */
    @Transactional
    public CategoryResponse toggleCategoryStatus(UUID id) {
        log.info("Toggling status for category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setActive(!category.isActive());
        Category savedCategory = categoryRepository.save(category);

        log.info("Category status toggled: {} -> active: {}", id, savedCategory.isActive());
        return categoryMapper.toResponse(savedCategory);
    }
}
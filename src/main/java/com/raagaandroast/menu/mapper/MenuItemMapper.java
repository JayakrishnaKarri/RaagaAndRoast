package com.raagaandroast.menu.mapper;

import com.raagaandroast.menu.dto.CategoryResponse;
import com.raagaandroast.menu.dto.MenuItemRequest;
import com.raagaandroast.menu.dto.MenuItemResponse;
import com.raagaandroast.menu.entity.Category;
import com.raagaandroast.menu.entity.MenuItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting between MenuItem entities and DTOs.
 * 
 * This mapper demonstrates:
 * - Complex entity-DTO mapping with relationships
 * - Proper BigDecimal handling in mapping operations
 * - Integration with other mappers (CategoryMapper)
 * - Business logic application during mapping
 * - Performance considerations for nested relationships
 * - Defensive programming with null safety
 * 
 * Key Learning Points:
 * - Handle complex relationships properly in mapping
 * - Avoid N+1 problems when mapping collections
 * - Apply business rules consistently during mapping
 * - Use dependency injection for related mappers
 * - Consider performance implications of nested mappings
 * 
 * @author RaagaAndRoast Development Team
 */
@Component
public class MenuItemMapper {

    private final CategoryMapper categoryMapper;

    public MenuItemMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * Converts a MenuItemRequest DTO to a MenuItem entity.
     * Used for creating new menu items.
     * Note: Category relationship must be set separately in the service layer.
     * 
     * @param request the menu item request DTO
     * @return new MenuItem entity, or null if request is null
     */
    public MenuItem toEntity(MenuItemRequest request) {
        if (request == null) {
            return null;
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getCleanName());
        menuItem.setDescription(request.getCleanDescription());
        menuItem.setPrice(request.price());
        menuItem.setAvailable(request.available() != null ? request.available() : true);
        menuItem.setPreparationTimeMinutes(
                request.preparationTimeMinutes() != null ? request.preparationTimeMinutes() : 15);

        // Set dietary preferences
        menuItem.setVegetarian(request.vegetarian() != null ? request.vegetarian() : false);
        menuItem.setVegan(request.vegan() != null ? request.vegan() : false);
        menuItem.setGlutenFree(request.glutenFree() != null ? request.glutenFree() : false);
        menuItem.setSpicy(request.spicy() != null ? request.spicy() : false);
        // Note: spiceLevel is handled in the DTO but not stored in entity
        // The entity only has a boolean spicy field

        // Apply business rules during mapping
        applyBusinessRules(menuItem);

        // Note: Category must be set in service layer after validation

        return menuItem;
    }

    /**
     * Updates an existing MenuItem entity with data from MenuItemRequest.
     * Used for updating existing menu items while preserving audit information.
     * 
     * @param entity  the existing menu item entity to update
     * @param request the menu item request DTO with new data
     * @return the updated entity, or null if either parameter is null
     */
    public MenuItem updateEntity(MenuItem entity, MenuItemRequest request) {
        if (entity == null || request == null) {
            return entity;
        }

        // Update modifiable fields
        entity.setName(request.getCleanName());
        entity.setDescription(request.getCleanDescription());
        entity.setPrice(request.price());
        entity.setAvailable(request.available() != null ? request.available() : entity.getAvailable());
        entity.setPreparationTimeMinutes(request.preparationTimeMinutes() != null ? request.preparationTimeMinutes()
                : entity.getPreparationTimeMinutes());

        // Update dietary preferences
        entity.setVegetarian(request.vegetarian() != null ? request.vegetarian() : entity.getVegetarian());
        entity.setVegan(request.vegan() != null ? request.vegan() : entity.getVegan());
        entity.setGlutenFree(request.glutenFree() != null ? request.glutenFree() : entity.getGlutenFree());
        entity.setSpicy(request.spicy() != null ? request.spicy() : entity.getSpicy());
        // Note: spiceLevel is handled in the DTO but not stored in entity
        // The entity only has a boolean spicy field

        // Apply business rules during update
        applyBusinessRules(entity);

        // Note: Category update must be handled in service layer
        // Note: ID, createdAt, updatedAt, and version are managed by JPA

        return entity;
    }

    /**
     * Converts a MenuItem entity to a MenuItemResponse DTO.
     * 
     * @param entity the menu item entity
     * @return MenuItemResponse DTO, or null if entity is null
     */
    public MenuItemResponse toResponse(MenuItem entity) {
        if (entity == null) {
            return null;
        }

        // Convert category to summary (avoid full category details to prevent circular
        // references)
        CategoryResponse.CategorySummary categorySummary = null;
        if (entity.getCategory() != null) {
            categorySummary = categoryMapper.toSummary(entity.getCategory());
        }

        return MenuItemResponse.of(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getAvailable(),
                entity.getPreparationTimeMinutes(),
                entity.getVegetarian(),
                entity.getVegan(),
                entity.getGlutenFree(),
                entity.getSpicy(),
                null, // spiceLevel not stored in entity, only in DTO
                categorySummary,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }

    /**
     * Converts a MenuItem entity to a MenuItemResponse DTO without computed fields.
     * Useful when computed fields are not needed or will be calculated separately.
     * 
     * @param entity the menu item entity
     * @return basic MenuItemResponse DTO, or null if entity is null
     */
    public MenuItemResponse toBasicResponse(MenuItem entity) {
        if (entity == null) {
            return null;
        }

        CategoryResponse.CategorySummary categorySummary = null;
        if (entity.getCategory() != null) {
            categorySummary = categoryMapper.toSummary(entity.getCategory());
        }

        return MenuItemResponse.basic(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getAvailable(),
                entity.getPreparationTimeMinutes(),
                entity.getVegetarian(),
                entity.getVegan(),
                entity.getGlutenFree(),
                entity.getSpicy(),
                null, // spiceLevel not stored in entity, only in DTO
                categorySummary,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }

    /**
     * Converts a MenuItem entity to a MenuItemSummary DTO.
     * Used when only basic menu item information is needed.
     * 
     * @param entity the menu item entity
     * @return MenuItemSummary DTO, or null if entity is null
     */
    public MenuItemResponse.MenuItemSummary toSummary(MenuItem entity) {
        if (entity == null) {
            return null;
        }

        return MenuItemResponse.MenuItemSummary.of(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getAvailable(),
                entity.getVegetarian(),
                entity.getVegan(),
                entity.getGlutenFree());
    }

    /**
     * Converts a list of MenuItem entities to a list of MenuItemResponse DTOs.
     * Optimized for bulk operations.
     * 
     * @param entities list of menu item entities
     * @return list of MenuItemResponse DTOs, empty list if input is null or empty
     */
    public List<MenuItemResponse> toResponseList(List<MenuItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Converts a list of MenuItem entities to a list of MenuItemSummary DTOs.
     * Optimized for bulk operations when only summary information is needed.
     * 
     * @param entities list of menu item entities
     * @return list of MenuItemSummary DTOs, empty list if input is null or empty
     */
    public List<MenuItemResponse.MenuItemSummary> toSummaryList(List<MenuItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Validates that a MenuItemRequest contains valid data for entity creation.
     * This method can be used in service layer for additional validation.
     * 
     * @param request the menu item request to validate
     * @return true if the request is valid for entity creation
     */
    public boolean isValidForCreation(MenuItemRequest request) {
        return request != null &&
                request.isValid() &&
                request.getCleanName() != null &&
                !request.getCleanName().isEmpty() &&
                request.price() != null &&
                request.categoryId() != null;
    }

    /**
     * Validates that a MenuItemRequest contains valid data for entity update.
     * This method can be used in service layer for additional validation.
     * 
     * @param request the menu item request to validate
     * @return true if the request is valid for entity update
     */
    public boolean isValidForUpdate(MenuItemRequest request) {
        return request != null &&
                request.isValid() &&
                request.isDietaryInfoConsistent();
    }

    /**
     * Applies business rules to a MenuItem entity during mapping.
     * This ensures consistency regardless of how the entity is created or updated.
     * 
     * @param menuItem the menu item entity to apply rules to
     */
    private void applyBusinessRules(MenuItem menuItem) {
        if (menuItem == null) {
            return;
        }

        // Business rule: vegan items are automatically vegetarian
        if (Boolean.TRUE.equals(menuItem.getVegan()) && !Boolean.TRUE.equals(menuItem.getVegetarian())) {
            menuItem.setVegetarian(true);
        }

        // Note: spiceLevel is handled in DTO layer only
        // Entity only has boolean spicy field for simplicity

        // Ensure preparation time is reasonable
        if (menuItem.getPreparationTimeMinutes() == null || menuItem.getPreparationTimeMinutes() <= 0) {
            menuItem.setPreparationTimeMinutes(15); // Default 15 minutes
        }
    }

    /**
     * Creates a MenuItem entity with a specific Category.
     * This is a convenience method for service layer use.
     * 
     * @param request  the menu item request DTO
     * @param category the category entity to associate with the menu item
     * @return new MenuItem entity with category set, or null if request is null
     */
    public MenuItem toEntityWithCategory(MenuItemRequest request, Category category) {
        MenuItem menuItem = toEntity(request);
        if (menuItem != null && category != null) {
            menuItem.setCategory(category);
        }
        return menuItem;
    }
}
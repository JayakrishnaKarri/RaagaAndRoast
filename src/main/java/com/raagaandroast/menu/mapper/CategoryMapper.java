package com.raagaandroast.menu.mapper;

import com.raagaandroast.menu.dto.CategoryRequest;
import com.raagaandroast.menu.dto.CategoryResponse;
import com.raagaandroast.menu.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting between Category entities and DTOs.
 * 
 * This mapper demonstrates:
 * - Clean separation of concerns between entities and DTOs
 * - Proper handling of null values and edge cases
 * - Business logic integration during mapping
 * - Performance considerations for bulk operations
 * - Defensive programming practices
 * 
 * Key Learning Points:
 * - Mappers should be stateless and thread-safe
 * - Handle null inputs gracefully
 * - Consider performance for bulk operations
 * - Apply business rules during mapping when appropriate
 * - Keep mapping logic simple and focused
 * 
 * @author RaagaAndRoast Development Team
 */
@Component
public class CategoryMapper {

    /**
     * Converts a CategoryRequest DTO to a Category entity.
     * Used for creating new categories.
     * 
     * @param request the category request DTO
     * @return new Category entity, or null if request is null
     */
    public Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }

        Category category = new Category();
        category.setName(request.getCleanName());
        category.setDescription(request.getCleanDescription());
        category.setActive(request.active() != null ? request.active() : true);

        return category;
    }

    /**
     * Updates an existing Category entity with data from CategoryRequest.
     * Used for updating existing categories while preserving audit information.
     * 
     * @param entity  the existing category entity to update
     * @param request the category request DTO with new data
     * @return the updated entity, or null if either parameter is null
     */
    public Category updateEntity(Category entity, CategoryRequest request) {
        if (entity == null || request == null) {
            return entity;
        }

        // Update only the fields that can be modified
        entity.setName(request.getCleanName());
        entity.setDescription(request.getCleanDescription());
        entity.setActive(request.active() != null ? request.active() : entity.getActive());

        // Note: ID, createdAt, updatedAt, and version are managed by JPA

        return entity;
    }

    /**
     * Converts a Category entity to a CategoryResponse DTO.
     * 
     * @param entity the category entity
     * @return CategoryResponse DTO, or null if entity is null
     */
    public CategoryResponse toResponse(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryResponse.of(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }

    /**
     * Converts a Category entity to a CategoryResponse DTO with menu item count.
     * This method is useful when you need to include the count of menu items
     * in the category response.
     * 
     * @param entity        the category entity
     * @param menuItemCount the number of menu items in this category
     * @return CategoryResponse DTO with item count, or null if entity is null
     */
    public CategoryResponse toResponseWithItemCount(Category entity, Integer menuItemCount) {
        if (entity == null) {
            return null;
        }

        return CategoryResponse.withItemCount(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion(),
                menuItemCount);
    }

    /**
     * Converts a Category entity to a CategorySummary DTO.
     * Used when only basic category information is needed.
     * 
     * @param entity the category entity
     * @return CategorySummary DTO, or null if entity is null
     */
    public CategoryResponse.CategorySummary toSummary(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryResponse.CategorySummary.of(
                entity.getId(),
                entity.getName(),
                entity.getActive());
    }

    /**
     * Converts a list of Category entities to a list of CategoryResponse DTOs.
     * Optimized for bulk operations.
     * 
     * @param entities list of category entities
     * @return list of CategoryResponse DTOs, empty list if input is null or empty
     */
    public List<CategoryResponse> toResponseList(List<Category> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Converts a list of Category entities to a list of CategorySummary DTOs.
     * Optimized for bulk operations when only summary information is needed.
     * 
     * @param entities list of category entities
     * @return list of CategorySummary DTOs, empty list if input is null or empty
     */
    public List<CategoryResponse.CategorySummary> toSummaryList(List<Category> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Validates that a CategoryRequest contains valid data for entity creation.
     * This method can be used in service layer for additional validation.
     * 
     * @param request the category request to validate
     * @return true if the request is valid for entity creation
     */
    public boolean isValidForCreation(CategoryRequest request) {
        return request != null &&
                request.isValid() &&
                request.getCleanName() != null &&
                !request.getCleanName().isEmpty();
    }

    /**
     * Validates that a CategoryRequest contains valid data for entity update.
     * This method can be used in service layer for additional validation.
     * 
     * @param request the category request to validate
     * @return true if the request is valid for entity update
     */
    public boolean isValidForUpdate(CategoryRequest request) {
        return request != null &&
                request.isValid();
    }
}
package com.raagaandroast.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raagaandroast.user.entity.Permission;

/**
 * Repository interface for Permission entity operations.
 * 
 * This repository provides:
 * - Permission management operations
 * - Role-permission relationship queries
 * - Resource-based permission queries
 * - Performance-optimized queries
 * 
 * @author RaagaAndRoast Development Team
 */

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

        // ================================================================
        // Basic Permission Queries
        // ================================================================

        /**
         * Finds a permission by name.
         * Used for permission assignment and authorization checks.
         * 
         * @param name the permission name
         * @return Optional containing the permission if found
         */
        Optional<Permission> findByName(String name);

        /**
         * Finds a permission by name with roles loaded.
         * Optimized for permission management operations.
         *
         * @param name the permission name
         * @return Optional containing the permission with roles
         */
        @Query("SELECT p FROM Permission p LEFT JOIN FETCH p.roles WHERE p.name = :name")
        Optional<Permission> findByNameWithRoles(@Param("name") String name);

        /**
         * Checks if a permission name already exists.
         * Used for permission creation validation.
         * 
         * @param name the permission name to check
         * @return true if permission exists
         */
        boolean existsByName(String name);

        /**
         * Checks if permission name exists for a different permission (for updates).
         * 
         * @param name         the permission name to check
         * @param permissionId the current permission's ID to exclude
         * @return true if name exists for another permission
         */
        boolean existsByNameAndIdNot(String name, UUID permissionId);

        // ================================================================
        // Resource-based Queries
        // ================================================================

        /**
         * Finds permissions by resource name.
         * Used to get all permissions for a specific resource (e.g., USER, MENU).
         * 
         * @param resource the resource name
         * @return List of permissions for the resource
         */
        @Query("SELECT p FROM Permission p WHERE p.resource = :resource")
        List<Permission> findByResource(@Param("resource") String resource);

        /**
         * Finds permissions by action.
         * Used to get all permissions for a specific action (e.g., READ, WRITE).
         * 
         * @param action the action name
         * @return List of permissions for the action
         */
        @Query("SELECT p FROM Permission p WHERE p.action = :action")
        List<Permission> findByAction(@Param("action") String action);

        /**
         * Finds permissions by resource and action.
         * Used for specific permission lookups.
         * 
         * @param resource the resource name
         * @param action   the action name
         * @return Optional containing the permission if found
         */
        @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action")
        Optional<Permission> findByResourceAndAction(@Param("resource") String resource,
                        @Param("action") String action);

        /**
         * Finds all permissions for multiple resources.
         * Efficient batch lookup for resource-based operations.
         * 
         * @param resources set of resource names
         * @return List of permissions for the resources
         */
        @Query("SELECT p FROM Permission p WHERE p.resource IN :resources")
        List<Permission> findByResourceIn(@Param("resources") Set<String> resources);

        // ================================================================
        // Role-Permission Relationship Queries
        // ================================================================

        /**
         * Finds permissions assigned to a specific role.
         * Used for role management and authorization operations.
         * 
         * @param roleId the role ID
         * @return List of permissions assigned to the role
         */
        @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
        List<Permission> findByRoleId(@Param("roleId") UUID roleId);

        /**
         * Finds permissions assigned to a role by role name.
         * Alternative query method for role name-based lookups.
         * 
         * @param roleName the role name
         * @return List of permissions assigned to the role
         */
        @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
        List<Permission> findByRoleName(@Param("roleName") String roleName);

        /**
         * Finds permissions assigned to multiple roles.
         * Used for complex authorization scenarios.
         * 
         * @param roleNames set of role names
         * @return List of permissions assigned to any of the roles
         */
        @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r WHERE r.name IN :roleNames")
        List<Permission> findByRoleNameIn(@Param("roleNames") Set<String> roleNames);

        /**
         * Finds permissions for a user through their roles.
         * Used for complete user authorization context.
         * 
         * @param userId the user ID
         * @return List of permissions available to the user
         */
        @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
        List<Permission> findByUserId(@Param("userId") UUID userId);

        /**
         * Finds permissions for a user by username.
         * Alternative query method for username-based authorization.
         * 
         * @param username the username
         * @return List of permissions available to the user
         */
        @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.username = :username")
        List<Permission> findByUsername(@Param("username") String username);

        // ================================================================
        // Statistics and Reporting
        // ================================================================

        /**
         * Counts roles assigned to each permission.
         * Used for permission usage analysis.
         * 
         * @return List of permission names and role counts
         */
        @Query("SELECT p.name, COUNT(r) FROM Permission p LEFT JOIN p.roles r GROUP BY p.name ORDER BY p.name")
        List<Object[]> getPermissionRoleCounts();

        /**
         * Finds permissions with no roles assigned.
         * Used for permission cleanup and optimization.
         * 
         * @return List of unused permissions
         */
        @Query("SELECT p FROM Permission p WHERE p.roles IS EMPTY")
        List<Permission> findUnusedPermissions();

        /**
         * Gets permission distribution by resource.
         * Used for resource coverage analysis.
         * 
         * @return List of resources and permission counts
         */
        @Query("SELECT p.resource, COUNT(p) FROM Permission p GROUP BY p.resource ORDER BY p.resource")
        List<Object[]> getPermissionCountsByResource();

        /**
         * Gets permission distribution by action.
         * Used for action coverage analysis.
         * 
         * @return List of actions and permission counts
         */
        @Query("SELECT p.action, COUNT(p) FROM Permission p GROUP BY p.action ORDER BY p.action")
        List<Object[]> getPermissionCountsByAction();

        // ================================================================
        // Common Permission Lookups
        // ================================================================

        /**
         * Finds multiple permissions by their names.
         * Efficient batch lookup for permission assignment operations.
         * 
         * @param permissionNames set of permission names
         * @return List of matching permissions
         */
        List<Permission> findByNameIn(Set<String> permissionNames);

        /**
         * Finds multiple permissions by names with roles loaded.
         * Optimized for batch operations requiring role details.
         * 
         * @param permissionNames set of permission names
         * @return List of permissions with roles
         */
        @EntityGraph(attributePaths = { "roles" })
        @Query("SELECT p FROM Permission p WHERE p.name IN :permissionNames")
        List<Permission> findByNameInWithRoles(@Param("permissionNames") Set<String> permissionNames);

        /**
         * Finds all permissions with roles loaded.
         * Optimized query for permission management interfaces.
         * 
         * @param pageable pagination information
         * @return Page of permissions with roles
         */
        @EntityGraph(attributePaths = { "roles" })
        @Query("SELECT p FROM Permission p")
        Page<Permission> findAllWithRoles(Pageable pageable);

        // ================================================================
        // Search Operations
        // ================================================================

        /**
         * Searches permissions by name or description.
         * Case-insensitive search for permission management interfaces.
         * 
         * @param searchTerm the term to search for
         * @param pageable   pagination information
         * @return Page of matching permissions
         */
        @Query("SELECT p FROM Permission p WHERE " +
                        "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<Permission> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Searches permissions by resource or action.
         * Used for resource-specific permission management.
         * 
         * @param searchTerm the term to search for
         * @param pageable   pagination information
         * @return Page of matching permissions
         */
        @Query("SELECT p FROM Permission p WHERE " +
                        "LOWER(p.resource) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.action) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<Permission> searchByResourceOrAction(@Param("searchTerm") String searchTerm, Pageable pageable);

        // ================================================================
        // Authorization Helper Queries
        // ================================================================

        /**
         * Checks if a user has a specific permission.
         * Used for authorization checks in services.
         * 
         * @param userId         the user ID
         * @param permissionName the permission name
         * @return true if user has the permission
         */
        @Query("SELECT COUNT(p) > 0 FROM Permission p JOIN p.roles r JOIN r.users u " +
                        "WHERE u.id = :userId AND p.name = :permissionName")
        boolean userHasPermission(@Param("userId") UUID userId, @Param("permissionName") String permissionName);

        /**
         * Checks if a user has any of the specified permissions.
         * Used for complex authorization scenarios.
         * 
         * @param userId          the user ID
         * @param permissionNames set of permission names
         * @return true if user has any of the permissions
         */
        @Query("SELECT COUNT(p) > 0 FROM Permission p JOIN p.roles r JOIN r.users u " +
                        "WHERE u.id = :userId AND p.name IN :permissionNames")
        boolean userHasAnyPermission(@Param("userId") UUID userId,
                        @Param("permissionNames") Set<String> permissionNames);
}
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

import com.raagaandroast.user.entity.Role;

/**
 * Repository interface for Role entity operations.
 * 
 * This repository provides:
 * - Role management operations
 * - Permission-based queries
 * - User-role relationship queries
 * - Performance-optimized queries with EntityGraph
 * 
 * @author RaagaAndRoast Development Team
 */

public interface RoleRepository extends JpaRepository<Role, UUID> {

    // ================================================================
    // Basic Role Queries
    // ================================================================

    /**
     * Finds a role by name.
     * Used for role assignment and authorization checks.
     * 
     * @param name the role name
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Finds a role by name with permissions loaded.
     * Optimized for authorization operations that need permission details.
     *
     * @param name the role name
     * @return Optional containing the role with permissions
     */
    @EntityGraph(attributePaths = { "permissions" })
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);

    /**
     * Checks if a role name already exists.
     * Used for role creation validation.
     * 
     * @param name the role name to check
     * @return true if role exists
     */
    boolean existsByName(String name);

    /**
     * Checks if role name exists for a different role (for updates).
     * 
     * @param name   the role name to check
     * @param roleId the current role's ID to exclude
     * @return true if name exists for another role
     */
    boolean existsByNameAndIdNot(String name, UUID roleId);

    // ================================================================
    // Permission-based Queries
    // ================================================================

    /**
     * Finds roles that have a specific permission.
     * Useful for permission management and auditing.
     * 
     * @param permissionName the permission name
     * @return List of roles with the permission
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Finds roles that have any of the specified permissions.
     * Used for complex authorization scenarios.
     * 
     * @param permissionNames set of permission names
     * @return List of roles with any of the permissions
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name IN :permissionNames")
    List<Role> findByPermissionNameIn(@Param("permissionNames") Set<String> permissionNames);

    /**
     * Finds roles with their permissions loaded.
     * Optimized query for role management interfaces.
     * 
     * @param pageable pagination information
     * @return Page of roles with permissions
     */
    @EntityGraph(attributePaths = { "permissions" })
    @Query("SELECT r FROM Role r")
    Page<Role> findAllWithPermissions(Pageable pageable);

    // ================================================================
    // User-Role Relationship Queries
    // ================================================================

    /**
     * Finds roles assigned to a specific user.
     * Used for user profile and authorization operations.
     * 
     * @param userId the user ID
     * @return List of roles assigned to the user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") UUID userId);

    /**
     * Finds roles assigned to a user with permissions loaded.
     * Optimized for complete authorization context.
     * 
     * @param userId the user ID
     * @return List of roles with permissions for the user
     */
    @EntityGraph(attributePaths = { "permissions" })
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserIdWithPermissions(@Param("userId") UUID userId);

    /**
     * Finds roles assigned to a user by username.
     * Alternative query method for username-based lookups.
     * 
     * @param username the username
     * @return List of roles assigned to the user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.username = :username")
    List<Role> findByUsername(@Param("username") String username);

    // ================================================================
    // Statistics and Reporting
    // ================================================================

    /**
     * Counts users assigned to each role.
     * Used for role distribution reporting.
     * 
     * @return List of role names and user counts
     */
    @Query("SELECT r.name, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r.name ORDER BY r.name")
    List<Object[]> getRoleUserCounts();

    /**
     * Counts permissions assigned to each role.
     * Used for role complexity analysis.
     * 
     * @return List of role names and permission counts
     */
    @Query("SELECT r.name, COUNT(p) FROM Role r LEFT JOIN r.permissions p GROUP BY r.name ORDER BY r.name")
    List<Object[]> getRolePermissionCounts();

    /**
     * Finds roles with no users assigned.
     * Used for role cleanup and optimization.
     * 
     * @return List of unused roles
     */
    @Query("SELECT r FROM Role r WHERE r.users IS EMPTY")
    List<Role> findUnusedRoles();

    /**
     * Finds roles with no permissions assigned.
     * Used for role configuration validation.
     * 
     * @return List of roles without permissions
     */
    @Query("SELECT r FROM Role r WHERE r.permissions IS EMPTY")
    List<Role> findRolesWithoutPermissions();

    // ================================================================
    // Common Role Lookups
    // ================================================================

    /**
     * Finds multiple roles by their names.
     * Efficient batch lookup for role assignment operations.
     * 
     * @param roleNames set of role names
     * @return List of matching roles
     */
    List<Role> findByNameIn(Set<String> roleNames);

    /**
     * Finds multiple roles by names with permissions loaded.
     * Optimized for batch operations requiring permission details.
     * 
     * @param roleNames set of role names
     * @return List of roles with permissions
     */
    @EntityGraph(attributePaths = { "permissions" })
    @Query("SELECT r FROM Role r WHERE r.name IN :roleNames")
    List<Role> findByNameInWithPermissions(@Param("roleNames") Set<String> roleNames);

    // ================================================================
    // Search Operations
    // ================================================================

    /**
     * Searches roles by name or description.
     * Case-insensitive search for role management interfaces.
     * 
     * @param searchTerm the term to search for
     * @param pageable   pagination information
     * @return Page of matching roles
     */
    @Query("SELECT r FROM Role r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Role> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
}
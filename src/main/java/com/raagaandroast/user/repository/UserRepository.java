package com.raagaandroast.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.raagaandroast.user.entity.User;

/**
 * Repository interface for User entity operations.
 * 
 * This repository demonstrates several advanced JPA concepts: - Custom query
 * methods with proper naming conventions - JPQL queries for complex operations
 * - EntityGraph for solving N+1 query problems - Pagination support for large
 * datasets - Performance-optimized queries
 * 
 * Design Decisions: - Extends JpaRepository for full CRUD + pagination support
 * - Custom finder methods for authentication and user management - EntityGraph
 * annotations to control fetch strategies - Optimized queries for common use
 * cases
 * 
 * Interview Points: - Why JpaRepository over CrudRepository? Pagination and
 * batch operations - Why custom queries? Business-specific operations not
 * covered by derived queries - Why EntityGraph? Solves N+1 problem without
 * changing entity relationships
 * 
 * @author RaagaAndRoast Development Team
 */

public interface UserRepository extends JpaRepository<User, UUID> {

	// ================================================================
	// Authentication Queries
	// ================================================================

	/**
	 * Finds a user by username for authentication. Uses EntityGraph to eagerly
	 * fetch roles and permissions to avoid N+1 queries.
	 * 
	 * This is critical for Spring Security UserDetailsService implementation.
	 * 
	 * @param username the username to search for
	 * @return Optional containing the user if found
	 */
	@EntityGraph(attributePaths = { "roles", "roles.permissions" })
	Optional<User> findByUsername(String username);

	/**
	 * Finds a user by email for authentication and password reset. Uses EntityGraph
	 * to eagerly fetch roles for complete user context.
	 * 
	 * @param email the email to search for
	 * @return Optional containing the user if found
	 */
	@EntityGraph(attributePaths = { "roles" })
	Optional<User> findByEmail(String email);

	/**
	 * Finds a user by username or email for flexible authentication. Useful for
	 * login systems that accept either username or email.
	 * 
	 * @param username the username to search for
	 * @param email    the email to search for
	 * @return Optional containing the user if found
	 */
	@EntityGraph(attributePaths = { "roles", "roles.permissions" })
	Optional<User> findByUsernameOrEmail(String username, String email);

	/**
	 * Finds a user by username with roles and permissions loaded. Alias method for
	 * CustomUserDetailsService compatibility.
	 *
	 * @param username the username to search for
	 * @return Optional containing the user with roles and permissions
	 */
	@EntityGraph(attributePaths = { "roles", "roles.permissions" })
	@Query("SELECT u FROM User u WHERE u.username = :username")
	Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);

	// ================================================================
	// Existence Checks (for validation)
	// ================================================================

	/**
	 * Checks if a username already exists. Used for registration validation to
	 * ensure unique usernames.
	 * 
	 * @param username the username to check
	 * @return true if username exists
	 */
	boolean existsByUsername(String username);

	/**
	 * Checks if an email already exists. Used for registration validation to ensure
	 * unique emails.
	 * 
	 * @param email the email to check
	 * @return true if email exists
	 */
	boolean existsByEmail(String email);

	/**
	 * Checks if username exists for a different user (for updates). Used when
	 * updating user profiles to ensure username uniqueness.
	 * 
	 * @param username the username to check
	 * @param userId   the current user's ID to exclude
	 * @return true if username exists for another user
	 */
	boolean existsByUsernameAndIdNot(String username, UUID userId);

	/**
	 * Checks if email exists for a different user (for updates). Used when updating
	 * user profiles to ensure email uniqueness.
	 * 
	 * @param email  the email to check
	 * @param userId the current user's ID to exclude
	 * @return true if email exists for another user
	 */
	boolean existsByEmailAndIdNot(String email, UUID userId);

	// ================================================================
	// User Management Queries
	// ================================================================

	/**
	 * Finds all enabled users with pagination. Used for user management interfaces.
	 * 
	 * @param enabled  the enabled status to filter by
	 * @param pageable pagination information
	 * @return Page of users
	 */
	Page<User> findByEnabled(Boolean enabled, Pageable pageable);

	/**
	 * Finds users by role name with pagination. Useful for administrative queries
	 * like "find all managers".
	 * 
	 * @param roleName the role name to filter by
	 * @param pageable pagination information
	 * @return Page of users with the specified role
	 */
	@Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
	Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

	/**
	 * Finds users created within a date range. Useful for reporting and analytics.
	 * 
	 * @param startDate the start date
	 * @param endDate   the end date
	 * @return List of users created in the date range
	 */
	List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * Searches users by username or email containing the search term.
	 * Case-insensitive search for user management interfaces.
	 * 
	 * @param searchTerm the term to search for
	 * @param pageable   pagination information
	 * @return Page of matching users
	 */
	@Query("SELECT u FROM User u WHERE " + "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
	Page<User> searchByUsernameOrEmail(@Param("searchTerm") String searchTerm, Pageable pageable);

	// ================================================================
	// Security and Account Management Queries
	// ================================================================

	/**
	 * Finds users with expired credentials. Used for security maintenance and
	 * password policy enforcement.
	 * 
	 * @return List of users with expired credentials
	 */
	List<User> findByCredentialsNonExpiredFalse();

	/**
	 * Finds locked user accounts. Used for security monitoring and account
	 * management.
	 * 
	 * @return List of locked user accounts
	 */
	List<User> findByAccountNonLockedFalse();

	/**
	 * Finds disabled user accounts. Used for administrative reporting.
	 * 
	 * @return List of disabled user accounts
	 */
	List<User> findByEnabledFalse();

	// ================================================================
	// Performance Optimized Queries
	// ================================================================

	/**
	 * Finds user with roles and permissions in a single query. Demonstrates
	 * EntityGraph usage to solve N+1 query problem.
	 * 
	 * This is crucial for performance when you need complete user context.
	 * 
	 * @param userId the user ID
	 * @return Optional containing user with all relationships loaded
	 */
	@EntityGraph(attributePaths = { "roles", "roles.permissions" })
	@Query("SELECT u FROM User u WHERE u.id = :userId")
	Optional<User> findByIdWithRolesAndPermissions(@Param("userId") UUID userId);

	/**
	 * Finds users with their roles (but not permissions) for lighter queries. Use
	 * when you need role information but not detailed permissions.
	 * 
	 * @param pageable pagination information
	 * @return Page of users with roles loaded
	 */
	@EntityGraph(attributePaths = { "roles" })
	@Query("SELECT u FROM User u")
	Page<User> findAllWithRoles(Pageable pageable);

	// ================================================================
	// Statistics and Reporting Queries
	// ================================================================

	/**
	 * Counts users by enabled status. Used for dashboard statistics.
	 * 
	 * @param enabled the enabled status
	 * @return count of users
	 */
	long countByEnabled(Boolean enabled);

	/**
	 * Counts users by role name. Used for role distribution reporting.
	 * 
	 * @param roleName the role name
	 * @return count of users with the role
	 */
	@Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
	long countByRoleName(@Param("roleName") String roleName);

	/**
	 * Gets user registration statistics by month. Used for growth analytics and
	 * reporting.
	 * 
	 * @param year the year to analyze
	 * @return List of monthly registration counts
	 */
	@Query("SELECT MONTH(u.createdAt) as month, COUNT(u) as count " + "FROM User u WHERE YEAR(u.createdAt) = :year "
			+ "GROUP BY MONTH(u.createdAt) ORDER BY MONTH(u.createdAt)")
	List<Object[]> getUserRegistrationStatsByYear(@Param("year") int year);

	// ================================================================
	// Custom Update Operations
	// ================================================================

	/**
	 * Updates user enabled status. More efficient than loading entity and saving
	 * for simple updates.
	 *
	 * @param userId  the user ID
	 * @param enabled the new enabled status
	 * @return number of affected rows
	 */
	@Modifying
	@Transactional
	@Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
	int updateEnabledStatus(@Param("userId") UUID userId, @Param("enabled") Boolean enabled);

	/**
	 * Updates user account locked status. Used for security operations like account
	 * locking after failed attempts.
	 *
	 * @param userId the user ID
	 * @param locked the new locked status
	 * @return number of affected rows
	 */
	@Modifying
	@Transactional
	@Query("UPDATE User u SET u.accountNonLocked = :locked WHERE u.id = :userId")
	int updateAccountLockedStatus(@Param("userId") UUID userId, @Param("locked") Boolean locked);
}
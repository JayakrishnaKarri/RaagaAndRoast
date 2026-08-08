package com.raagaandroast.user.controller;

import com.raagaandroast.user.dto.UserResponse;
import com.raagaandroast.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for User Management operations.
 * 
 * This controller demonstrates comprehensive authorization patterns: -
 * Role-based authorization (@PreAuthorize with roles) - Permission-based
 * authorization (@PreAuthorize with authorities) - Resource ownership
 * authorization (users can access their own data) - Combined authorization
 * (multiple conditions)
 * 
 * Authorization Patterns Demonstrated: 1. ADMIN-only operations (user
 * management) 2. User can access own data (resource ownership) 3. ADMIN or
 * owner can access data (combined authorization) 4. Permission-based access
 * (fine-grained control)
 * 
 * Interview Points: - Why separate role and permission authorization? Different
 * granularity levels - Why resource ownership checks? Data privacy and security
 * - How does @PreAuthorize work? Method-level security with SpEL expressions -
 * What's the difference between 401 and 403? Authentication vs Authorization
 * 
 * @author RaagaAndRoast Development Team
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final UserService userService;

	/**
	 * Get all users with pagination.
	 * 
	 * Authorization: Only ADMIN users can list all users. This demonstrates
	 * role-based authorization.
	 * 
	 * @param pageable pagination parameters
	 * @return paginated list of users
	 */
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get all users", description = "Retrieve a paginated list of all users. Only accessible by ADMIN users.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required") })
	public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {

		log.info("Admin requesting all users with pagination: {}", pageable);
		Page<UserResponse> users = userService.getAllUsers(pageable);
		return ResponseEntity.ok(users);
	}

	/**
	 * Get user by ID.
	 * 
	 * Authorization: User can access their own data OR ADMIN can access any user.
	 * This demonstrates combined authorization (ownership OR role).
	 * 
	 * @param userId         the user ID
	 * @param authentication current authentication context
	 * @return user details
	 */
	@GetMapping("/{userId}")
	@PreAuthorize("@resourceOwnership.isOwnerOrAdmin(authentication, #userId)")
	@Operation(summary = "Get user by ID", description = "Retrieve user details. Users can access their own data, ADMINs can access any user.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied - not owner or admin"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<UserResponse> getUserById(@Parameter(description = "User ID") @PathVariable UUID userId,
			Authentication authentication) {

		log.info("User {} requesting details for user {}", authentication.getName(), userId);

		UserResponse user = userService.getUserById(userId);
		return ResponseEntity.ok(user);
	}

	/**
	 * Update user enabled status.
	 * 
	 * Authorization: Only users with USER_WRITE permission can update user status.
	 * This demonstrates permission-based authorization.
	 * 
	 * @param userId         the user ID
	 * @param enabled        the new enabled status
	 * @param authentication current authentication context
	 * @return success response
	 */
	@PatchMapping("/{userId}/enabled")
	@PreAuthorize("hasAuthority('USER_WRITE')")
	@Operation(summary = "Update user enabled status", description = "Enable or disable a user account. Requires USER_WRITE permission.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "User status updated successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied - USER_WRITE permission required"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<Void> updateUserEnabledStatus(@Parameter(description = "User ID") @PathVariable UUID userId,
			@Parameter(description = "Enabled status") @RequestParam boolean enabled, Authentication authentication) {

		log.info("User {} updating enabled status for user {} to {}", authentication.getName(), userId, enabled);

		userService.updateEnabledStatus(userId, enabled);
		return ResponseEntity.ok().build();
	}

	/**
	 * Delete user.
	 * 
	 * Authorization: Only ADMIN users can delete users. This demonstrates
	 * role-based authorization for destructive operations.
	 * 
	 * @param userId         the user ID to delete
	 * @param authentication current authentication context
	 * @return success response
	 */
	@DeleteMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete user", description = "Delete a user account. Only accessible by ADMIN users.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "User deleted successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable UUID userId,
			Authentication authentication) {

		log.warn("Admin {} deleting user {}", authentication.getName(), userId);
		userService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Get current user profile.
	 * 
	 * Authorization: Any authenticated user can access their own profile. This
	 * demonstrates basic authentication requirement.
	 * 
	 * @param authentication current authentication context
	 * @return current user's profile
	 */
	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get current user profile", description = "Retrieve the profile of the currently authenticated user.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required") })
	public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication authentication) {
		log.info("User {} requesting own profile", authentication.getName());

		UserResponse user = userService.getCurrentUserProfile(authentication);
		return ResponseEntity.ok(user);
	}

	/**
	 * Search users by username or email.
	 * 
	 * Authorization: Users with USER_READ permission can search users. This
	 * demonstrates permission-based authorization for search operations.
	 * 
	 * @param searchTerm     the search term
	 * @param pageable       pagination parameters
	 * @param authentication current authentication context
	 * @return paginated search results
	 */
	@GetMapping("/search")
	@PreAuthorize("hasAuthority('USER_READ')")
	@Operation(summary = "Search users", description = "Search users by username or email. Requires USER_READ permission.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Search completed successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied - USER_READ permission required") })
	public ResponseEntity<Page<UserResponse>> searchUsers(
			@Parameter(description = "Search term") @RequestParam String searchTerm,
			@PageableDefault(size = 20) Pageable pageable, Authentication authentication) {

		log.info("User {} searching for users with term: {}", authentication.getName(), searchTerm);

		Page<UserResponse> users = userService.searchUsers(searchTerm, pageable);
		return ResponseEntity.ok(users);
	}

	/**
	 * Get users by role.
	 * 
	 * Authorization: Only ADMIN or MANAGER users can query users by role. This
	 * demonstrates multiple role authorization.
	 * 
	 * @param roleName       the role name to filter by
	 * @param pageable       pagination parameters
	 * @param authentication current authentication context
	 * @return paginated list of users with the specified role
	 */
	@GetMapping("/by-role/{roleName}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Get users by role", description = "Retrieve users with a specific role. Accessible by ADMIN and MANAGER users.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied - ADMIN or MANAGER role required") })
	public ResponseEntity<Page<UserResponse>> getUsersByRole(
			@Parameter(description = "Role name") @PathVariable String roleName,
			@PageableDefault(size = 20) Pageable pageable, Authentication authentication) {

		log.info("User {} requesting users with role: {}", authentication.getName(), roleName);

		Page<UserResponse> users = userService.getUsersByRole(roleName, pageable);
		return ResponseEntity.ok(users);
	}

	/**
	 * Update user account locked status.
	 * 
	 * Authorization: Combined permission and role check. User must have USER_WRITE
	 * permission AND (be ADMIN or MANAGER). This demonstrates complex authorization
	 * expressions.
	 * 
	 * @param userId         the user ID
	 * @param locked         the new locked status
	 * @param authentication current authentication context
	 * @return success response
	 */
	@PatchMapping("/{userId}/locked")
	@PreAuthorize("hasAuthority('USER_WRITE') and hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Update user locked status", description = "Lock or unlock a user account. Requires USER_WRITE permission and ADMIN or MANAGER role.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "User lock status updated successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<Void> updateUserLockedStatus(@Parameter(description = "User ID") @PathVariable UUID userId,
			@Parameter(description = "Locked status") @RequestParam boolean locked, Authentication authentication) {

		log.info("User {} updating locked status for user {} to {}", authentication.getName(), userId, locked);

		userService.updateLockedStatus(userId, locked);
		return ResponseEntity.ok().build();
	}
}
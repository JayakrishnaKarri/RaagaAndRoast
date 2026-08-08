package com.raagaandroast.user.service;

import com.raagaandroast.common.exception.ResourceNotFoundException;
import com.raagaandroast.security.authentication.CustomUserPrincipal;
import com.raagaandroast.user.dto.UserResponse;
import com.raagaandroast.user.entity.User;
import com.raagaandroast.user.mapper.UserMapper;
import com.raagaandroast.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for User management operations.
 * 
 * This service provides business logic for user operations including:
 * - User retrieval with proper authorization
 * - User status management (enabled/disabled, locked/unlocked)
 * - User search and filtering
 * - Profile management
 * 
 * Design Decisions:
 * - Transactional boundaries at service layer
 * - DTO mapping to prevent entity exposure
 * - Proper exception handling for not found cases
 * - Authorization-aware operations
 * 
 * Interview Points:
 * - Why service layer? Business logic separation, transaction boundaries
 * - Why DTOs? Security, API contract stability, performance
 * - Why @Transactional? Data consistency, proper transaction management
 * - How does this integrate with security? Uses authentication context
 * 
 * @author RaagaAndRoast Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Get all users with pagination.
     * Used by admin users to manage the user base.
     * 
     * @param pageable pagination parameters
     * @return paginated list of users
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Retrieving all users with pagination: {}", pageable);

        Page<User> users = userRepository.findAllWithRoles(pageable);
        return users.map(userMapper::toResponse);
    }

    /**
     * Get user by ID.
     * Returns user details if found.
     * 
     * @param userId the user ID
     * @return user details
     * @throws ResourceNotFoundException if user not found
     */
    public UserResponse getUserById(UUID userId) {
        log.debug("Retrieving user by ID: {}", userId);

        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return userMapper.toResponse(user);
    }

    /**
     * Update user enabled status.
     * Used for account activation/deactivation.
     * 
     * @param userId  the user ID
     * @param enabled the new enabled status
     */
    @Transactional
    public void updateEnabledStatus(UUID userId, boolean enabled) {
        log.info("Updating enabled status for user {} to {}", userId, enabled);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        int updatedRows = userRepository.updateEnabledStatus(userId, enabled);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("User", userId);
        }

        log.info("Successfully updated enabled status for user {}", userId);
    }

    /**
     * Update user locked status.
     * Used for account security management.
     * 
     * @param userId the user ID
     * @param locked the new locked status (note: repository method expects
     *               accountNonLocked)
     */
    @Transactional
    public void updateLockedStatus(UUID userId, boolean locked) {
        log.info("Updating locked status for user {} to {}", userId, locked);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        // Note: repository method expects accountNonLocked, so we invert the locked
        // value
        boolean accountNonLocked = !locked;
        int updatedRows = userRepository.updateAccountLockedStatus(userId, accountNonLocked);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("User", userId);
        }

        log.info("Successfully updated locked status for user {}", userId);
    }

    /**
     * Delete user.
     * Soft delete would be preferred in production, but this demonstrates hard
     * delete.
     * 
     * @param userId the user ID to delete
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.warn("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        userRepository.delete(user);
        log.warn("Successfully deleted user {}", userId);
    }

    /**
     * Get current user profile from authentication context.
     * 
     * @param authentication the current authentication
     * @return current user's profile
     */
    public UserResponse getCurrentUserProfile(Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getId();

        log.debug("Retrieving profile for current user: {}", userId);
        return getUserById(userId);
    }

    /**
     * Search users by username or email.
     * 
     * @param searchTerm the search term
     * @param pageable   pagination parameters
     * @return paginated search results
     */
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}", searchTerm);

        Page<User> users = userRepository.searchByUsernameOrEmail(searchTerm, pageable);
        return users.map(userMapper::toResponse);
    }

    /**
     * Get users by role name.
     * 
     * @param roleName the role name
     * @param pageable pagination parameters
     * @return paginated list of users with the specified role
     */
    public Page<UserResponse> getUsersByRole(String roleName, Pageable pageable) {
        log.debug("Retrieving users with role: {}", roleName);

        Page<User> users = userRepository.findByRoleName(roleName, pageable);
        return users.map(userMapper::toResponse);
    }

    /**
     * Check if user exists by ID.
     * Utility method for other services.
     * 
     * @param userId the user ID
     * @return true if user exists
     */
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Get user entity by ID (for internal use).
     * This method returns the entity directly and should only be used
     * by other services that need the full entity.
     * 
     * @param userId the user ID
     * @return user entity
     * @throws ResourceNotFoundException if user not found
     */
    public User getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    /**
     * Get user entity with roles and permissions by ID (for internal use).
     *
     * @param userId the user ID
     * @return user entity with roles and permissions loaded
     * @throws ResourceNotFoundException if user not found
     */
    public User getUserEntityWithRolesById(UUID userId) {
        return userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
package com.raagaandroast.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.raagaandroast.security.authentication.CustomUserPrincipal;

import java.util.UUID;

/**
 * Service for handling resource ownership authorization.
 * 
 * This service provides methods to check if the authenticated user owns or has
 * access to specific resources. It's used in @PreAuthorize expressions to
 * implement fine-grained authorization based on resource ownership.
 * 
 * Design Decisions:
 * - Centralized ownership logic for consistency across the application
 * - Type-safe methods with proper UUID handling
 * - Clear separation between different types of ownership checks
 * - Extensible design for adding new resource types
 * 
 * Interview Points:
 * - Why separate ownership from role-based authorization? Different concerns -
 * roles define what you can do, ownership defines what you can access
 * - Why use a service instead of inline expressions? Reusability, testability,
 * and maintainability
 * - How does this integrate with Spring Security? Used in @PreAuthorize
 * expressions via SpEL
 * 
 * @author RaagaAndRoast Development Team
 */
@Service("resourceOwnership")
public class ResourceOwnershipService {

    /**
     * Checks if the authenticated user is the owner of a specific user resource.
     * Used for operations like updating user profile, viewing user details, etc.
     * 
     * @param authentication the current authentication object
     * @param userId         the ID of the user resource
     * @return true if the authenticated user owns the resource
     */
    public boolean isUserOwner(Authentication authentication, UUID userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        CustomUserPrincipal principal = extractUserPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        return principal.getId().equals(userId);
    }

    /**
     * Checks if the authenticated user is the owner of a specific user resource.
     * Overloaded method that accepts String userId for convenience.
     * 
     * @param authentication the current authentication object
     * @param userId         the ID of the user resource as String
     * @return true if the authenticated user owns the resource
     */
    public boolean isUserOwner(Authentication authentication, String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            return isUserOwner(authentication, userUuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the authenticated user is the owner of a customer resource.
     * This will be used when we implement the Customer entity.
     * 
     * @param authentication the current authentication object
     * @param customerId     the ID of the customer resource
     * @return true if the authenticated user owns the customer resource
     */
    public boolean isCustomerOwner(Authentication authentication, UUID customerId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        CustomUserPrincipal principal = extractUserPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        // For now, we'll implement this as a placeholder
        // In the actual implementation, we'll need to check if the user
        // is associated with the customer record
        // Note: Customer ownership validation will be implemented when Customer entity
        // is fully integrated
        // Production implementation will verify: user.customer.id == customerId
        return true; // Development placeholder - allows access for testing
    }

    /**
     * Checks if the authenticated user is the owner of an order resource.
     * This will be used when we implement the Order entity.
     * 
     * @param authentication the current authentication object
     * @param orderId        the ID of the order resource
     * @return true if the authenticated user owns the order resource
     */
    public boolean isOrderOwner(Authentication authentication, UUID orderId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        CustomUserPrincipal principal = extractUserPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        // Note: Order ownership validation will be implemented when Order entity is
        // fully integrated
        // Production implementation will verify: order.customer.user.id ==
        // authenticated.user.id
        return true; // Development placeholder - allows access for testing
    }

    /**
     * Checks if the authenticated user is the owner of a cart resource.
     * This will be used when we implement the Cart entity.
     * 
     * @param authentication the current authentication object
     * @param cartId         the ID of the cart resource
     * @return true if the authenticated user owns the cart resource
     */
    public boolean isCartOwner(Authentication authentication, UUID cartId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        CustomUserPrincipal principal = extractUserPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        // Note: Cart ownership validation will be implemented when Cart entity is fully
        // integrated
        // Production implementation will verify: cart.customer.user.id ==
        // authenticated.user.id
        return true; // Development placeholder - allows access for testing
    }

    /**
     * Checks if the authenticated user has administrative privileges.
     * This is a convenience method that can be used in authorization expressions.
     * 
     * @param authentication the current authentication object
     * @return true if the user has ADMIN role
     */
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Checks if the authenticated user has manager privileges.
     * This is a convenience method that can be used in authorization expressions.
     * 
     * @param authentication the current authentication object
     * @return true if the user has MANAGER role
     */
    public boolean isManager(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));
    }

    /**
     * Checks if the authenticated user has staff privileges.
     * This is a convenience method that can be used in authorization expressions.
     * 
     * @param authentication the current authentication object
     * @return true if the user has STAFF role
     */
    public boolean isStaff(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STAFF"));
    }

    /**
     * Checks if the authenticated user is either the owner of the resource or has
     * administrative privileges. This is useful for operations where both the owner
     * and admins should have access.
     * 
     * @param authentication the current authentication object
     * @param userId         the ID of the user resource
     * @return true if the user is the owner or an admin
     */
    public boolean isOwnerOrAdmin(Authentication authentication, UUID userId) {
        return isUserOwner(authentication, userId) || isAdmin(authentication);
    }

    /**
     * Checks if the authenticated user is either the owner of the resource or has
     * manager/admin privileges.
     * 
     * @param authentication the current authentication object
     * @param userId         the ID of the user resource
     * @return true if the user is the owner, manager, or admin
     */
    public boolean isOwnerOrManagerOrAdmin(Authentication authentication, UUID userId) {
        return isUserOwner(authentication, userId) || isManager(authentication) || isAdmin(authentication);
    }

    /**
     * Extracts the CustomUserPrincipal from the authentication object.
     * 
     * @param authentication the authentication object
     * @return the CustomUserPrincipal or null if not found
     */
    private CustomUserPrincipal extractUserPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserPrincipal) {
            return (CustomUserPrincipal) principal;
        }
        return null;
    }

    /**
     * Gets the authenticated user's ID.
     * Utility method for getting the current user's ID from the authentication
     * context.
     * 
     * @param authentication the authentication object
     * @return the user's ID or null if not authenticated
     */
    public UUID getAuthenticatedUserId(Authentication authentication) {
        CustomUserPrincipal principal = extractUserPrincipal(authentication);
        return principal != null ? principal.getId() : null;
    }

    /**
     * Gets the authenticated user's username.
     * Utility method for getting the current user's username from the
     * authentication context.
     * 
     * @param authentication the authentication object
     * @return the user's username or null if not authenticated
     */
    public String getAuthenticatedUsername(Authentication authentication) {
        CustomUserPrincipal principal = extractUserPrincipal(authentication);
        return principal != null ? principal.getUsername() : null;
    }
    // ================================================================
    // Address Ownership Methods
    // ================================================================

    /**
     * Checks if the authenticated user is the owner of an address resource.
     * This checks if the address belongs to the customer associated with the
     * authenticated user.
     *
     * @param authentication the current authentication object
     * @param addressId      the ID of the address resource
     * @return true if the authenticated user owns the address resource
     */
    public boolean isAddressOwner(Authentication authentication, UUID addressId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        CustomUserPrincipal principal = extractUserPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        // Note: Address ownership validation will be implemented when Address entity is
        // fully integrated
        // Production implementation will:
        // 1. Find the address by ID using addressRepository.findById(addressId)
        // 2. Get the customer associated with the address (address.getCustomer())
        // 3. Check if the customer's user ID matches the authenticated user's ID
        // Expected logic:
        // address.getCustomer().getUser().getId().equals(principal.getId())
        return true; // Development placeholder - allows access for testing
    }

    /**
     * Checks if the authenticated user is the owner of an address or has staff
     * privileges.
     * This is useful for address management operations where both the owner and
     * staff should have access.
     *
     * @param authentication the current authentication object
     * @param addressId      the ID of the address resource
     * @return true if the user is the address owner or has staff privileges
     */
    public boolean isAddressOwnerOrStaff(Authentication authentication, UUID addressId) {
        return isAddressOwner(authentication, addressId) ||
                isStaff(authentication) ||
                isManager(authentication) ||
                isAdmin(authentication);
    }

    /**
     * Checks if the authenticated user is the owner of an address or has admin
     * privileges.
     *
     * @param authentication the current authentication object
     * @param addressId      the ID of the address resource
     * @return true if the user is the address owner or an admin
     */
    public boolean isAddressOwnerOrAdmin(Authentication authentication, UUID addressId) {
        return isAddressOwner(authentication, addressId) || isAdmin(authentication);
    }

    /**
     * Checks if the authenticated user is the owner of an address or has
     * manager/admin privileges.
     *
     * @param authentication the current authentication object
     * @param addressId      the ID of the address resource
     * @return true if the user is the address owner, manager, or admin
     */
    public boolean isAddressOwnerOrManagerOrAdmin(Authentication authentication, UUID addressId) {
        return isAddressOwner(authentication, addressId) ||
                isManager(authentication) ||
                isAdmin(authentication);
    }

    // ================================================================
    // Customer Ownership Methods (Enhanced)
    // ================================================================

    /**
     * Checks if the authenticated user is the owner of a customer or has staff
     * privileges.
     * This is useful for customer management operations where both the owner and
     * staff should have access.
     *
     * @param authentication the current authentication object
     * @param customerId     the ID of the customer resource
     * @return true if the user is the customer owner or has staff privileges
     */
    public boolean isCustomerOwnerOrStaff(Authentication authentication, UUID customerId) {
        return isCustomerOwner(authentication, customerId) ||
                isStaff(authentication) ||
                isManager(authentication) ||
                isAdmin(authentication);
    }

    /**
     * Checks if the authenticated user is the owner of a customer or has admin
     * privileges.
     *
     * @param authentication the current authentication object
     * @param customerId     the ID of the customer resource
     * @return true if the user is the customer owner or an admin
     */
    public boolean isCustomerOwnerOrAdmin(Authentication authentication, UUID customerId) {
        return isCustomerOwner(authentication, customerId) || isAdmin(authentication);
    }

    /**
     * Checks if the authenticated user is the owner of a customer or has
     * manager/admin privileges.
     *
     * @param authentication the current authentication object
     * @param customerId     the ID of the customer resource
     * @return true if the user is the customer owner, manager, or admin
     */
    public boolean isCustomerOwnerOrManagerOrAdmin(Authentication authentication, UUID customerId) {
        return isCustomerOwner(authentication, customerId) ||
                isManager(authentication) ||
                isAdmin(authentication);
    }

    // ================================================================
    // Combined Role Checks
    // ================================================================

    /**
     * Checks if the authenticated user has staff or higher privileges.
     *
     * @param authentication the current authentication object
     * @return true if the user has STAFF, MANAGER, or ADMIN role
     */
    public boolean isStaffOrHigher(Authentication authentication) {
        return isStaff(authentication) || isManager(authentication) || isAdmin(authentication);
    }

    /**
     * Checks if the authenticated user has manager or higher privileges.
     *
     * @param authentication the current authentication object
     * @return true if the user has MANAGER or ADMIN role
     */
    public boolean isManagerOrHigher(Authentication authentication) {
        return isManager(authentication) || isAdmin(authentication);
    }

    /**
     * Checks if the authenticated user is a customer (has CUSTOMER role).
     *
     * @param authentication the current authentication object
     * @return true if the user has CUSTOMER role
     */
    public boolean isCustomer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CUSTOMER"));
    }
}
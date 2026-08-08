package com.raagaandroast.security.authentication;

import com.raagaandroast.user.entity.Permission;
import com.raagaandroast.user.entity.Role;
import com.raagaandroast.user.entity.User;
import com.raagaandroast.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * 
 * This service: - Loads user details from the database - Converts User entity
 * to Spring Security UserDetails - Loads user roles and permissions as
 * authorities - Provides proper authentication context
 * 
 * Key Features: - Transactional user loading with roles and permissions -
 * Proper authority mapping (roles + permissions) - Account status checking
 * (enabled/disabled) - Performance optimization with EntityGraph
 * 
 * Authority Structure: - Roles are prefixed with "ROLE_" (Spring Security
 * convention) - Permissions are used as-is for fine-grained authorization -
 * Both are available for @PreAuthorize expressions
 * 
 * @author RaagaAndRoast Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	/**
	 * Loads user details by username for authentication.
	 * 
	 * This method: 1. Finds user by username with roles and permissions loaded 2.
	 * Validates user exists and is enabled 3. Converts to Spring Security
	 * UserDetails 4. Maps roles and permissions to authorities
	 * 
	 * @param username the username to load
	 * @return UserDetails for Spring Security
	 * @throws UsernameNotFoundException if user not found or disabled
	 */
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.debug("Loading user details for username: {}", username);

		// Load user with roles and permissions in a single query
		User user = userRepository.findByUsernameWithRolesAndPermissions(username).orElseThrow(() -> {
			log.warn("User not found: {}", username);
			return new UsernameNotFoundException("User not found: " + username);
		});

		// Check if user account is enabled
		if (!user.isEnabled()) {
			log.warn("User account is disabled: {}", username);
			throw new UsernameNotFoundException("User account is disabled: " + username);
		}

		// Build authorities from roles and permissions
		Collection<? extends GrantedAuthority> authorities = buildAuthorities(user);

		log.debug("Loaded user {} with {} authorities", username, authorities.size());

		// Create CustomUserPrincipal with user information
		return new CustomUserPrincipal(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getPassword(),
				user.isEnabled(),
				user.isAccountNonExpired(),
				user.isAccountNonLocked(),
				user.isCredentialsNonExpired(),
				authorities);
	}

	/**
	 * Builds Spring Security authorities from user roles and permissions.
	 * 
	 * Authority Structure: - Roles: "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CUSTOMER" -
	 * Permissions: "USER_READ", "MENU_WRITE", "ORDER_DELETE"
	 * 
	 * This dual approach allows: - Role-based
	 * authorization: @PreAuthorize("hasRole('ADMIN')") - Permission-based
	 * authorization: @PreAuthorize("hasAuthority('USER_WRITE')") - Combined
	 * authorization: @PreAuthorize("hasRole('MANAGER') and
	 * hasAuthority('MENU_WRITE')")
	 * 
	 * @param user the user entity with loaded roles and permissions
	 * @return collection of granted authorities
	 */
	private Collection<GrantedAuthority> buildAuthorities(User user) {
		Set<GrantedAuthority> authorities = new HashSet<>();

		// Add role-based authorities (prefixed with ROLE_)
		for (Role role : user.getRoles()) {
			String roleAuthority = "ROLE_" + role.getName();
			authorities.add(new SimpleGrantedAuthority(roleAuthority));
			log.trace("Added role authority: {}", roleAuthority);

			// Add permission-based authorities from each role
			for (Permission permission : role.getPermissions()) {
				String permissionAuthority = permission.getName();
				authorities.add(new SimpleGrantedAuthority(permissionAuthority));
				log.trace("Added permission authority: {}", permissionAuthority);
			}
		}

		return authorities;
	}

	/**
	 * Loads user details by user ID. Used for JWT authentication where we have the
	 * user ID from the token.
	 * 
	 * @param userId the user ID
	 * @return UserDetails for Spring Security
	 * @throws UsernameNotFoundException if user not found or disabled
	 */
	@Transactional(readOnly = true)
	public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
		log.debug("Loading user details for user ID: {}", userId);

		try {
			java.util.UUID userUuid = java.util.UUID.fromString(userId);

			User user = userRepository.findByIdWithRolesAndPermissions(userUuid).orElseThrow(() -> {
				log.warn("User not found with ID: {}", userId);
				return new UsernameNotFoundException("User not found with ID: " + userId);
			});

			if (!user.isEnabled()) {
				log.warn("User account is disabled for ID: {}", userId);
				throw new UsernameNotFoundException("User account is disabled for ID: " + userId);
			}

			Collection<? extends GrantedAuthority> authorities = buildAuthorities(user);

			log.debug("Loaded user {} with {} authorities", user.getUsername(), authorities.size());

			return new CustomUserPrincipal(
					user.getId(),
					user.getUsername(),
					user.getEmail(),
					user.getPassword(),
					user.isEnabled(),
					user.isAccountNonExpired(),
					user.isAccountNonLocked(),
					user.isCredentialsNonExpired(),
					authorities);

		} catch (IllegalArgumentException e) {
			log.warn("Invalid user ID format: {}", userId);
			throw new UsernameNotFoundException("Invalid user ID format: " + userId);
		}
	}
}
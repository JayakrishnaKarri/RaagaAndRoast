package com.raagaandroast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing Configuration.
 *
 * This configuration provides AuditorAware implementation for JPA auditing.
 * The actual @EnableJpaAuditing is configured in JpaConfig to avoid
 * bean definition conflicts.
 *
 * This configuration enables automatic auditing of JPA entities with:
 * - @CreatedDate - Automatically sets creation timestamp
 * - @LastModifiedDate - Automatically sets modification timestamp
 * - @CreatedBy - Automatically sets creator username
 * - @LastModifiedBy - Automatically sets last modifier username
 *
 * Design Decisions:
 * - Uses Spring Security context to determine current user
 * - Graceful handling of unauthenticated operations (system operations)
 * - Thread-safe implementation for concurrent operations
 * - Production-ready configuration with proper error handling
 *
 * Interview Points:
 * - Why JPA auditing? Automatic tracking of entity lifecycle events
 * - Why AuditorAware? Provides current user context for audit fields
 * - Why Optional? Handles cases where no user is authenticated
 * - Why SecurityContext? Integrates with Spring Security authentication
 *
 * Audit Trail Benefits:
 * - Compliance and regulatory requirements
 * - Debugging and troubleshooting support
 * - Security monitoring and forensics
 * - Data governance and change tracking
 *
 * Usage Examples:
 * - Order creation: tracks who created the order and when
 * - Menu item updates: tracks who modified prices and when
 * - Customer data changes: tracks modifications for compliance
 * - System operations: handles automated processes gracefully
 *
 * @author RaagaAndRoast Development Team
 */
@Configuration
public class JpaAuditingConfig {

	/**
	 * Provides the current auditor (user) for JPA auditing.
	 * 
	 * This bean is used by JPA auditing to populate @CreatedBy and
	 * 
	 * @LastModifiedBy fields automatically.
	 * 
	 * @return AuditorAware implementation
	 */
	@Bean
	AuditorAware<String> auditorProvider() {
		return new SpringSecurityAuditorAware();
	}

	/**
	 * Spring Security-based auditor aware implementation.
	 * 
	 * This class integrates JPA auditing with Spring Security to automatically
	 * track which user performed entity operations.
	 */
	public static class SpringSecurityAuditorAware implements AuditorAware<String> {

		/**
		 * Gets the current auditor from Spring Security context.
		 * 
		 * Returns the username of the currently authenticated user, or "system" for
		 * unauthenticated operations.
		 * 
		 * @return Optional containing the current auditor
		 */
		@Override
		public Optional<String> getCurrentAuditor() {
			try {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

				if (authentication == null || !authentication.isAuthenticated()) {
					// Handle unauthenticated operations (system operations, startup, etc.)
					return Optional.of("system");
				}

				String username = authentication.getName();

				// Handle anonymous users
				if ("anonymousUser".equals(username)) {
					return Optional.of("anonymous");
				}

				return Optional.of(username);

			} catch (Exception e) {
				// Fallback for any security context issues
				return Optional.of("system");
			}
		}
	}
}
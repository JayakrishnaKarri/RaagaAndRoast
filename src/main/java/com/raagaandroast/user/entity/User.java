package com.raagaandroast.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User entity representing system users with authentication and authorization
 * capabilities.
 * 
 * This entity demonstrates several advanced JPA concepts:
 * - UUID primary keys for better distributed system support
 * - Optimistic locking with @Version for concurrent access control
 * - JPA Auditing with @CreatedDate and @LastModifiedDate
 * - Many-to-Many relationship with Role entity
 * - Proper validation constraints
 * - Security considerations (password never exposed in toString/equals)
 * 
 * Design Decisions:
 * - UUID instead of Long: Better for microservices, no sequence conflicts,
 * security through obscurity
 * - Optimistic locking: Handles concurrent updates gracefully without
 * pessimistic locks
 * - Separate enabled flags: Allows fine-grained account control (Spring
 * Security UserDetails)
 * - Lazy loading for roles: Prevents N+1 queries, roles loaded only when needed
 * 
 * Interview Points:
 * - Why UUID over Long? Distributed systems, security, no central sequence
 * - Why optimistic locking? Better performance than pessimistic, handles most
 * concurrent scenarios
 * - Why separate User from Customer? Clean separation of authentication vs
 * business concerns
 * 
 * @author RaagaAndRoast Development Team
 */

/**
 * User entity representing system users with authentication and authorization
 * capabilities.
 *
 * FIX 1 — @Data replaced with @Getter + @Setter.
 * 
 * @Data generates equals/hashCode/toString that traverse ALL fields, including
 *       the bidirectional `roles` collection. That causes Role.equals() to call
 *       User.equals() which calls Role.equals() → StackOverflowError at
 *       runtime. The hand-written overrides below are only safe because @Data
 *       is gone; with @Data present, Lombok silently overwrites them at compile
 *       time.
 */
@Entity
@Table(name = "users", indexes = { @Index(name = "idx_users_username", columnList = "username"),
		@Index(name = "idx_users_email", columnList = "email"),
		@Index(name = "idx_users_enabled", columnList = "enabled"),
		@Index(name = "idx_users_created_at", columnList = "created_at") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
	@Column(name = "username", unique = true, nullable = false, length = 50)
	private String username;

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Size(max = 100, message = "Email must not exceed 100 characters")
	@Column(name = "email", unique = true, nullable = false, length = 100)
	private String email;

	/**
	 * BCrypt hashed password — never exposed in toString, equals, or logs.
	 */
	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	@Column(name = "password", nullable = false)
	private String password;

	@Builder.Default
	@Column(name = "enabled", nullable = false)
	private Boolean enabled = true;

	@Builder.Default
	@Column(name = "account_non_expired", nullable = false)
	private Boolean accountNonExpired = true;

	@Builder.Default
	@Column(name = "account_non_locked", nullable = false)
	private Boolean accountNonLocked = true;

	@Builder.Default
	@Column(name = "credentials_non_expired", nullable = false)
	private Boolean credentialsNonExpired = true;

	/**
	 * Many-to-Many with Role (owning side — defines the join table). LAZY: roles
	 * loaded only when explicitly accessed or via @EntityGraph.
	 * CascadeType.PERSIST/MERGE: allows creating/updating roles alongside users. No
	 * CascadeType.REMOVE: roles are shared and must not be deleted with a user.
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"), indexes = {
			@Index(name = "idx_user_roles_user_id", columnList = "user_id"),
			@Index(name = "idx_user_roles_role_id", columnList = "role_id") })
	@Builder.Default
	private Set<Role> roles = new HashSet<>();

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	// ================================================================
	// Business Methods
	// ================================================================

	public void addRole(Role role) {
		if (role != null) {
			this.roles.add(role);
			role.getUsers().add(this);
		}
	}

	public void removeRole(Role role) {
		if (role != null) {
			this.roles.remove(role);
			role.getUsers().remove(this);
		}
	}

	public boolean hasRole(String roleName) {
		return roles.stream().anyMatch(r -> r.getName().equals(roleName));
	}

	public Set<String> getAllPermissions() {
		return roles.stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getName)
				.collect(Collectors.toSet());
	}

	// Spring Security UserDetails compatibility helpers
	public boolean isEnabled() {
		return Boolean.TRUE.equals(enabled);
	}

	public boolean isAccountNonExpired() {
		return Boolean.TRUE.equals(accountNonExpired);
	}

	public boolean isAccountNonLocked() {
		return Boolean.TRUE.equals(accountNonLocked);
	}

	public boolean isCredentialsNonExpired() {
		return Boolean.TRUE.equals(credentialsNonExpired);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof User user))
			return false;
		return username != null && username.equals(user.username);
	}

	@Override
	public int hashCode() {
		return username != null ? username.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "User{" + "id=" + id + ", username='" + username + '\'' + ", email='" + email + '\'' + ", enabled="
				+ enabled + ", accountNonExpired=" + accountNonExpired + ", accountNonLocked=" + accountNonLocked
				+ ", credentialsNonExpired=" + credentialsNonExpired + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + ", version=" + version + '}';
	}
}
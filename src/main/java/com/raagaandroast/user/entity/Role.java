package com.raagaandroast.user.entity;

import jakarta.persistence.*;
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

/**
 * Role entity representing user roles in the system for authorization.
 * 
 * This entity demonstrates: - Many-to-Many relationship with User entity
 * (bidirectional) - Many-to-Many relationship with Permission entity - JPA
 * Auditing for creation and modification tracking - Proper indexing for
 * performance - Business methods for role management
 * 
 * Design Decisions: - UUID primary key: Consistent with User entity, better for
 * distributed systems - Bidirectional relationships: Allows navigation from
 * both sides - LAZY loading: Prevents N+1 queries, load collections only when
 * needed - No cascade DELETE: Users and Permissions are independent entities
 * 
 * Role Hierarchy in RaagaAndRoast: - ADMIN: Full system access, user management
 * - MANAGER: Menu management, order oversight, reporting - STAFF: Order
 * processing, customer support - CUSTOMER: Menu browsing, ordering, account
 * management
 * 
 * Interview Points: - Why separate Role from Permission? Flexibility - roles
 * can have different permission sets - Why bidirectional relationships? Easier
 * querying from both sides - Why LAZY loading? Performance - load related data
 * only when needed
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "roles", indexes = { @Index(name = "idx_roles_name", columnList = "name") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

	/**
	 * Primary key using UUID for consistency with other entities.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	/**
	 * Unique role name (e.g., "ADMIN", "MANAGER", "STAFF", "CUSTOMER"). Indexed for
	 * fast role-based queries.
	 * 
	 * Naming Convention: - Use uppercase for consistency with Spring Security -
	 * Descriptive names that reflect business roles - No "ROLE_" prefix (Spring
	 * Security adds this automatically)
	 */
	@NotBlank(message = "Role name is required")
	@Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
	@Column(name = "name", unique = true, nullable = false, length = 50)
	private String name;

	/**
	 * Human-readable description of the role. Helps administrators understand role
	 * purposes.
	 */
	@Size(max = 255, message = "Description must not exceed 255 characters")
	@Column(name = "description", length = 255)
	private String description;

	/**
	 * Many-to-Many relationship with User entity (inverse side).
	 * 
	 * Design Notes: - mappedBy: This is the inverse side, User entity owns the
	 * relationship - LAZY loading: Users are loaded only when accessed - No
	 * cascade: Users exist independently of roles - fetch = LAZY: Explicit for
	 * clarity, prevents N+1 queries
	 */
	@ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<User> users = new HashSet<>();

	/**
	 * Many-to-Many relationship with Permission entity.
	 * 
	 * Design Notes: - This side owns the relationship (defines the join table) -
	 * LAZY loading: Permissions loaded only when needed - CascadeType.PERSIST: New
	 * permissions can be created with roles - No CascadeType.REMOVE: Permissions
	 * are shared across roles
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"), indexes = {
			@Index(name = "idx_role_permissions_role_id", columnList = "role_id"),
			@Index(name = "idx_role_permissions_permission_id", columnList = "permission_id") })
	@Builder.Default
	private Set<Permission> permissions = new HashSet<>();

	/**
	 * Audit field: When the role was created.
	 */
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * Audit field: When the role was last modified.
	 */
	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	// ================================================================
	// Business Methods
	// ================================================================

	/**
	 * Adds a permission to this role. Maintains bidirectional relationship
	 * consistency.
	 * 
	 * @param permission the permission to add
	 */
	public void addPermission(Permission permission) {
		if (permission != null) {
			this.permissions.add(permission);
			permission.getRoles().add(this);
		}
	}

	/**
	 * Removes a permission from this role. Maintains bidirectional relationship
	 * consistency.
	 * 
	 * @param permission the permission to remove
	 */
	public void removePermission(Permission permission) {
		if (permission != null) {
			this.permissions.remove(permission);
			permission.getRoles().remove(this);
		}
	}

	/**
	 * Checks if this role has a specific permission.
	 * 
	 * @param permissionName the permission name to check
	 * @return true if role has the permission
	 */
	public boolean hasPermission(String permissionName) {
		return permissions.stream().anyMatch(permission -> permission.getName().equals(permissionName));
	}

	/**
	 * Gets all permission names for this role. Useful for Spring Security
	 * authorities.
	 * 
	 * @return set of permission names
	 */
	public Set<String> getPermissionNames() {
		return permissions.stream().map(Permission::getName).collect(java.util.stream.Collectors.toSet());
	}

	/**
	 * Adds a user to this role. Maintains bidirectional relationship consistency.
	 * Note: Usually called from User.addRole() to maintain consistency.
	 * 
	 * @param user the user to add
	 */
	public void addUser(User user) {
		if (user != null) {
			this.users.add(user);
			user.getRoles().add(this);
		}
	}

	/**
	 * Removes a user from this role. Maintains bidirectional relationship
	 * consistency. Note: Usually called from User.removeRole() to maintain
	 * consistency.
	 * 
	 * @param user the user to remove
	 */
	public void removeUser(User user) {
		if (user != null) {
			this.users.remove(user);
			user.getRoles().remove(this);
		}
	}

	/**
	 * Gets the count of users with this role. Useful for administrative reporting.
	 * 
	 * @return number of users with this role
	 */
	public int getUserCount() {
		return users.size();
	}

	// ================================================================
	// Static Factory Methods for Common Roles
	// ================================================================

	/**
	 * Creates an ADMIN role with full system permissions. Factory method for
	 * consistent role creation.
	 * 
	 * @return configured ADMIN role
	 */
	public static Role createAdminRole() {
		return Role.builder().name("ADMIN").description("System administrator with full access").build();
	}

	/**
	 * Creates a MANAGER role with menu and order management permissions.
	 * 
	 * @return configured MANAGER role
	 */
	public static Role createManagerRole() {
		return Role.builder().name("MANAGER").description("Manager with menu and order management access").build();
	}

	/**
	 * Creates a STAFF role with order processing permissions.
	 * 
	 * @return configured STAFF role
	 */
	public static Role createStaffRole() {
		return Role.builder().name("STAFF").description("Staff member with order processing access").build();
	}

	/**
	 * Creates a CUSTOMER role with basic ordering permissions.
	 * 
	 * @return configured CUSTOMER role
	 */
	public static Role createCustomerRole() {
		return Role.builder().name("CUSTOMER").description("Customer with ordering and account management access")
				.build();
	}

	// ================================================================
	// Object Methods
	// ================================================================

	/**
	 * Custom equals based on business key (name) rather than ID. Two roles are
	 * equal if they have the same name.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Role role))
			return false;
		return name != null && name.equals(role.name);
	}

	/**
	 * Custom hashCode based on business key (name) for consistency with equals.
	 */
	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	/**
	 * Custom toString that provides useful information without circular references.
	 */
	@Override
	public String toString() {
		return "Role{" + "id=" + id + ", name='" + name + '\'' + ", description='" + description + '\'' + ", userCount="
				+ (users != null ? users.size() : 0) + ", permissionCount="
				+ (permissions != null ? permissions.size() : 0) + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + '}';
	}
}
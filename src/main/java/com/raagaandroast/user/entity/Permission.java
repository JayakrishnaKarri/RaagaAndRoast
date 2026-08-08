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
 * Permission entity representing fine-grained access control in the system.
 * 
 * This entity demonstrates: - Fine-grained permission model for flexible
 * authorization - Many-to-Many relationship with Role entity (inverse side) -
 * Resource-Action based permission naming convention - JPA Auditing for
 * tracking changes - Proper indexing for performance
 * 
 * Permission Naming Convention: - Format: {RESOURCE}_{ACTION} - Examples:
 * USER_READ, MENU_WRITE, ORDER_STATUS_UPDATE - Resources: USER, MENU, CATEGORY,
 * ORDER, CART, REPORT - Actions: READ, WRITE, DELETE, STATUS_UPDATE
 * 
 * Design Decisions: - Separate resource and action fields: Allows flexible
 * querying - UUID primary key: Consistent with other entities - Inverse side of
 * Role relationship: Role owns the relationship - No cascade operations:
 * Permissions are independent entities
 * 
 * Interview Points: - Why separate Permission from Role? Flexibility - same
 * permission can be in multiple roles - Why resource-action model? Granular
 * control, easier to understand and manage - Why not enum? Database-driven
 * permissions allow runtime changes without code deployment
 * 
 * @author RaagaAndRoast Development Team
 */
@Entity
@Table(name = "permissions", indexes = { @Index(name = "idx_permissions_name", columnList = "name"),
		@Index(name = "idx_permissions_resource", columnList = "resource"),
		@Index(name = "idx_permissions_action", columnList = "action"),
		@Index(name = "idx_permissions_resource_action", columnList = "resource, action") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

	/**
	 * Primary key using UUID for consistency with other entities.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	/**
	 * Unique permission name following RESOURCE_ACTION convention. Examples:
	 * USER_READ, MENU_WRITE, ORDER_STATUS_UPDATE
	 * 
	 * Indexed for fast permission checks during authorization.
	 */
	@NotBlank(message = "Permission name is required")
	@Size(min = 3, max = 100, message = "Permission name must be between 3 and 100 characters")
	@Column(name = "name", unique = true, nullable = false, length = 100)
	private String name;

	/**
	 * Human-readable description of what this permission allows. Helps
	 * administrators understand permission purposes.
	 */
	@Size(max = 255, message = "Description must not exceed 255 characters")
	@Column(name = "description", length = 255)
	private String description;

	/**
	 * Resource that this permission applies to. Examples: USER, MENU, CATEGORY,
	 * ORDER, CART, REPORT
	 * 
	 * Indexed for resource-based permission queries.
	 */
	@NotBlank(message = "Resource is required")
	@Size(min = 2, max = 50, message = "Resource must be between 2 and 50 characters")
	@Column(name = "resource", nullable = false, length = 50)
	private String resource;

	/**
	 * Action that this permission allows on the resource. Examples: READ, WRITE,
	 * DELETE, STATUS_UPDATE
	 * 
	 * Indexed for action-based permission queries.
	 */
	@NotBlank(message = "Action is required")
	@Size(min = 2, max = 50, message = "Action must be between 2 and 50 characters")
	@Column(name = "action", nullable = false, length = 50)
	private String action;

	/**
	 * Many-to-Many relationship with Role entity (inverse side).
	 * 
	 * Design Notes: - mappedBy: Role entity owns the relationship - LAZY loading:
	 * Roles are loaded only when accessed - No cascade: Roles exist independently
	 * of permissions
	 */
	@ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<Role> roles = new HashSet<>();

	/**
	 * Audit field: When the permission was created.
	 */
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * Audit field: When the permission was last modified.
	 */
	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	// ================================================================
	// Business Methods
	// ================================================================

	/**
	 * Adds a role to this permission. Maintains bidirectional relationship
	 * consistency. Note: Usually called from Role.addPermission() to maintain
	 * consistency.
	 * 
	 * @param role the role to add
	 */
	public void addRole(Role role) {
		if (role != null) {
			this.roles.add(role);
			role.getPermissions().add(this);
		}
	}

	/**
	 * Removes a role from this permission. Maintains bidirectional relationship
	 * consistency. Note: Usually called from Role.removePermission() to maintain
	 * consistency.
	 * 
	 * @param role the role to remove
	 */
	public void removeRole(Role role) {
		if (role != null) {
			this.roles.remove(role);
			role.getPermissions().remove(this);
		}
	}

	/**
	 * Gets the count of roles that have this permission. Useful for administrative
	 * reporting.
	 * 
	 * @return number of roles with this permission
	 */
	public int getRoleCount() {
		return roles.size();
	}

	/**
	 * Checks if this permission applies to a specific resource and action.
	 * 
	 * @param resource the resource to check
	 * @param action   the action to check
	 * @return true if this permission matches the resource and action
	 */
	public boolean appliesTo(String resource, String action) {
		return this.resource.equals(resource) && this.action.equals(action);
	}

	// ================================================================
	// Static Factory Methods for Common Permissions
	// ================================================================

	/**
	 * Creates a READ permission for a resource.
	 * 
	 * @param resource    the resource name
	 * @param description human-readable description
	 * @return configured READ permission
	 */
	public static Permission createReadPermission(String resource, String description) {
		return Permission.builder().name(resource + "_READ").description(description).resource(resource).action("READ")
				.build();
	}

	/**
	 * Creates a WRITE permission for a resource.
	 * 
	 * @param resource    the resource name
	 * @param description human-readable description
	 * @return configured WRITE permission
	 */
	public static Permission createWritePermission(String resource, String description) {
		return Permission.builder().name(resource + "_WRITE").description(description).resource(resource)
				.action("WRITE").build();
	}

	/**
	 * Creates a DELETE permission for a resource.
	 * 
	 * @param resource    the resource name
	 * @param description human-readable description
	 * @return configured DELETE permission
	 */
	public static Permission createDeletePermission(String resource, String description) {
		return Permission.builder().name(resource + "_DELETE").description(description).resource(resource)
				.action("DELETE").build();
	}

	/**
	 * Creates a custom permission with specific action.
	 * 
	 * @param resource    the resource name
	 * @param action      the action name
	 * @param description human-readable description
	 * @return configured custom permission
	 */
	public static Permission createCustomPermission(String resource, String action, String description) {
		return Permission.builder().name(resource + "_" + action).description(description).resource(resource)
				.action(action).build();
	}

	// ================================================================
	// Common RaagaAndRoast Permissions
	// ================================================================

	/**
	 * Factory methods for common RaagaAndRoast permissions. These can be used in
	 * data initialization scripts.
	 */
	public static class RaagaAndRoastPermissions {

		// User Management Permissions
		public static Permission userRead() {
			return createReadPermission("USER", "Read user information");
		}

		public static Permission userWrite() {
			return createWritePermission("USER", "Create and update users");
		}

		public static Permission userDelete() {
			return createDeletePermission("USER", "Delete users");
		}

		// Menu Management Permissions
		public static Permission menuRead() {
			return createReadPermission("MENU", "Read menu items and categories");
		}

		public static Permission menuWrite() {
			return createWritePermission("MENU", "Create and update menu items");
		}

		public static Permission menuDelete() {
			return createDeletePermission("MENU", "Delete menu items");
		}

		// Category Management Permissions
		public static Permission categoryRead() {
			return createReadPermission("CATEGORY", "Read categories");
		}

		public static Permission categoryWrite() {
			return createWritePermission("CATEGORY", "Create and update categories");
		}

		public static Permission categoryDelete() {
			return createDeletePermission("CATEGORY", "Delete categories");
		}

		// Order Management Permissions
		public static Permission orderRead() {
			return createReadPermission("ORDER", "Read orders");
		}

		public static Permission orderWrite() {
			return createWritePermission("ORDER", "Create orders");
		}

		public static Permission orderStatusUpdate() {
			return createCustomPermission("ORDER", "STATUS_UPDATE", "Update order status");
		}

		// Cart Management Permissions
		public static Permission cartRead() {
			return createReadPermission("CART", "Read cart contents");
		}

		public static Permission cartWrite() {
			return createWritePermission("CART", "Modify cart contents");
		}

		// Report Access Permission
		public static Permission reportRead() {
			return createReadPermission("REPORT", "Access reports and analytics");
		}
	}

	// ================================================================
	// Object Methods
	// ================================================================

	/**
	 * Custom equals based on business key (name) rather than ID. Two permissions
	 * are equal if they have the same name.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Permission permission))
			return false;
		return name != null && name.equals(permission.name);
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
		return "Permission{" + "id=" + id + ", name='" + name + '\'' + ", description='" + description + '\''
				+ ", resource='" + resource + '\'' + ", action='" + action + '\'' + ", roleCount="
				+ (roles != null ? roles.size() : 0) + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
	}
}
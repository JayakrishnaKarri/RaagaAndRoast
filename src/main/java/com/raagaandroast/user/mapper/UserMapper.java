package com.raagaandroast.user.mapper;

import com.raagaandroast.user.dto.UserResponse;
import com.raagaandroast.user.entity.Permission;
import com.raagaandroast.user.entity.Role;
import com.raagaandroast.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User entities and DTOs.
 * 
 * This mapper handles the conversion between internal entity representations
 * and external API DTOs. It ensures that sensitive information is not exposed
 * and that the API contract remains stable.
 * 
 * Design Decisions:
 * - Manual mapping for full control over the conversion process
 * - Excludes sensitive fields like passwords
 * - Flattens complex relationships for API simplicity
 * - Handles null safety
 * 
 * Interview Points:
 * - Why manual mapping vs MapStruct? Full control, no magic, easier debugging
 * - Why exclude password? Security - never expose passwords in responses
 * - Why flatten roles/permissions? Simpler API contract, easier client
 * consumption
 * - How does this support API versioning? Stable DTO structure independent of
 * entity changes
 * 
 * @author RaagaAndRoast Development Team
 */
@Component
public class UserMapper {

    /**
     * Converts a User entity to a UserResponse DTO.
     * 
     * @param user the user entity
     * @return the user response DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .roles(extractRoleNames(user.getRoles()))
                .permissions(extractPermissionNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .version(user.getVersion())
                .build();
    }

    /**
     * Extracts role names from a set of Role entities.
     * 
     * @param roles the set of roles
     * @return set of role names
     */
    private Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Extracts permission names from a set of Role entities.
     * This flattens all permissions from all roles into a single set.
     * 
     * @param roles the set of roles
     * @return set of permission names
     */
    private Set<String> extractPermissionNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
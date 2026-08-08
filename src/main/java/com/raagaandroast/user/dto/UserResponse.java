package com.raagaandroast.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for User entity.
 * 
 * This DTO is used to return user information in API responses.
 * It excludes sensitive information like passwords and includes
 * only the data that should be exposed to clients.
 * 
 * Design Decisions:
 * - No password field for security
 * - Includes role names for authorization context
 * - Includes audit information for transparency
 * - Uses UUID for better security than sequential IDs
 * 
 * Interview Points:
 * - Why separate DTO from entity? Security, API contract stability, performance
 * - Why exclude password? Security - never expose passwords in responses
 * - Why include roles? Client needs to know user permissions for UI decisions
 * 
 * @author RaagaAndRoast Development Team
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information response")
public class UserResponse {

    @Schema(description = "User unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Username", example = "john.doe")
    private String username;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Whether the account is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Whether the account is non-expired", example = "true")
    private Boolean accountNonExpired;

    @Schema(description = "Whether the account is non-locked", example = "true")
    private Boolean accountNonLocked;

    @Schema(description = "Whether the credentials are non-expired", example = "true")
    private Boolean credentialsNonExpired;

    @Schema(description = "User roles", example = "[\"CUSTOMER\", \"ADMIN\"]")
    private Set<String> roles;

    @Schema(description = "User permissions", example = "[\"USER_READ\", \"MENU_WRITE\"]")
    private Set<String> permissions;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last modification timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Version for optimistic locking", example = "1")
    private Long version;
}
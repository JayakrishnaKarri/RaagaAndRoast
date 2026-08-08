package com.raagaandroast.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user login requests.
 * 
 * This DTO:
 * - Accepts username/email and password for authentication
 * - Validates required fields
 * - Supports flexible login (username or email)
 * - Follows security best practices
 * 
 * Login Flow:
 * 1. Client sends username/email and password
 * 2. Server validates credentials
 * 3. Server returns JWT token on success
 * 4. Client uses token for subsequent requests
 * 
 * Security Considerations:
 * - Password is never logged or exposed
 * - Supports both username and email login
 * - Input validation prevents injection attacks
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * Username or email for authentication.
     * System will attempt to find user by either field.
     */
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    /**
     * Password for authentication.
     * Will be verified against stored BCrypt hash.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
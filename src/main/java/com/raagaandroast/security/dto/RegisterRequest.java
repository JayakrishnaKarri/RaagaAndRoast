package com.raagaandroast.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user registration requests.
 * 
 * This DTO:
 * - Validates user input for registration
 * - Ensures data integrity and security
 * - Provides clear validation messages
 * - Follows REST API best practices
 * 
 * Validation Rules:
 * - Username: 3-50 characters, required
 * - Email: Valid email format, required
 * - Password: Minimum 8 characters, required
 * 
 * Security Considerations:
 * - Password validation on client and server side
 * - Email format validation to prevent injection
 * - Username length limits to prevent abuse
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    /**
     * Username for the new account.
     * Must be unique across the system.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Email address for the new account.
     * Must be unique and valid format.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /**
     * Password for the new account.
     * Will be hashed using BCrypt before storage.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
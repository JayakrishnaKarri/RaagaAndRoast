package com.raagaandroast.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for authentication responses.
 * 
 * This DTO:
 * - Returns JWT token and metadata after successful authentication
 * - Provides token expiration information
 * - Includes user context for client applications
 * - Follows OAuth 2.0 token response format
 * 
 * Response Structure:
 * - accessToken: JWT token for API access
 * - tokenType: Always "Bearer" for JWT
 * - expiresIn: Token lifetime in seconds
 * - username: Authenticated user identifier
 * - authorities: User roles and permissions
 * 
 * Security Considerations:
 * - No sensitive information exposed
 * - Token expiration clearly communicated
 * - Authorities provided for client-side authorization
 * 
 * @author RaagaAndRoast Development Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {

    /**
     * JWT access token for API authentication.
     * Client should include this in Authorization header as "Bearer {token}".
     */
    private String accessToken;

    /**
     * Token type - always "Bearer" for JWT tokens.
     * Follows OAuth 2.0 specification.
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token expiration time in seconds from now.
     * Client should refresh token before expiration.
     */
    private Long expiresIn;

    /**
     * Username of the authenticated user.
     * Useful for client-side user context.
     */
    private String username;

    /**
     * User authorities (roles and permissions).
     * Enables client-side authorization decisions.
     */
    private List<String> authorities;

    /**
     * Timestamp when the token was issued.
     * Useful for client-side token management.
     */
    private LocalDateTime issuedAt;

    /**
     * Refresh token for obtaining new access tokens.
     * Optional - only included if refresh tokens are enabled.
     */
    private String refreshToken;
}
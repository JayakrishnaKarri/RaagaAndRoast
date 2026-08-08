package com.raagaandroast.security.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Custom implementation of Spring Security's UserDetails interface.
 * 
 * This class represents the authenticated user principal in the security
 * context.
 * It provides access to user information needed for authentication and
 * authorization
 * while maintaining security best practices.
 * 
 * Design Decisions:
 * - Implements UserDetails for Spring Security integration
 * - Exposes user ID for authorization checks
 * - Maintains authorities for role/permission-based authorization
 * - Immutable design for security
 * 
 * Interview Points:
 * - Why custom UserDetails? Need access to user ID and custom fields
 * - Why immutable? Security - prevents modification after authentication
 * - How does this integrate with JWT? JWT filter creates this principal
 * 
 * @author RaagaAndRoast Development Team
 */
public class CustomUserPrincipal implements UserDetails {

    private static final long serialVersionUID = -3542259758300458255L;
	private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor for creating a CustomUserPrincipal.
     * 
     * @param id                    the user's unique identifier
     * @param username              the username
     * @param email                 the user's email
     * @param password              the encoded password
     * @param enabled               whether the account is enabled
     * @param accountNonExpired     whether the account is non-expired
     * @param accountNonLocked      whether the account is non-locked
     * @param credentialsNonExpired whether the credentials are non-expired
     * @param authorities           the user's authorities (roles and permissions)
     */
    public CustomUserPrincipal(UUID id, String username, String email, String password,
            boolean enabled, boolean accountNonExpired, boolean accountNonLocked,
            boolean credentialsNonExpired, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.authorities = authorities;
    }

    /**
     * Gets the user's unique identifier.
     * This is used for authorization checks and resource ownership validation.
     * 
     * @return the user's UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the user's email address.
     * 
     * @return the user's email
     */
    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        CustomUserPrincipal that = (CustomUserPrincipal) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CustomUserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                ", accountNonExpired=" + accountNonExpired +
                ", accountNonLocked=" + accountNonLocked +
                ", credentialsNonExpired=" + credentialsNonExpired +
                ", authorities=" + authorities +
                '}';
    }
}
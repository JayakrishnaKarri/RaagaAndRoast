package com.raagaandroast.security.service;

import com.raagaandroast.common.exception.*;
import com.raagaandroast.security.dto.AuthenticationResponse;
import com.raagaandroast.security.dto.LoginRequest;
import com.raagaandroast.security.dto.RegisterRequest;
import com.raagaandroast.security.jwt.JwtService;
import com.raagaandroast.user.entity.Role;
import com.raagaandroast.user.entity.User;
import com.raagaandroast.user.repository.RoleRepository;
import com.raagaandroast.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication service handling user registration and login operations.
 * 
 * This service provides: - User registration with validation and role
 * assignment - User authentication with JWT token generation - Password
 * security with BCrypt hashing - Transactional operations for data consistency
 * - Comprehensive error handling and logging
 * 
 * Business Rules: - New users are assigned CUSTOMER role by default - Usernames
 * and emails must be unique - Passwords are hashed using BCrypt - JWT tokens
 * include user authorities
 * 
 * Security Features: - Password hashing with BCrypt - Input validation and
 * sanitization - Secure token generation - Audit logging for security events
 * 
 * Interview Points: - Why @Transactional? Ensures data consistency during
 * registration - Why AuthenticationManager? Leverages Spring Security's
 * authentication - Why separate registration/login? Different validation and
 * business logic - Error handling strategy: Specific exceptions for different
 * scenarios
 * 
 * @author RaagaAndRoast Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	@Value("${app.jwt.expiration}")
	private long jwtExpirationMs;

	/**
	 * Registers a new user account.
	 * 
	 * Registration Process: 1. Validate input data 2. Check username and email
	 * uniqueness 3. Hash password with BCrypt 4. Create user entity 5. Assign
	 * default CUSTOMER role 6. Save to database 7. Generate JWT token 8. Return
	 * authentication response
	 * 
	 * @param request registration request with user details
	 * @return authentication response with JWT token
	 * @throws IllegalArgumentException   if validation fails
	 * @throws DuplicateResourceException if user already exists
	 * @throws ResourceNotFoundException  if default role not found
	 */
	@Transactional
	public AuthenticationResponse register(RegisterRequest request) {
		log.info("Attempting to register new user: {}", request.getUsername());

		// Validate input
		validateRegistrationRequest(request);

		// Check if username already exists
		if (userRepository.existsByUsername(request.getUsername())) {
			log.warn("Registration failed: Username already exists: {}", request.getUsername());
			throw new DuplicateResourceException("User", "username", request.getUsername());
		}

		// Check if email already exists
		if (userRepository.existsByEmail(request.getEmail())) {
			log.warn("Registration failed: Email already exists: {}", request.getEmail());
			throw new DuplicateResourceException("User", "email", request.getEmail());
		}

		// Get default CUSTOMER role
		Role customerRole = roleRepository.findByName("CUSTOMER")
				.orElseThrow(() -> new ResourceNotFoundException("Role", "CUSTOMER"));

		// Create new user
		User user = User.builder().username(request.getUsername()).email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword())).enabled(true).accountNonExpired(true)
				.accountNonLocked(true).credentialsNonExpired(true).build();

		// Assign default role
		user.addRole(customerRole);

		// Save user
		User savedUser = userRepository.save(user);

		log.info("Successfully registered new user: {} with ID: {}", savedUser.getUsername(), savedUser.getId());

		// Generate JWT token
		List<String> authorities = savedUser.getRoles().stream().flatMap(role -> role.getPermissions().stream())
				.map(permission -> permission.getName()).collect(Collectors.toList());

		// Add role authorities
		savedUser.getRoles().forEach(role -> authorities.add("ROLE_" + role.getName()));

		String accessToken = jwtService.generateToken(savedUser.getUsername(), authorities);

		return AuthenticationResponse.builder().accessToken(accessToken).tokenType("Bearer")
				.expiresIn(jwtExpirationMs / 1000) // Convert to seconds
				.username(savedUser.getUsername()).authorities(authorities).issuedAt(LocalDateTime.now()).build();
	}

	/**
	 * Authenticates a user and generates JWT token.
	 * 
	 * Login Process: 1. Validate input data 2. Authenticate with Spring Security 3.
	 * Load user details with authorities 4. Generate JWT token 5. Return
	 * authentication response
	 * 
	 * @param request login request with credentials
	 * @return authentication response with JWT token
	 * @throws org.springframework.security.core.AuthenticationException if
	 *                                                                   authentication
	 *                                                                   fails
	 */
	@Transactional(readOnly = true)
	public AuthenticationResponse login(LoginRequest request) {
		log.info("Attempting to authenticate user: {}", request.getUsernameOrEmail());

		// Validate input
		validateLoginRequest(request);

		// Authenticate user with Spring Security
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));

		// Extract authorities from authentication
		List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		// Generate JWT token
		String accessToken = jwtService.generateToken(authentication);

		log.info("Successfully authenticated user: {}", authentication.getName());

		return AuthenticationResponse.builder().accessToken(accessToken).tokenType("Bearer")
				.expiresIn(jwtExpirationMs / 1000) // Convert to seconds
				.username(authentication.getName()).authorities(authorities).issuedAt(LocalDateTime.now()).build();
	}

	/**
	 * Refreshes an existing JWT token.
	 * 
	 * @param refreshToken the refresh token
	 * @return new authentication response with fresh token
	 */
	@Transactional(readOnly = true)
	public AuthenticationResponse refreshToken(String refreshToken) {
		log.info("Attempting to refresh token");

		// Validate refresh token
		if (!jwtService.validateToken(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
			throw InvalidRefreshTokenException.invalidToken();
		}

		// Extract user information
		String username = jwtService.getUsernameFromToken(refreshToken);
		List<String> authorities = jwtService.getAuthoritiesFromToken(refreshToken);

		// Generate new access token
		String accessToken = jwtService.generateToken(username, authorities);

		log.info("Successfully refreshed token for user: {}", username);

		return AuthenticationResponse.builder().accessToken(accessToken).tokenType("Bearer")
				.expiresIn(jwtExpirationMs / 1000).username(username).authorities(authorities)
				.issuedAt(LocalDateTime.now()).build();
	}

	/**
	 * Validates registration request data.
	 * 
	 * @param request registration request to validate
	 * @throws IllegalArgumentException if validation fails
	 */
	private void validateRegistrationRequest(RegisterRequest request) {
		if (request == null) {
			throw AuthenticationRequestValidationException.nullRegistrationRequest();
		}

		if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
			throw AuthenticationRequestValidationException.missingUsername();
		}

		if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
			throw AuthenticationRequestValidationException.missingEmail();
		}

		if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
			throw AuthenticationRequestValidationException.missingPassword();
		}

		// Additional business validation can be added here
		if (request.getUsername().length() < 3) {
			throw AuthenticationRequestValidationException.usernameTooShort();
		}

		if (request.getPassword().length() < 8) {
			throw AuthenticationRequestValidationException.passwordTooShort();
		}
	}

	/**
	 * Validates login request data.
	 * 
	 * @param request login request to validate
	 * @throws IllegalArgumentException if validation fails
	 */
	private void validateLoginRequest(LoginRequest request) {
		if (request == null) {
			throw AuthenticationRequestValidationException.nullLoginRequest();
		}

		if (request.getUsernameOrEmail() == null || request.getUsernameOrEmail().trim().isEmpty()) {
			throw AuthenticationRequestValidationException.missingUsernameOrEmail();
		}

		if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
			throw AuthenticationRequestValidationException.missingLoginPassword();
		}
	}
}
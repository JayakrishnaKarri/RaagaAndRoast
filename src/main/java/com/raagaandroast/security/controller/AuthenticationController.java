package com.raagaandroast.security.controller;

import com.raagaandroast.security.dto.AuthenticationResponse;
import com.raagaandroast.security.dto.LoginRequest;
import com.raagaandroast.security.dto.RefreshTokenRequest;
import com.raagaandroast.security.dto.RegisterRequest;
import com.raagaandroast.security.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 *
 * Endpoints: - POST /api/auth/register — register a new user account - POST
 * /api/auth/login — authenticate and receive tokens - POST /api/auth/refresh —
 * exchange a refresh token for a new access token
 *
 * Exception handling is delegated entirely to the global @ControllerAdvice.
 * This controller contains no try-catch blocks; it only handles the happy path.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration operations")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	/**
	 * Registers a new user account.
	 *
	 * Returns 201 Created with tokens so the client is immediately authenticated
	 * without a separate login round-trip.
	 *
	 * FIX 1 — try-catch blocks removed from all three endpoints. The original
	 * caught exceptions only to re-throw them with "will be handled by global
	 * exception handler" comments. That is exactly what happens when you don't
	 * catch them at all. The catch blocks added stack-frame noise, made the code
	 * harder to read, and risked accidentally swallowing an exception if the
	 * re-throw line were ever deleted during a refactor. All error handling now
	 * lives in the global @ControllerAdvice, which is the correct single place for
	 * it.
	 */
	@Operation(summary = "Register new user", description = "Creates a new user account and returns JWT tokens for immediate authentication")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input or username/email already taken", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {

		// FIX 2 — Username logged at DEBUG, not INFO.
		// Logging usernames (and especially passwords, emails, or tokens) at INFO
		// means they appear in production log aggregators, alerting pipelines, and
		// log-shipping destinations. Username is not secret, but it is PII.
		// DEBUG is off by default in production (see application-prod.properties)
		// so this only surfaces in dev/test where it is actually useful.
		log.debug("Registration attempt for username: {}", request.getUsername());

		AuthenticationResponse response = authenticationService.register(request);

		log.debug("Registration successful for username: {}", request.getUsername());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * Authenticates a user and returns access + refresh tokens.
	 *
	 * FIX 2 (continued) — Login identifier logged at DEBUG only. The original
	 * logged request.getUsernameOrEmail() at INFO on every login attempt. Combined
	 * with a failed-login error log, this creates a record of every username that
	 * attempted to authenticate, including failed attempts from credential-stuffing
	 * attacks — exactly the data an attacker wants if they later access your log
	 * store.
	 */
	@Operation(summary = "Authenticate user", description = "Validates credentials and returns JWT access and refresh tokens")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping("/login")
	public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {

		log.debug("Login attempt for identifier: {}", request.getUsernameOrEmail());

		AuthenticationResponse response = authenticationService.login(request);

		log.debug("Login successful for identifier: {}", request.getUsernameOrEmail());
		return ResponseEntity.ok(response);
	}

	/**
	 * Exchanges a valid refresh token for a new access token.
	 *
	 * FIX 3 — Refresh token moved from @RequestParam to @RequestBody. Query
	 * parameters are: (a) logged verbatim by every reverse proxy, load balancer,
	 * and CDN (nginx access logs, AWS ALB logs, Cloudfront logs, etc.); (b) stored
	 * in browser history; (c) leaked via the HTTP Referer header to third-party
	 * scripts. A refresh token in a query param will end up in at least one of
	 * those places. Sending it as a JSON body over TLS keeps it off the wire in
	 * plaintext and out of server access logs.
	 *
	 * The new RefreshTokenRequest DTO gives you a place to add @NotBlank validation
	 * via @Valid, which @RequestParam alone cannot enforce through Bean Validation.
	 *
	 * FIX 4 — Removed the /auth/health endpoint. It duplicated /actuator/health
	 * (already exposed and already public per SecurityConfig), added a second
	 * unauthenticated surface with no benefit, and cluttered the Swagger docs with
	 * a meaningless entry. Application health belongs to the actuator, not to a
	 * business-logic controller.
	 */
	@Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token for a new access token")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
			@ApiResponse(responseCode = "400", description = "Missing or malformed refresh token", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Refresh token expired or invalid", content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping("/refresh")
	public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {

		log.debug("Token refresh requested");

		AuthenticationResponse response = authenticationService.refreshToken(request.getRefreshToken());

		log.debug("Token refresh successful");
		return ResponseEntity.ok(response);
	}
}
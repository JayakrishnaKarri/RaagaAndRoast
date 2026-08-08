package com.raagaandroast.security.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.raagaandroast.security.authentication.CustomUserDetailsService;
import com.raagaandroast.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Modern Spring Security configuration using SecurityFilterChain.
 * 
 * This configuration provides: - JWT-based stateless authentication -
 * Method-level security with @PreAuthorize - CORS configuration for frontend
 * integration - Custom authentication provider - Proper exception handling -
 * Public endpoint configuration
 * 
 * Security Features: - Stateless session management (no server-side sessions) -
 * JWT authentication filter integration - Role and permission-based
 * authorization - CSRF protection disabled (stateless JWT) - CORS enabled for
 * cross-origin requests
 * 
 * Interview Points: - Why SecurityFilterChain over
 * WebSecurityConfigurerAdapter? Modern approach, not deprecated - Why
 * stateless? Better scalability, no session storage required - Why disable
 * CSRF? Not needed for stateless JWT authentication - Filter ordering: JWT
 * filter before UsernamePasswordAuthenticationFilter
 * 
 * @author RaagaAndRoast Development Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final PasswordEncoder passwordEncoder;

	/**
	 * Configures the main security filter chain.
	 * 
	 * Security Configuration: - Disables CSRF (not needed for stateless JWT) -
	 * Enables CORS for frontend integration - Configures stateless session
	 * management - Sets up public and protected endpoints - Integrates JWT
	 * authentication filter - Configures authentication provider
	 * 
	 * @param http HttpSecurity configuration
	 * @return configured SecurityFilterChain
	 * @throws Exception if configuration fails
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// Disable CSRF - not needed for stateless JWT authentication
				.csrf(AbstractHttpConfigurer::disable)

				// Enable CORS with custom configuration
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				// Configure session management - stateless for JWT
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// Configure authorization rules
				.authorizeHttpRequests(authz -> authz
						// Public endpoints - no authentication required
						.requestMatchers("/api/auth/**", // Authentication endpoints
								"/api/public/**", // Public API endpoints
								"/swagger-ui/**", // Swagger UI
								"/swagger-ui.html", // Swagger UI HTML
								"/v3/api-docs/**", // OpenAPI docs (default path)
								"/api-docs/**", // OpenAPI docs (custom path)
								"/api-docs", // OpenAPI docs root
								"/actuator/health", // Health check
								"/actuator/info", // Application info
								"/error" // Error handling
						).permitAll()

						// Admin-only endpoints
						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						// Manager and Admin endpoints
						.requestMatchers("/api/management/**").hasAnyRole("MANAGER", "ADMIN")

						// All other requests require authentication
						.anyRequest().authenticated())

				// Set custom authentication provider
				.authenticationProvider(authenticationProvider())

				// Add JWT filter before UsernamePasswordAuthenticationFilter
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Configures the authentication provider.
	 * 
	 * Uses DaoAuthenticationProvider with: - Custom UserDetailsService for loading
	 * user details - BCrypt password encoder for password verification
	 * 
	 * @return configured AuthenticationProvider
	 */
	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

	/**
	 * Exposes AuthenticationManager as a bean. Required for manual authentication
	 * in services.
	 * 
	 * @param config AuthenticationConfiguration
	 * @return AuthenticationManager
	 * @throws Exception if configuration fails
	 */
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Configures CORS for cross-origin requests.
	 * 
	 * CORS Configuration: - Allows specific origins (configure for production) -
	 * Allows common HTTP methods - Allows Authorization header for JWT - Allows
	 * credentials for authenticated requests
	 * 
	 * @return CORS configuration source
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Configure allowed origins - update for production
		configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", // React development server
				"http://localhost:4200", // Angular development server
				"http://localhost:8080", // Local development
				"https://*.yourdomain.com" // Production domain
		));

		// Configure allowed methods
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		// Configure allowed headers
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept",
				"Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));

		// Configure exposed headers
		configuration
				.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));

		// Allow credentials (cookies, authorization headers)
		configuration.setAllowCredentials(true);

		// Cache preflight response for 1 hour
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
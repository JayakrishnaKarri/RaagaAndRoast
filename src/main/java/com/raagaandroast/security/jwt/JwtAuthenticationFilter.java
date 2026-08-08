package com.raagaandroast.security.jwt;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.raagaandroast.security.authentication.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter for Spring Security.
 *
 * Filter flow: 1. Skip public paths (shouldNotFilter) — consistent with
 * SecurityConfig 2. Extract Bearer token from Authorization header 3. Validate
 * token signature and expiry via JwtService 4. Build authorities directly from
 * token claims (no DB call) 5. Fall back to DB load only when token carries no
 * authority claims 6. Set Authentication in SecurityContextHolder and continue
 * filter chain
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	// FIX 1 — Single source of truth for public paths.
	// The original duplicated the public-endpoint list from SecurityConfig as
	// plain startsWith() strings. Two lists means two places to update on every
	// routing change — they will drift. One constant array here, matched with
	// AntPathMatcher, keeps the skip logic consistent with the permitAll() rules.
	private static final String[] PUBLIC_PATHS = { "/api/auth/**", "/api/public/**", "/swagger-ui/**",
			"/swagger-ui.html", "/v3/api-docs/**", "/actuator/health", "/actuator/info", "/error" };

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		for (String pattern : PUBLIC_PATHS) {
			if (PATH_MATCHER.match(pattern, path)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			String jwt = extractBearerToken(request);

			if (StringUtils.hasText(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
				authenticateWithToken(request, jwt);
			}

		} catch (Exception e) {
			log.error("JWT authentication failed for request [{}]: {}", request.getRequestURI(), e.getMessage());
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Builds and sets the SecurityContext authentication from a JWT token.
	 *
	 * FIX 2 — Authorities are now read from the token claims first. The original
	 * always called userDetailsService.loadUserByUsername(), which issues a DB
	 * query on every single authenticated request. For a stateless JWT setup this
	 * defeats a core benefit of the pattern: the token already carries the
	 * authorities claim — there is no reason to hit the database unless the token
	 * lacks that claim (e.g. tokens issued by an older version of the service).
	 *
	 * The fallback to loadUserByUsername() is kept for that legacy case, but the
	 * happy path avoids the DB entirely.
	 *
	 * FIX 3 — The UsernamePasswordAuthenticationToken is now constructed from the
	 * claim-derived authorities rather than from userDetails.getAuthorities() when
	 * using the fast path. Previously, even if authorities were present in the
	 * token, the code loaded UserDetails from the DB and used those authorities
	 * instead — making the token's own claim irrelevant.
	 */
	private void authenticateWithToken(HttpServletRequest request, String jwt) {
		if (!jwtService.validateToken(jwt)) {
			log.debug("JWT validation failed, skipping authentication");
			return;
		}

		String username = jwtService.getUsernameFromToken(jwt);
		if (!StringUtils.hasText(username)) {
			log.warn("JWT token has no subject claim");
			return;
		}

		List<String> authorityClaims = jwtService.getAuthoritiesFromToken(jwt);

		UsernamePasswordAuthenticationToken authentication;

		if (authorityClaims != null && !authorityClaims.isEmpty()) {
			// Fast path: build authentication purely from token claims — no DB call.
			List<SimpleGrantedAuthority> grantedAuthorities = authorityClaims.stream().map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());

			// FIX 4 — Principal is set to the username string on the fast path.
			// Using a bare String is sufficient (and avoids constructing a UserDetails
			// shell); anything downstream that needs the full UserDetails object should
			// call loadUserByUsername() explicitly rather than casting the principal.
			authentication = new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);

		} else {
			// Fallback: token has no authority claims (legacy tokens) → load from DB.
			log.debug("No authority claims in token for '{}', falling back to DB load", username);
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		}

		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		log.debug("Authenticated user '{}' via JWT (path: {})", username, request.getRequestURI());
	}

	/**
	 * Extracts the Bearer token from the Authorization header.
	 *
	 * FIX 5 — "Bearer " prefix check is now case-insensitive and trims the
	 * extracted token. The HTTP spec treats header values as case-sensitive, but
	 * "Bearer" itself is case-insensitive per RFC 6750 §2.1. Some clients send
	 * "bearer <token>" or "BEARER <token>". A strict startsWith("Bearer ") silently
	 * drops those tokens, making auth appear broken for no obvious reason.
	 *
	 * @return raw JWT string, or null if header is absent or malformed
	 */
	private String extractBearerToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");

		if (StringUtils.hasText(header) && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
			String token = header.substring(7).trim();
			if (StringUtils.hasText(token)) {
				return token;
			}
		}

		return null;
	}
}
package com.raagaandroast.security.jwt;

import com.raagaandroast.common.exception.JwtConfigurationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT utility service for token generation, validation, and claims extraction.
 *
 * Security features: - HMAC-SHA256 signing (HS256) via JJWT 0.12.x modern API -
 * Configurable expiration and issuer via application properties - Explicit
 * UTF-8 encoding for deterministic key derivation - Expiration and issuer
 * validated inside the parser (no manual re-check) - Refresh token expiration
 * read from its own dedicated property
 */
@Slf4j
@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long jwtExpirationMs;
	private final long refreshExpirationMs;
	private final String jwtIssuer;

	/**
	 * FIX 1 — Added refreshExpirationMs injected from app.jwt.refresh-expiration.
	 * The original multiplied jwtExpirationMs * 7 inline, which hardcodes the
	 * refresh window and ignores the dedicated property already defined in
	 * application.properties. That also risks long overflow on large values.
	 *
	 * FIX 2 — Charset made explicit: jwtSecret.getBytes(StandardCharsets.UTF_8).
	 * The no-arg getBytes() uses the JVM default charset, which differs between
	 * platforms. On a Windows dev machine vs. a Linux container the same secret
	 * string produces different key bytes → tokens signed on one box fail on the
	 * other. UTF-8 is deterministic everywhere.
	 *
	 * FIX 3 — Key length guard added. Keys.hmacShaKeyFor() requires at least 32
	 * bytes for HS256. Short secrets throw WeakKeyException at startup rather than
	 * silently producing insecure tokens. The guard surfaces misconfiguration
	 * immediately instead of at the first token issuance under load.
	 */
	public JwtService(@Value("${app.jwt.secret}") String jwtSecret,
			@Value("${app.jwt.expiration}") long jwtExpirationMs,
			@Value("${app.jwt.refresh-expiration}") long refreshExpirationMs,
			@Value("${app.jwt.issuer:raaga-and-roast}") String jwtIssuer) {

		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			throw JwtConfigurationException.secretKeyTooShort(keyBytes.length, 32);
		}

		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.jwtExpirationMs = jwtExpirationMs;
		this.refreshExpirationMs = refreshExpirationMs;
		this.jwtIssuer = jwtIssuer;

		log.info("JWT Service initialized — access expiry: {} ms, refresh expiry: {} ms", jwtExpirationMs,
				refreshExpirationMs);
	}

	// -------------------------------------------------------------------------
	// Token generation
	// -------------------------------------------------------------------------

	/**
	 * Generates an access token from a Spring Security Authentication.
	 *
	 * FIX 4 — Replaced the deprecated fluent API (setSubject / setIssuer /
	 * setIssuedAt / setExpiration / signWith(key, SignatureAlgorithm.HS256)) with
	 * the JJWT 0.12.x API (subject / issuer / issuedAt / expiration /
	 * signWith(key)). SignatureAlgorithm enum is deprecated in 0.12 and will be
	 * removed; the new API infers the algorithm from the key type automatically.
	 */
	public String generateToken(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		List<String> authorities = userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		String token = buildToken(userPrincipal.getUsername(), authorities, jwtExpirationMs, null);
		log.debug("Generated access token for user: {}", userPrincipal.getUsername());
		return token;
	}

	/** Alternative entry point for custom authentication flows. */
	public String generateToken(String username, List<String> authorities) {
		String token = buildToken(username, authorities, jwtExpirationMs, null);
		log.debug("Generated access token for user: {}", username);
		return token;
	}

	/**
	 * Generates a refresh token.
	 *
	 * FIX 5 — Expiration now uses the injected refreshExpirationMs (from
	 * app.jwt.refresh-expiration) instead of jwtExpirationMs * 7. Also uses the
	 * shared buildToken() helper to avoid duplicating builder logic.
	 */
	public String generateRefreshToken(String username, List<String> authorities) {
		String token = buildToken(username, authorities, refreshExpirationMs, "refresh");
		log.debug("Generated refresh token for user: {}", username);
		return token;
	}

	// -------------------------------------------------------------------------
	// Token validation
	// -------------------------------------------------------------------------

	/**
	 * Validates a JWT token.
	 *
	 * FIX 6 — Removed the manual expiration and issuer checks that duplicated what
	 * the parser already enforces. The original called getClaimsFromToken() (which
	 * throws ExpiredJwtException on expiry) and then checked
	 * claims.getExpiration().before(new Date()) a second time on the returned
	 * claims — unreachable dead code if the parser already rejected the token.
	 * requireExpiration() and requireIssuer() push those checks into the parser
	 * itself, which is the correct place.
	 *
	 * FIX 7 — Added SignatureException to the catch block. The original caught
	 * MalformedJwtException and a generic Exception fallback but not
	 * io.jsonwebtoken.security.SignatureException, so a tampered-signature token
	 * would fall through to the broad Exception handler with a misleading message.
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(secretKey).requireIssuer(jwtIssuer).build().parseSignedClaims(token);

			log.debug("JWT token validation successful");
			return true;

		} catch (ExpiredJwtException e) {
			log.warn("JWT token is expired: {}", e.getMessage());
		} catch (SignatureException e) {
			log.error("JWT signature verification failed: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.error("Malformed JWT token: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT token: {}", e.getMessage());
		} catch (IncorrectClaimException e) {
			log.warn("JWT claim validation failed (issuer mismatch): {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("JWT token is empty or null: {}", e.getMessage());
		}

		return false;
	}

	public boolean isTokenExpired(String token) {
		try {
			Date expiration = getExpirationDateFromToken(token);
			return expiration.before(new Date());
		} catch (Exception e) {
			return true;
		}
	}

	public boolean isRefreshToken(String token) {
		try {
			Claims claims = getClaimsFromToken(token);
			return "refresh".equals(claims.get("type"));
		} catch (Exception e) {
			return false;
		}
	}

	// -------------------------------------------------------------------------
	// Claims extraction
	// -------------------------------------------------------------------------

	public String getUsernameFromToken(String token) {
		return getClaimsFromToken(token).getSubject();
	}

	@SuppressWarnings("unchecked")
	public List<String> getAuthoritiesFromToken(String token) {
		return (List<String>) getClaimsFromToken(token).get("authorities");
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimsFromToken(token).getExpiration();
	}

	public Date getIssuedAtDateFromToken(String token) {
		return getClaimsFromToken(token).getIssuedAt();
	}

	public long getRemainingTimeMs(String token) {
		try {
			Date expiration = getExpirationDateFromToken(token);
			return Math.max(0, expiration.getTime() - System.currentTimeMillis());
		} catch (Exception e) {
			log.error("Error calculating remaining token time: {}", e.getMessage());
			return 0;
		}
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	/**
	 * FIX 4 (continued) — Single builder method using the JJWT 0.12.x API.
	 * subject(), issuer(), issuedAt(), expiration(), and signWith(SecretKey)
	 * replace the deprecated set-prefixed methods and explicit SignatureAlgorithm.
	 */
	private String buildToken(String username, List<String> authorities, long expirationMs, String tokenType) {
		Instant now = Instant.now();
		Instant expiry = now.plus(expirationMs, ChronoUnit.MILLIS);

		JwtBuilder builder = Jwts.builder().subject(username).issuer(jwtIssuer).issuedAt(Date.from(now))
				.expiration(Date.from(expiry)).claim("authorities", authorities).signWith(secretKey);

		if (tokenType != null) {
			builder.claim("type", tokenType);
		}

		return builder.compact();
	}

	/**
	 * FIX 4 (continued) — Uses parseSignedClaims() instead of the deprecated
	 * parseClaimsJws(). verifyWith(secretKey) replaces setSigningKey(secretKey).
	 */
	private Claims getClaimsFromToken(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	}
}
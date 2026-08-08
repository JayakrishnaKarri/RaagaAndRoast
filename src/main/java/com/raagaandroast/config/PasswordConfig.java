package com.raagaandroast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for password encoding.
 * 
 * This configuration provides: - BCrypt password encoder with optimal strength
 * - Secure password hashing for user authentication - Production-ready password
 * security
 * 
 * BCrypt is chosen because: - Adaptive hashing function designed for passwords
 * - Built-in salt generation - Configurable work factor (strength) - Resistant
 * to rainbow table attacks - Industry standard for password hashing
 * 
 * @author RaagaAndRoast Development Team
 */
@Configuration
public class PasswordConfig {

	/**
	 * Creates a BCrypt password encoder bean.
	 * 
	 * BCrypt strength of 12 is used which provides: - Strong security against brute
	 * force attacks - Reasonable performance for authentication - Future-proof
	 * against hardware improvements
	 * 
	 * Strength levels: - 10: Fast, suitable for development - 12: Recommended for
	 * production (default) - 14+: Very secure but slower
	 * 
	 * @return BCryptPasswordEncoder instance
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
}
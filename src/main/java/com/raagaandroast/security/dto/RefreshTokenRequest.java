package com.raagaandroast.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for the token refresh endpoint.
 *
 * Carries the refresh token in the request body (not a query parameter) so it
 * is transmitted over TLS and never appears in server access logs, browser
 * history, or Referer headers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

	@NotBlank(message = "Refresh token must not be blank")
	private String refreshToken;
}
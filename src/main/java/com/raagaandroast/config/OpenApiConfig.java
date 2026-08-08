package com.raagaandroast.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for RaagaAndRoast API Documentation.
 * 
 * This configuration provides: - Comprehensive API documentation with Swagger
 * UI - JWT Bearer token authentication setup - API versioning and contact
 * information - Server configuration for different environments
 * 
 * Access Swagger UI at: /swagger-ui.html Access API docs at: /api-docs
 * 
 * @author RaagaAndRoast Development Team
 */
@Configuration
public class OpenApiConfig {

	@Value("${spring.application.name:RaagaAndRoast}")
	private String applicationName;

	/**
	 * Configures OpenAPI documentation with security schemes and API information.
	 * 
	 * @return OpenAPI configuration
	 */
	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI().info(apiInfo())
				.servers(List.of(new Server().url("http://localhost:8080").description("Development Server"),
						new Server().url("https://api.raagaandroast.com").description("Production Server")))
				.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
				.components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("Bearer Authentication",
						createAPIKeyScheme()));
	}

	/**
	 * Creates API information including title, description, version, and contact
	 * details.
	 * 
	 * @return API Info object
	 */
	private Info apiInfo() {
		return new Info().title("RaagaAndRoast API").description("""
				Portfolio-grade café ordering and management platform API.

				This API demonstrates advanced Spring Boot backend engineering including:
				- JWT Authentication & Authorization
				- Role-based and Permission-based Access Control
				- RESTful API Design with proper HTTP status codes
				- Comprehensive validation and error handling
				- JPA performance optimization and relationship management

				## Authentication
				Most endpoints require authentication. Use the `/api/auth/login` endpoint to obtain a JWT token,
				then include it in the Authorization header as `Bearer <token>`.

				## Authorization
				The API supports multiple user roles:
				- **CUSTOMER**: Can browse menu, manage cart, place orders
				- **STAFF**: Can view and update order status
				- **MANAGER**: Can manage menu items and categories
				- **ADMIN**: Full system access
				""").version("1.0.0")
				.contact(new Contact().name("RaagaAndRoast Development Team").email("dev@raagaandroast.com")
						.url("https://github.com/raagaandroast/backend"))
				.license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"));
	}

	/**
	 * Creates JWT Bearer token security scheme for API authentication.
	 * 
	 * @return SecurityScheme for JWT authentication
	 */
	private SecurityScheme createAPIKeyScheme() {
		return new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer")
				.description("Enter JWT Bearer token obtained from /api/auth/login");
	}
}
package com.mgs.claudecode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	public static final String BEARER_AUTH = "bearerAuth";

	@Bean
	public OpenAPI mgsClaudeCodeOpenApi() {
		return new OpenAPI().info(new Info()
			.title("mgs-claude-code API")
			.description("CRUD API for managing people.")
			.version("0.0.1"))
			.components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.description("Token from POST /api/auth/login")));
	}

}

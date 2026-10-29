package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI mgsClaudeCodeOpenApi() {
		return new OpenAPI().info(new Info()
			.title("mgs-claude-code API")
			.description("CRUD API for managing people.")
			.version("0.0.1"));
	}

}

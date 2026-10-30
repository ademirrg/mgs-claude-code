package com.mgs.claudecode.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security")
public record AppSecurityProperties(Jwt jwt, List<User> users) {

	public enum Role {

		ADMIN, USER

	}

	public record Jwt(String secret, String issuer, Duration expiration) {

		public Jwt {
			if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
				throw new IllegalArgumentException("app.security.jwt.secret must be at least 32 bytes for HS256");
			}
		}

	}

	public record User(String username, String password, Role role) {
	}

}

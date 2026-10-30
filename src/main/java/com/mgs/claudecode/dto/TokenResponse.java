package com.mgs.claudecode.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A signed JWT access token")
public record TokenResponse(
		@Schema(description = "Send as 'Authorization: Bearer <accessToken>'") String accessToken,
		@Schema(example = "Bearer") String tokenType,
		@Schema(description = "Seconds until the token expires", example = "3600") long expiresIn) {
}

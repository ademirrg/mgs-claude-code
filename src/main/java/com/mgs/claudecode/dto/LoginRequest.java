package com.mgs.claudecode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials for obtaining an access token")
public record LoginRequest(
		@Schema(example = "admin") @NotBlank String username,
		@Schema(example = "admin123") @NotBlank String password) {
}

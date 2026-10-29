package com.example.demo.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Application health status")
public record HealthResponse(
		@Schema(description = "UP, DOWN, OUT_OF_SERVICE or UNKNOWN", example = "UP") String status,
		@Schema(example = "2026-10-29T18:00:00Z") Instant timestamp) {
}

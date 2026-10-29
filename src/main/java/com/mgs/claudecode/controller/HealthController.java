package com.mgs.claudecode.controller;

import java.time.Instant;

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgs.claudecode.dto.HealthResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Application health check")
public class HealthController {

	private final HealthEndpoint healthEndpoint;

	public HealthController(HealthEndpoint healthEndpoint) {
		this.healthEndpoint = healthEndpoint;
	}

	@GetMapping
	@Operation(summary = "Check application health",
			description = "Aggregates the Actuator health checks, including the database.")
	@ApiResponse(responseCode = "200", description = "Application is up")
	@ApiResponse(responseCode = "503", description = "Application or one of its dependencies is down")
	public ResponseEntity<HealthResponse> health() {
		Status status = healthEndpoint.health().getStatus();
		HttpStatus httpStatus = Status.UP.equals(status) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
		return ResponseEntity.status(httpStatus).body(new HealthResponse(status.getCode(), Instant.now()));
	}

}

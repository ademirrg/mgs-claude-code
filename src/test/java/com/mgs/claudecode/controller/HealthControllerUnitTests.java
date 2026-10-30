package com.mgs.claudecode.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mgs.claudecode.dto.HealthResponse;

@ExtendWith(MockitoExtension.class)
class HealthControllerUnitTests {

	@Mock
	private HealthEndpoint healthEndpoint;

	@InjectMocks
	private HealthController controller;

	@Test
	void returnsOkWhenUp() {
		givenStatus(Status.UP);
		Instant before = Instant.now();

		ResponseEntity<HealthResponse> response = controller.health();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo("UP");
		assertThat(response.getBody().timestamp()).isBetween(before, Instant.now());
	}

	@ParameterizedTest
	@ValueSource(strings = { "DOWN", "OUT_OF_SERVICE", "UNKNOWN" })
	void returnsServiceUnavailableWhenNotUp(String code) {
		givenStatus(new Status(code));

		ResponseEntity<HealthResponse> response = controller.health();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(code);
	}

	private void givenStatus(Status status) {
		// HealthDescriptor is sealed, so mock its public non-final subtype
		CompositeHealthDescriptor descriptor = mock(CompositeHealthDescriptor.class);
		when(descriptor.getStatus()).thenReturn(status);
		when(healthEndpoint.health()).thenReturn(descriptor);
	}

}

package com.mgs.claudecode.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTests {

	private static final AtomicBoolean down = new AtomicBoolean();

	@Autowired
	private MockMvc mvc;

	@AfterEach
	void reset() {
		down.set(false);
	}

	@Test
	void returnsOkWhenUp() throws Exception {
		mvc.perform(get("/api/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void returnsServiceUnavailableWhenAnyCheckIsDown() throws Exception {
		down.set(true);
		mvc.perform(get("/api/health"))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.status").value("DOWN"));
	}

	@Test
	void actuatorHealthEndpointIsNotExposed() throws Exception {
		// Authenticated, so a 404 proves the endpoints do not exist rather than being hidden by security
		mvc.perform(get("/actuator/health").with(jwt())).andExpect(status().isNotFound());
		mvc.perform(get("/actuator").with(jwt())).andExpect(status().isNotFound());
	}

	@TestConfiguration
	static class Config {

		@Bean
		HealthIndicator toggleHealthIndicator() {
			return () -> down.get() ? Health.down().build() : Health.up().build();
		}

	}

}

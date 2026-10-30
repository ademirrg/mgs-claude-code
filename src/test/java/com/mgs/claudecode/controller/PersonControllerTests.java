package com.mgs.claudecode.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersonControllerTests {

	private static final String ALICE = """
			{"firstName":"Alice","lastName":"Smith","email":"alice@example.com","birthDate":"1990-05-01"}
			""";

	@Autowired
	private MockMvc mvc;

	@Test
	void crudLifecycle() throws Exception {
		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON).content(ALICE))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/api/persons/1"))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.firstName").value("Alice"));

		mvc.perform(get("/api/persons").with(admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));

		mvc.perform(get("/api/persons/1").with(admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("alice@example.com"));

		mvc.perform(put("/api/persons/1").with(admin()).contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"firstName":"Alice","lastName":"Jones","email":"alice@example.com","birthDate":"1990-05-01"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lastName").value("Jones"));

		mvc.perform(delete("/api/persons/1").with(admin())).andExpect(status().isNoContent());
		mvc.perform(get("/api/persons/1").with(admin())).andExpect(status().isNotFound());
	}

	@Test
	void rejectsInvalidPayload() throws Exception {
		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"firstName":"","lastName":"Smith","email":"not-an-email"}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsDuplicateEmail() throws Exception {
		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON).content(ALICE))
			.andExpect(status().isCreated());
		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON).content(ALICE))
			.andExpect(status().isConflict());
	}

	@Test
	void unknownIdReturnsNotFound() throws Exception {
		mvc.perform(get("/api/persons/99").with(admin())).andExpect(status().isNotFound());
		mvc.perform(delete("/api/persons/99").with(admin())).andExpect(status().isNotFound());
	}

	private static JwtRequestPostProcessor admin() {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
	}

}

package com.mgs.claudecode.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTests {

	private static final String ALICE = """
			{"firstName":"Alice","lastName":"Smith","email":"alice@example.com","birthDate":"1990-05-01"}
			""";

	@Autowired
	private MockMvc mvc;

	@Test
	void loginReturnsBearerToken() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"admin","password":"admin123"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.expiresIn").value(3600));
	}

	@Test
	void loginRejectsWrongPassword() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"admin","password":"wrong"}
					"""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginRejectsUnknownUser() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"nobody","password":"admin123"}
					"""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginRejectsBlankCredentials() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"","password":""}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void adminTokenCanReadAndWrite() throws Exception {
		String token = login("admin", "admin123");

		mvc.perform(post("/api/persons").header(HttpHeaders.AUTHORIZATION, bearer(token))
			.contentType(MediaType.APPLICATION_JSON)
			.content(ALICE)).andExpect(status().isCreated());
		mvc.perform(get("/api/persons").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));
		mvc.perform(delete("/api/persons/1").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isNoContent());
	}

	@Test
	void userTokenCanReadButNotWrite() throws Exception {
		String token = login("user", "user123");

		mvc.perform(get("/api/persons").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isOk());
		mvc.perform(post("/api/persons").header(HttpHeaders.AUTHORIZATION, bearer(token))
			.contentType(MediaType.APPLICATION_JSON)
			.content(ALICE)).andExpect(status().isForbidden());
		mvc.perform(delete("/api/persons/1").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isForbidden());
	}

	@Test
	void missingTokenIsUnauthorized() throws Exception {
		mvc.perform(get("/api/persons")).andExpect(status().isUnauthorized());
	}

	@Test
	void tamperedTokenIsUnauthorized() throws Exception {
		String token = login("user", "user123");
		String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

		mvc.perform(get("/api/persons").header(HttpHeaders.AUTHORIZATION, bearer(tampered)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void publicEndpointsNeedNoToken() throws Exception {
		mvc.perform(get("/api/health")).andExpect(status().isOk());
		mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
	}

	private String login(String username, String password) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return JsonPath.read(body, "$.accessToken");
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

}

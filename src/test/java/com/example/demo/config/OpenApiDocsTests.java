package com.example.demo.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void servesOpenApiSpec() throws Exception {
		mvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.info.title").value("mgs-claude-code API"))
			.andExpect(jsonPath("$.paths['/api/persons']").exists())
			.andExpect(jsonPath("$.paths['/api/persons/{id}']").exists())
			.andExpect(jsonPath("$.paths['/api/health']").exists());
	}

}

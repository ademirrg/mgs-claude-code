package com.mgs.claudecode.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mgs.claudecode.config.SecurityConfig;
import com.mgs.claudecode.dto.PersonRequest;
import com.mgs.claudecode.dto.PersonResponse;
import com.mgs.claudecode.exception.EmailAlreadyInUseException;
import com.mgs.claudecode.exception.PersonNotFoundException;
import com.mgs.claudecode.service.PersonService;

@WebMvcTest(PersonController.class)
@Import(SecurityConfig.class)
class PersonControllerWebMvcTests {

	private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 1);

	private static final String ALICE_JSON = """
			{"firstName":"Alice","lastName":"Smith","email":"alice@example.com","birthDate":"1990-05-01"}
			""";

	private static final PersonRequest ALICE_REQUEST = new PersonRequest("Alice", "Smith", "alice@example.com",
			BIRTH_DATE);

	private static final PersonResponse ALICE = new PersonResponse(1L, "Alice", "Smith", "alice@example.com",
			BIRTH_DATE);

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private PersonService service;

	@Test
	void findAllReturnsPeople() throws Exception {
		when(service.findAll()).thenReturn(
				List.of(ALICE, new PersonResponse(2L, "Bob", "Brown", "bob@example.com", null)));

		mvc.perform(get("/api/persons").with(admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].birthDate").value("1990-05-01"))
			.andExpect(jsonPath("$[1].email").value("bob@example.com"));
	}

	@Test
	void findAllReturnsEmptyArray() throws Exception {
		when(service.findAll()).thenReturn(List.of());

		mvc.perform(get("/api/persons").with(admin())).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void findByIdReturnsPerson() throws Exception {
		when(service.findById(1L)).thenReturn(ALICE);

		mvc.perform(get("/api/persons/1").with(admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.firstName").value("Alice"))
			.andExpect(jsonPath("$.lastName").value("Smith"))
			.andExpect(jsonPath("$.email").value("alice@example.com"));
	}

	@Test
	void findByIdReturnsNotFound() throws Exception {
		when(service.findById(99L)).thenThrow(new PersonNotFoundException(99L));

		mvc.perform(get("/api/persons/99").with(admin())).andExpect(status().isNotFound());
	}

	@Test
	void findByIdRejectsNonNumericId() throws Exception {
		mvc.perform(get("/api/persons/abc").with(admin())).andExpect(status().isBadRequest());
		verifyNoInteractions(service);
	}

	@Test
	void createReturnsCreatedWithLocation() throws Exception {
		when(service.create(ALICE_REQUEST)).thenReturn(ALICE);

		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON).content(ALICE_JSON))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/api/persons/1"))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.firstName").value("Alice"));
	}

	@Test
	void createRejectsInvalidPayload() throws Exception {
		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"firstName":"","lastName":"Smith","email":"not-an-email"}
					"""))
			.andExpect(status().isBadRequest());
		verifyNoInteractions(service);
	}

	@Test
	void createRejectsFutureBirthDate() throws Exception {
		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"firstName":"Alice","lastName":"Smith","email":"alice@example.com","birthDate":"2999-01-01"}
					"""))
			.andExpect(status().isBadRequest());
		verifyNoInteractions(service);
	}

	@Test
	void createRejectsMalformedJson() throws Exception {
		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON).content("{not json"))
			.andExpect(status().isBadRequest());
		verifyNoInteractions(service);
	}

	@Test
	void createReturnsConflictForDuplicateEmail() throws Exception {
		when(service.create(any())).thenThrow(new EmailAlreadyInUseException("alice@example.com"));

		mvc.perform(post("/api/persons").with(admin()).contentType(MediaType.APPLICATION_JSON).content(ALICE_JSON))
			.andExpect(status().isConflict());
	}

	@Test
	void updateReturnsUpdatedPerson() throws Exception {
		PersonRequest request = new PersonRequest("Alice", "Jones", "alice@example.com", BIRTH_DATE);
		when(service.update(1L, request))
			.thenReturn(new PersonResponse(1L, "Alice", "Jones", "alice@example.com", BIRTH_DATE));

		mvc.perform(put("/api/persons/1").with(admin()).contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"firstName":"Alice","lastName":"Jones","email":"alice@example.com","birthDate":"1990-05-01"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lastName").value("Jones"));
	}

	@Test
	void updateRejectsInvalidPayload() throws Exception {
		mvc.perform(put("/api/persons/1").with(admin()).contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"firstName":"Alice","lastName":"","email":"alice@example.com"}
					"""))
			.andExpect(status().isBadRequest());
		verifyNoInteractions(service);
	}

	@Test
	void updateReturnsNotFound() throws Exception {
		when(service.update(anyLong(), any())).thenThrow(new PersonNotFoundException(99L));

		mvc.perform(put("/api/persons/99").with(admin()).contentType(MediaType.APPLICATION_JSON).content(ALICE_JSON))
			.andExpect(status().isNotFound());
	}

	@Test
	void updateReturnsConflictForDuplicateEmail() throws Exception {
		when(service.update(anyLong(), any())).thenThrow(new EmailAlreadyInUseException("alice@example.com"));

		mvc.perform(put("/api/persons/1").with(admin()).contentType(MediaType.APPLICATION_JSON).content(ALICE_JSON))
			.andExpect(status().isConflict());
	}

	@Test
	void deleteReturnsNoContent() throws Exception {
		mvc.perform(delete("/api/persons/1").with(admin())).andExpect(status().isNoContent());
		verify(service).delete(1L);
	}

	@Test
	void deleteReturnsNotFound() throws Exception {
		doThrow(new PersonNotFoundException(99L)).when(service).delete(99L);

		mvc.perform(delete("/api/persons/99").with(admin())).andExpect(status().isNotFound());
	}

	@Test
	void anonymousRequestsAreUnauthorized() throws Exception {
		mvc.perform(get("/api/persons")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/persons/1")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/persons").contentType(MediaType.APPLICATION_JSON).content(ALICE_JSON))
			.andExpect(status().isUnauthorized());
		mvc.perform(put("/api/persons/1").contentType(MediaType.APPLICATION_JSON).content(ALICE_JSON))
			.andExpect(status().isUnauthorized());
		mvc.perform(delete("/api/persons/1")).andExpect(status().isUnauthorized());
		verifyNoInteractions(service);
	}

	@Test
	void userRoleCanRead() throws Exception {
		when(service.findAll()).thenReturn(List.of(ALICE));
		when(service.findById(1L)).thenReturn(ALICE);

		mvc.perform(get("/api/persons").with(user())).andExpect(status().isOk());
		mvc.perform(get("/api/persons/1").with(user())).andExpect(status().isOk());
	}

	@Test
	void userRoleCannotWrite() throws Exception {
		mvc.perform(post("/api/persons").with(user()).contentType(MediaType.APPLICATION_JSON).content(ALICE_JSON))
			.andExpect(status().isForbidden());
		mvc.perform(put("/api/persons/1").with(user()).contentType(MediaType.APPLICATION_JSON).content(ALICE_JSON))
			.andExpect(status().isForbidden());
		mvc.perform(delete("/api/persons/1").with(user())).andExpect(status().isForbidden());
		verifyNoInteractions(service);
	}

	@Test
	void tokenWithoutRolesIsForbidden() throws Exception {
		mvc.perform(get("/api/persons").with(jwt())).andExpect(status().isForbidden());
		verify(service, never()).findAll();
	}

	private static JwtRequestPostProcessor admin() {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
	}

	private static JwtRequestPostProcessor user() {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
	}

}

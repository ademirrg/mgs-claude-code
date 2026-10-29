package com.mgs.claudecode.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgs.claudecode.dto.PersonRequest;
import com.mgs.claudecode.dto.PersonResponse;
import com.mgs.claudecode.service.PersonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/persons")
@Tag(name = "Persons", description = "Create, read, update and delete people")
public class PersonController {

	private final PersonService service;

	public PersonController(PersonService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "List all people")
	@ApiResponse(responseCode = "200", description = "All people")
	public List<PersonResponse> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a person by id")
	@ApiResponse(responseCode = "200", description = "The person")
	@ApiResponse(responseCode = "404", description = "Person not found")
	public PersonResponse findById(@Parameter(description = "Person id") @PathVariable Long id) {
		return service.findById(id);
	}

	@PostMapping
	@Operation(summary = "Create a person")
	@ApiResponse(responseCode = "201", description = "Person created; Location header points to it")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	@ApiResponse(responseCode = "409", description = "Email already in use")
	public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonRequest request) {
		PersonResponse created = service.create(request);
		return ResponseEntity.created(URI.create("/api/persons/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a person")
	@ApiResponse(responseCode = "200", description = "Person updated")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	@ApiResponse(responseCode = "404", description = "Person not found")
	@ApiResponse(responseCode = "409", description = "Email already in use by another person")
	public PersonResponse update(@Parameter(description = "Person id") @PathVariable Long id,
			@Valid @RequestBody PersonRequest request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a person")
	@ApiResponse(responseCode = "204", description = "Person deleted")
	@ApiResponse(responseCode = "404", description = "Person not found")
	public ResponseEntity<Void> delete(@Parameter(description = "Person id") @PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

}

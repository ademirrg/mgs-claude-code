package com.mgs.claudecode.dto;

import java.time.LocalDate;

import com.mgs.claudecode.entity.Person;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A stored person")
public record PersonResponse(
		@Schema(example = "1") Long id,
		@Schema(example = "Alice") String firstName,
		@Schema(example = "Smith") String lastName,
		@Schema(example = "alice@example.com") String email,
		@Schema(example = "1990-05-01") LocalDate birthDate) {

	public static PersonResponse from(Person person) {
		return new PersonResponse(person.getId(), person.getFirstName(), person.getLastName(), person.getEmail(),
				person.getBirthDate());
	}

}

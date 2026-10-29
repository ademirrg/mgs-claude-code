package com.example.demo.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for creating or updating a person")
public record PersonRequest(
		@Schema(example = "Alice") @NotBlank @Size(max = 100) String firstName,
		@Schema(example = "Smith") @NotBlank @Size(max = 100) String lastName,
		@Schema(description = "Must be unique", example = "alice@example.com") @NotBlank @Email @Size(max = 255) String email,
		@Schema(description = "Must be in the past", example = "1990-05-01") @Past LocalDate birthDate) {
}

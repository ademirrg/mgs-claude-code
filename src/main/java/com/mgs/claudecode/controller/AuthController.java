package com.mgs.claudecode.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mgs.claudecode.dto.LoginRequest;
import com.mgs.claudecode.dto.TokenResponse;
import com.mgs.claudecode.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Obtain JWT access tokens")
public class AuthController {

	private final AuthenticationManager authenticationManager;

	private final TokenService tokenService;

	public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
		this.authenticationManager = authenticationManager;
		this.tokenService = tokenService;
	}

	@PostMapping("/login")
	@Operation(summary = "Log in", description = "Exchanges a username and password for a JWT access token.")
	@ApiResponse(responseCode = "200", description = "Authenticated; the body holds the access token")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	@ApiResponse(responseCode = "401", description = "Wrong username or password")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return tokenService.issue(authenticationManager
			.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())));
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	void badCredentials() {
	}

}

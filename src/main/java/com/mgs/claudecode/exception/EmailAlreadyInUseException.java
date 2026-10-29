package com.mgs.claudecode.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyInUseException extends RuntimeException {

	public EmailAlreadyInUseException(String email) {
		super("Email already in use: " + email);
	}

}

package com.iuh.WiseOwlEnglish_Backend.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException(String email) {
        super(ErrorCode.EMAIL_EXISTS, HttpStatus.CONFLICT, "Email already exists: " + email);

    }
}

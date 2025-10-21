package com.iuh.WiseOwlEnglish_Backend.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(ErrorCode notFound, HttpStatus httpStatus, String what) {
        super(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, what + " not found");
    }
}

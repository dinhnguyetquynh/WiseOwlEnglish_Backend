package com.iuh.WiseOwlEnglish_Backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends ApiException{

    public ResourceAlreadyExistsException(String message) {
        super(ErrorCode.CONFLICT, HttpStatus.CONFLICT, message);
    }
}

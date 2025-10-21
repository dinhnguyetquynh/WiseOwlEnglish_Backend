package com.iuh.WiseOwlEnglish_Backend.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus status;
    public ApiException(ErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
    public ErrorCode getCode() {
        return code;
    }
    public HttpStatus getStatus() {
        return status;
    }

}

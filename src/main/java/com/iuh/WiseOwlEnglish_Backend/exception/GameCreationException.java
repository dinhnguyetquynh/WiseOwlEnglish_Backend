package com.iuh.WiseOwlEnglish_Backend.exception;

import org.springframework.http.HttpStatus;

public class GameCreationException extends ApiException{
    public GameCreationException(String message) {
        super(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, message);

    }
}

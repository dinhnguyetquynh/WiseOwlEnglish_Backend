package com.iuh.WiseOwlEnglish_Backend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private ErrorCode error;
    private String message;
    private Map<String, Object> details;
    private int status;
    private Instant timestamp;
}
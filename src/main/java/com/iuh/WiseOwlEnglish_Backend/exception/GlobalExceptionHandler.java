package com.iuh.WiseOwlEnglish_Backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bắt các ApiException do mình chủ động ném
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        var body =  new ErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                Map.of(),
                ex.getStatus().value(),
                Instant.now()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // Bắt lỗi validate @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fields.put(fe.getField(), fe.getDefaultMessage())
        );
        var body = new ErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                fields,
                HttpStatus.BAD_REQUEST.value(),
                Instant.now()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Còn lại (lỗi không lường trước)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
//        var body = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "Internal server error", null, HttpStatus.INTERNAL_SERVER_ERROR.value());
        var body = new ErrorResponse(
                ErrorCode.INTERNAL_ERROR,
                "Internal server error",
                Map.of(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Instant.now()
        );
        // TODO: log ex
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

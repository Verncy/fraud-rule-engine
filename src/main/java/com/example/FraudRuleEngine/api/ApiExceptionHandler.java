package com.example.FraudRuleEngine.api;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleBodyValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "message", "Validation failed",
                        "errors", ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                .toList()
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handlePathValidation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "message", "Validation failed",
                        "errors", ex.getConstraintViolations()
                                .stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .toList()
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMalformedJson() {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "message", "Malformed JSON request",
                        "errors", List.of("Invalid request body")
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("message", ex.getMessage())
        );
    }
}

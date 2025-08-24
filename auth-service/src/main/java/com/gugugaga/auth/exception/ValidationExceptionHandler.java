package com.gugugaga.auth.exception;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gugugaga.auth.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.*;

@RestControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>(); 
        ex.getBindingResult().getFieldErrors().forEach( error -> errors.put( error.getField(), error.getDefaultMessage()));
        OffsetDateTime timestamps = OffsetDateTime.now();
        String location = req.getRequestURI();
        ErrorResponse response = new ErrorResponse(
            timestamps, 400, "Validation Failed", "VALIDATION_ERROR", location, errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation( DataIntegrityViolationException ex, HttpServletRequest req ) {
        String message = "Terdapat data duplikat";
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof ConstraintViolationException constraintEx) {
            message = "Data duplikat pada field : " + constraintEx.getConstraintName();
        }

        ErrorResponse error = new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Database Error",
            message,
            req.getRequestURI(),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}

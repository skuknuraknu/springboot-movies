package com.gugugaga.auth.exception;

import com.gugugaga.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> respond( HttpStatus status, String message, HttpServletRequest request, Map<String, String> errors ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                message,
                status.getReasonPhrase(),
                request.getRequestURI(),
                (errors == null || errors.isEmpty()) ? null : errors
        );
        return ResponseEntity.status(status).body(body);
    }

    /** @Valid @RequestBody validation errors (MVC) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid( MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return respond(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }
    
}

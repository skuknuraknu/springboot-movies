package com.gugugaga.movie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ValidationErrorResponse> handleArgumentNotValid( MethodArgumentNotValidException ex, HttpServletRequest request ) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
                String path = request.getRequestURI();
                ValidationErrorResponse errorResponse = new ValidationErrorResponse(500, "Validation failed", path, errors, false);
                return ResponseEntity.badRequest().body(errorResponse);
        }
        @ExceptionHandler(VideoNotFoundException.class)
        public ResponseEntity<?> handleVideoNotFound(VideoNotFoundException ex, HttpServletRequest request) {
                String path = request.getRequestURI();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "path", path
                ));
        }
}

package com.gugugaga.movie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex,
            ServerWebExchange exchange  // swapped in WebFlux request context 🎉
    ) {
        // gather field errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        // build your response, grabbing the path from WebFlux request
        ValidationErrorResponse response = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                exchange.getRequest().getPath().value(),  // reactive path fetcher 🔥
                errors,
                false
        );

        // wrap in Mono and return a 400 ResponseEntity
        return Mono.just(ResponseEntity
                .badRequest()
                .body(response)
        );
    }
}

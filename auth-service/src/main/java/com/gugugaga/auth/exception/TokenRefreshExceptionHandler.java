package com.gugugaga.auth.exception;

public class TokenRefreshExceptionHandler extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TokenRefreshExceptionHandler(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }
}

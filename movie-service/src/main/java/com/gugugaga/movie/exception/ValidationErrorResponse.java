package com.gugugaga.movie.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String path;
    private Map<String, String> errors;
    private Boolean success;

    public ValidationErrorResponse( int status, String error, String path, Map<String, String> errors, Boolean success) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.path = path;
        this.errors = errors;
        this.success = success;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
    public Boolean getSuccess() {
        return success;
    }
}

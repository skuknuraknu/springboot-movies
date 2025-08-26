package com.gugugaga.auth.dto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse (
    Instant timeInstant,
    int status,
    String error, // short title error
    String message,
    String path,
    Map<String, String> errors // long description of the error
){}

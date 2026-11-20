package com.dispatch.loadbalancer.dto.response;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(String status, String message, List<String> errors,
                            String correlationId, Instant timestamp) {

    public static ErrorResponse of(String message, List<String> errors) {
        return new ErrorResponse("error", message, errors, MDC.get("correlationId"), Instant.now());
    }
}

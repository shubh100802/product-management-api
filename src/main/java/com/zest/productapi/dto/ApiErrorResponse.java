package com.zest.productapi.dto;

// ==========file-context==========

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ApiErrorResponse(
        @Schema(description = "Error timestamp in UTC")
        Instant timestamp,
        @Schema(description = "HTTP status code")
        int status,
        @Schema(description = "HTTP status reason")
        String error,
        @Schema(description = "Error details")
        String message,
        @Schema(description = "Request path that caused the error")
        String path
) {
}


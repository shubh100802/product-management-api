package com.zest.productapi.dto;

// ==========file-context==========

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ApiResponse<T>(
        @Schema(description = "Response creation timestamp in UTC")
        Instant timestamp,
        @Schema(description = "HTTP status code")
        int status,
        @Schema(description = "Human-readable status message")
        String message,
        @Schema(description = "Payload data")
        T data
) {
}


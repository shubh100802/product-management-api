package com.zest.productapi.util;

// ==========file-context==========

import com.zest.productapi.dto.ApiResponse;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(Instant.now(), status.value(), message, data);
    }
}


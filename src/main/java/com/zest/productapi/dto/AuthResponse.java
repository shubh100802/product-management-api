package com.zest.productapi.dto;

// ==========file-context==========

import java.util.Set;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Set<String> roles
) {
}

